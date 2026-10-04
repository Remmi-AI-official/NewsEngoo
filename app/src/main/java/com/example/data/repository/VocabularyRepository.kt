package com.example.data.repository

import com.example.data.local.dao.VocabularyDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.EditorialVocabularyCrossRef
import com.example.data.local.entity.VocabularyEntity
import com.example.data.local.entity.WordCategoryCrossRef
import com.example.domain.model.DateUtils
import com.example.domain.model.ImportMode
import com.example.domain.model.SpacedRepetitionCalculator
import com.example.domain.model.VocabularyImportItem
import com.example.domain.model.WordLearningStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class VocabImportSummary(
    val totalProcessed: Int,
    val newCount: Int,
    val mergedCount: Int,
    val skippedCount: Int
)

class VocabularyRepository(private val vocabularyDao: VocabularyDao) {

    fun getAllWords(): Flow<List<VocabularyEntity>> = vocabularyDao.getAllWords()

    fun getWordById(id: String): Flow<VocabularyEntity?> = vocabularyDao.getWordById(id)

    suspend fun getWordByIdSync(id: String): VocabularyEntity? = vocabularyDao.getWordByIdSync(id)

    fun getWordsForDate(date: String): Flow<List<VocabularyEntity>> = vocabularyDao.getWordsForDate(date)

    fun getWordsForEditorial(editorialId: String): Flow<List<VocabularyEntity>> =
        vocabularyDao.getWordsForEditorial(editorialId)

    fun getWordsByCategory(categoryId: String): Flow<List<VocabularyEntity>> =
        vocabularyDao.getWordsForCategory(categoryId)

    fun getCategoriesForWord(wordId: String): Flow<List<CategoryEntity>> =
        vocabularyDao.getCategoriesForWord(wordId)

    fun getAllCategories(): Flow<List<CategoryEntity>> = vocabularyDao.getAllCategories()

    fun getAllWordCategoryRefs(): Flow<List<WordCategoryCrossRef>> = vocabularyDao.getAllWordCategoryRefs()

    suspend fun ensureDefaultCategories() {
        val defaults = listOf(
            CategoryEntity("cat_handpicked", "⭐ Hand-picked / Chune hue", "Personal list of handpicked words to master", "#D97706", true),
            CategoryEntity("cat_economy", "📈 Economy & Finance", "Banking, inflation, trade, fiscal policies", "#2563EB", true),
            CategoryEntity("cat_polity", "🏛️ Polity & Governance", "Constitution, judiciary, governance, laws", "#7C3AED", true),
            CategoryEntity("cat_geopolitics", "🌍 Geopolitics & Foreign Affairs", "International relations, summits, diplomacy", "#059669", true),
            CategoryEntity("cat_science", "🔬 Science, Tech & Climate", "AI, technology, climate change, space", "#0284C7", true),
            CategoryEntity("cat_advanced", "🎓 High-Frequency & Exams", "Essential vocabulary for competitive exams & reading", "#DC2626", true)
        )
        for (cat in defaults) {
            vocabularyDao.insertCategory(cat)
        }
    }

    suspend fun addCategory(name: String, description: String = "", colorHex: String = "#1A365D") {
        val normalized = name.lowercase().trim().replace(Regex("[^a-z0-9]"), "_")
        val id = if (normalized.isNotBlank()) "cat_${normalized}" else "cat_${System.currentTimeMillis()}"
        vocabularyDao.insertCategory(
            CategoryEntity(
                id = id,
                name = name.trim(),
                description = description.trim(),
                colorHex = colorHex
            )
        )
    }

    suspend fun renameCategory(id: String, newName: String, newDescription: String) {
        vocabularyDao.updateCategory(id, newName.trim(), newDescription.trim())
    }

    suspend fun deleteCategory(id: String) {
        vocabularyDao.clearWordsForCategory(id)
        vocabularyDao.deleteCategory(id)
    }

