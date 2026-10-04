package com.example.data.repository

import com.example.data.local.dao.ImportHistoryDao
import com.example.data.local.entity.DailyProgressEntity
import com.example.data.local.entity.EditorialEntity
import com.example.data.local.entity.ImportHistoryEntity
import com.example.domain.model.ContentParser
import com.example.domain.model.DailyPackageImport
import com.example.domain.model.DateUtils
import com.example.domain.model.EditorialImportItem
import com.example.domain.model.GrammarImportItem
import com.example.domain.model.ImportMode
import com.example.domain.model.PhraseImportItem
import com.example.domain.model.QuestionImportItem
import com.example.domain.model.TestImportItem
import com.example.domain.model.VocabularyImportItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class PackageImportReport(
    val date: String,
    val editorialImported: Boolean,
    val vocabReport: VocabImportSummary,
    val grammarCount: Int,
    val phrasesCount: Int,
    val questionsCount: Int,
    val testId: String?
)

class ContentImportExportRepository(
    private val editorialRepository: EditorialRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val grammarRepository: GrammarRepository,
    private val phraseRepository: PhraseRepository,
    private val testRepository: PracticeTestRepository,
    private val progressRepository: ProgressRepository,
    private val importHistoryDao: ImportHistoryDao
) {
    fun getImportHistory(): Flow<List<ImportHistoryEntity>> = importHistoryDao.getAllHistory()

    suspend fun clearHistory() = importHistoryDao.clearHistory()

    /**
     * Imports a single editorial article.
     */
    suspend fun importEditorial(
        title: String,
        contentMarkdown: String,
        date: String,
        source: String = "The Daily Editorial",
        readTimeMinutes: Int = 5
    ): EditorialEntity {
        val id = "ed_$date"
        val entity = EditorialEntity(
            id = id,
            date = date,
            title = title.trim(),
            source = source.trim(),
            contentMarkdown = contentMarkdown.trim(),
            readTimeMinutes = readTimeMinutes
        )
        editorialRepository.saveEditorial(entity)

        importHistoryDao.insertHistory(
            ImportHistoryEntity(
                id = UUID.randomUUID().toString(),
                date = date,
                type = "editorial",
                itemCount = 1,
                newCount = 1,
                mergedCount = 0,
                description = "Editorial: \"${title.take(30)}\""
            )
        )
        return entity
    }

    /**
     * Imports a complete daily package.
     */
    suspend fun importDailyPackage(
        packageData: DailyPackageImport,
        mode: ImportMode = ImportMode.MERGE_DUPLICATES
    ): PackageImportReport {
        val date = packageData.date
        val edId = "ed_$date"

        // 1. Editorial
        var editorialImported = false
        if (packageData.editorial != null) {
            importEditorial(
                title = packageData.editorial.title,
                contentMarkdown = packageData.editorial.contentMarkdown,
                date = date,
                source = packageData.editorial.source,
                readTimeMinutes = packageData.editorial.readTimeMinutes
            )
            editorialImported = true
        }

        // 2. Vocabulary
        val vocabSummary = if (packageData.vocabulary.isNotEmpty()) {
            vocabularyRepository.importWords(
                items = packageData.vocabulary,
                date = date,
                editorialId = edId,
                mode = mode
            )
        } else {
            VocabImportSummary(0, 0, 0, 0)
        }

        // 3. Grammar
        val grammarCount = if (packageData.grammar.isNotEmpty()) {
            grammarRepository.importRules(packageData.grammar, date)
        } else 0

        // 4. Phrases
        val phrasesCount = if (packageData.phrases.isNotEmpty()) {
            phraseRepository.importPhrases(packageData.phrases, date)
        } else 0

        // 5. Practice Questions
        val questionsCount = if (packageData.questions.isNotEmpty()) {
            testRepository.importQuestions(packageData.questions, date, "test_$date")
        } else 0

        // 6. Test
        val testId = if (packageData.dailyTest != null) {
            testRepository.importTest(packageData.dailyTest, date)
        } else null

        // Initialize / update daily progress record
        val existingProgress = progressRepository.getDailyProgressSync(date)
        val progress = (existingProgress ?: DailyProgressEntity(date = date)).copy(
            totalVocabCount = if (packageData.vocabulary.isNotEmpty()) packageData.vocabulary.size else existingProgress?.totalVocabCount ?: 0,
            totalGrammarCount = if (packageData.grammar.isNotEmpty()) packageData.grammar.size else existingProgress?.totalGrammarCount ?: 0,
            totalExpressionsCount = if (packageData.phrases.isNotEmpty()) packageData.phrases.size else existingProgress?.totalExpressionsCount ?: 0,
            totalPracticeCount = if (packageData.questions.isNotEmpty()) packageData.questions.size else existingProgress?.totalPracticeCount ?: 0
        )
        progressRepository.updateVocabProgress(date, progress.vocabLearnedCount, progress.totalVocabCount)

        importHistoryDao.insertHistory(
            ImportHistoryEntity(
                id = UUID.randomUUID().toString(),
                date = date,
                type = "daily_package",
                itemCount = (if (editorialImported) 1 else 0) + vocabSummary.totalProcessed + grammarCount + phrasesCount + questionsCount,
                newCount = (if (editorialImported) 1 else 0) + vocabSummary.newCount + grammarCount + phrasesCount + questionsCount,
                mergedCount = vocabSummary.mergedCount,
                description = "Complete Daily Package for $date"
            )
        )

        return PackageImportReport(
            date = date,
            editorialImported = editorialImported,
            vocabReport = vocabSummary,
            grammarCount = grammarCount,
            phrasesCount = phrasesCount,
            questionsCount = questionsCount,
            testId = testId
        )
    }

    /**
     * Exports entire database content to structured JSON for backup.
     */
    suspend fun exportAllDataJson(): String {
        val root = JSONObject()
        root.put("version", "1.0")
        root.put("exportedAt", System.currentTimeMillis())

        // Editorials
        val editorials = editorialRepository.getAllEditorials().first()
        val edArray = JSONArray()
        for (ed in editorials) {
            val obj = JSONObject()
            obj.put("id", ed.id)
            obj.put("date", ed.date)
            obj.put("title", ed.title)
            obj.put("source", ed.source)
            obj.put("contentMarkdown", ed.contentMarkdown)
            obj.put("isFavorite", ed.isFavorite)
            obj.put("isCompleted", ed.isCompleted)
            edArray.put(obj)
        }
        root.put("editorials", edArray)

        // Vocabulary
        val words = vocabularyRepository.getAllWords().first()
        val wordsArray = JSONArray()
        for (w in words) {
            val obj = JSONObject()
            obj.put("word", w.word)
            obj.put("partOfSpeech", w.partOfSpeech)
            obj.put("meaning", w.meaning)
            obj.put("hindiMeaning", w.hindiMeaning)
            obj.put("synonyms", JSONArray(w.synonyms))
            obj.put("antonyms", JSONArray(w.antonyms))
            obj.put("example", w.exampleSentence)
            obj.put("difficulty", w.difficulty)
            obj.put("status", w.learningStatus)
            obj.put("isFavorite", w.isFavorite)
            wordsArray.put(obj)
        }
        root.put("vocabulary", wordsArray)

        // Grammar
        val rules = grammarRepository.getAllRules().first()
        val rulesArray = JSONArray()
        for (r in rules) {
            val obj = JSONObject()
            obj.put("date", r.date)
            obj.put("title", r.title)
            obj.put("rule", r.rule)
            obj.put("explanation", r.explanation)
            obj.put("correctExamples", JSONArray(r.correctExamples))
            obj.put("incorrectExamples", JSONArray(r.incorrectExamples))
            obj.put("commonMistakes", r.commonMistakes)
            obj.put("isFavorite", r.isFavorite)
            rulesArray.put(obj)
        }
        root.put("grammar", rulesArray)

        // Phrases
        val phrases = phraseRepository.getAllPhrases().first()
        val phrasesArray = JSONArray()
        for (p in phrases) {
            val obj = JSONObject()
            obj.put("date", p.date)
            obj.put("phrase", p.phrase)
            obj.put("type", p.type)
            obj.put("meaning", p.meaning)
            obj.put("hindiMeaning", p.hindiMeaning)
            obj.put("example", p.example)
            obj.put("isFavorite", p.isFavorite)
            phrasesArray.put(obj)
        }
        root.put("phrases", phrasesArray)

        return root.toString(2)
    }

    /**
     * Restores data from JSON backup.
     */
    suspend fun restoreBackupJson(jsonStr: String): Int {
        val root = JSONObject(jsonStr)
        var restoredCount = 0

        // Restore vocabulary
        val vocabArray = root.optJSONArray("vocabulary")
        if (vocabArray != null) {
            val vocabParsed = ContentParser.parseVocabularyJson(vocabArray.toString())
            if (vocabParsed.isValid) {
                val summary = vocabularyRepository.importWords(
                    items = vocabParsed.items,
                    date = DateUtils.getTodayDate(),
                    mode = ImportMode.MERGE_DUPLICATES
                )
                restoredCount += summary.totalProcessed
            }
        }

        // Restore grammar
        val grammarArray = root.optJSONArray("grammar")
        if (grammarArray != null) {
            val grammarParsed = ContentParser.parseGrammarJson(grammarArray.toString())
            if (grammarParsed.isValid) {
                restoredCount += grammarRepository.importRules(grammarParsed.items, DateUtils.getTodayDate())
            }
        }

        // Restore phrases
        val phrasesArray = root.optJSONArray("phrases")
        if (phrasesArray != null) {
            val phrasesParsed = ContentParser.parsePhrasesJson(phrasesArray.toString())
            if (phrasesParsed.isValid) {
                restoredCount += phraseRepository.importPhrases(phrasesParsed.items, DateUtils.getTodayDate())
            }
        }

        return restoredCount
    }
}