    suspend fun assignWordToCategory(wordId: String, categoryId: String) {
        vocabularyDao.linkWordToCategory(WordCategoryCrossRef(wordId = wordId, categoryId = categoryId))
    }

    suspend fun assignWordsToCategory(wordIds: List<String>, categoryId: String) {
        val refs = wordIds.map { WordCategoryCrossRef(wordId = it, categoryId = categoryId) }
        vocabularyDao.linkWordsToCategory(refs)
    }

    fun getCategoryWordCounts(): Flow<Map<String, Int>> {
        return vocabularyDao.getCategoryWordCounts().map { list ->
            list.associate { it.categoryId to it.count }
        }
    }

    suspend fun removeWordFromCategory(wordId: String, categoryId: String) {
        vocabularyDao.unlinkWordFromCategory(wordId = wordId, categoryId = categoryId)
    }

    fun getFavoriteWords(): Flow<List<VocabularyEntity>> = vocabularyDao.getFavoriteWords()

    fun getWordsByStatus(status: String): Flow<List<VocabularyEntity>> = vocabularyDao.getWordsByStatus(status)

    fun getWordsDueForRevision(todayDate: String = DateUtils.getTodayDate()): Flow<List<VocabularyEntity>> =
        vocabularyDao.getWordsDueForRevision(todayDate)

    fun searchWords(query: String): Flow<List<VocabularyEntity>> = vocabularyDao.searchWords(query)

    suspend fun toggleFavorite(wordId: String, isFavorite: Boolean) =
        vocabularyDao.setFavorite(wordId, isFavorite)

    suspend fun updateNote(wordId: String, note: String) =
        vocabularyDao.updateNote(wordId, note)

    suspend fun saveWord(word: VocabularyEntity) = vocabularyDao.insertWord(word)

    suspend fun updateWord(word: VocabularyEntity) = vocabularyDao.updateWord(word)

    suspend fun deleteWord(wordId: String) = vocabularyDao.deleteWord(wordId)

    suspend fun clearAll() = vocabularyDao.clearAll()

    suspend fun unlinkWordsForDate(date: String) = vocabularyDao.unlinkWordsForDate(date)

    suspend fun unlinkWordsForEditorial(editorialId: String) = vocabularyDao.unlinkWordsForEditorial(editorialId)

    suspend fun deleteUnlinkedWords() = vocabularyDao.deleteUnlinkedWords()

    fun getTotalCount(): Flow<Int> = vocabularyDao.getTotalWordsCount()

    fun getMasteredCount(): Flow<Int> = vocabularyDao.getMasteredWordsCount()

    suspend fun linkWordToEditorial(editorialId: String, wordId: String, date: String) {
        vocabularyDao.linkWordToEditorial(
            EditorialVocabularyCrossRef(
                editorialId = editorialId,
                wordId = wordId,
                date = date
            )
        )
    }

    /**
     * Records review/practice attempt on a word and updates spaced repetition schedule.
     */
    suspend fun recordReview(wordId: String, isCorrect: Boolean, todayDate: String = DateUtils.getTodayDate()) {
        val current = vocabularyDao.getWordByIdSync(wordId) ?: return
        val currentStatus = WordLearningStatus.fromString(current.learningStatus)
        val result = SpacedRepetitionCalculator.calculateNextReview(currentStatus, isCorrect, todayDate)

        vocabularyDao.updateReviewResult(
            wordId = wordId,
            status = result.newStatus.name,
            nextReviewDate = result.nextReviewDate,
            correctDelta = if (isCorrect) 1 else 0,
            incorrectDelta = if (!isCorrect) 1 else 0,
            confidence = result.confidence,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Imports vocabulary items with deduplication handling.
     */
    suspend fun importWords(
        items: List<VocabularyImportItem>,
        date: String,
        editorialId: String = "",
        mode: ImportMode = ImportMode.MERGE_DUPLICATES
    ): VocabImportSummary {
        var newCount = 0
        var mergedCount = 0
        var skippedCount = 0

        for (item in items) {
            val normalizedId = item.word.trim().lowercase()
            val existing = vocabularyDao.getWordByIdSync(normalizedId)

            if (existing == null) {
                // New word
                val newEntity = VocabularyEntity(
                    id = normalizedId,
                    word = item.word.trim(),
                    pronunciation = "",
                    partOfSpeech = item.partOfSpeech.ifBlank { "word" },
                    meaning = item.meaning.trim(),
                    hindiMeaning = item.hindiMeaning.trim(),
                    synonyms = item.synonyms,
                    antonyms = item.antonyms,
                    exampleSentence = item.example.trim(),
                    wordFamily = item.wordFamily.trim(),
                    difficulty = item.difficulty,
                    sourceArticle = item.source.ifBlank { editorialId },
                    isFavorite = false,
                    learningStatus = WordLearningStatus.NEW.name,
                    nextReviewDate = DateUtils.addDays(date, 1)
                )
                vocabularyDao.insertWord(newEntity)
                newCount++
            } else {
                when (mode) {
                    ImportMode.SKIP_DUPLICATES -> {
                        skippedCount++
                    }
                    ImportMode.REPLACE_EXISTING -> {
                        val replaced = existing.copy(
                            word = item.word.trim(),
                            partOfSpeech = item.partOfSpeech.ifBlank { existing.partOfSpeech },
                            meaning = item.meaning.trim(),
                            hindiMeaning = item.hindiMeaning.ifBlank { existing.hindiMeaning },
                            synonyms = item.synonyms.ifEmpty { existing.synonyms },
                            antonyms = item.antonyms.ifEmpty { existing.antonyms },
                            exampleSentence = item.example.ifBlank { existing.exampleSentence },
                            difficulty = item.difficulty.ifBlank { existing.difficulty }
                        )
                        vocabularyDao.insertWord(replaced)
                        mergedCount++
                    }
                    ImportMode.MERGE_DUPLICATES, ImportMode.IMPORT_NEW -> {
                        // Merge fields cleanly without losing existing learning progress
                        val mergedSynonyms = (existing.synonyms + item.synonyms).distinct()
                        val mergedAntonyms = (existing.antonyms + item.antonyms).distinct()
                        val merged = existing.copy(
                            meaning = if (existing.meaning.isBlank()) item.meaning.trim() else existing.meaning,
                            hindiMeaning = if (existing.hindiMeaning.isBlank()) item.hindiMeaning.trim() else existing.hindiMeaning,
                            synonyms = mergedSynonyms,
                            antonyms = mergedAntonyms,
                            exampleSentence = if (existing.exampleSentence.isBlank()) item.example.trim() else existing.exampleSentence,
                            partOfSpeech = if (existing.partOfSpeech == "word") item.partOfSpeech else existing.partOfSpeech
                        )
                        vocabularyDao.insertWord(merged)
                        mergedCount++
                    }
                }
            }

            // Always link to this date/editorial
            if (editorialId.isNotBlank() || date.isNotBlank()) {
                val effectiveEdId = editorialId.ifBlank { "ed_$date" }
                vocabularyDao.linkWordToEditorial(
                    EditorialVocabularyCrossRef(
                        editorialId = effectiveEdId,
                        wordId = normalizedId,
                        date = date
                    )
                )
            }

            // Link category if specified
            if (item.category.isNotBlank()) {
                val catId = "cat_${item.category.lowercase().trim().replace(Regex("[^a-z0-9]"), "_")}"
                vocabularyDao.insertCategory(
                    CategoryEntity(
                        id = catId,
                        name = item.category.trim()
                    )
                )
                vocabularyDao.linkWordToCategory(WordCategoryCrossRef(wordId = normalizedId, categoryId = catId))
            }
        }

        return VocabImportSummary(
            totalProcessed = items.size,
            newCount = newCount,
            mergedCount = mergedCount,
            skippedCount = skippedCount
        )
    }
}
