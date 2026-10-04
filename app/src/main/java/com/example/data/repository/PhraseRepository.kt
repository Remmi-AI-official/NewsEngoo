package com.example.data.repository

import com.example.data.local.dao.PhraseDao
import com.example.data.local.entity.PhraseEntity
import com.example.domain.model.PhraseImportItem
import kotlinx.coroutines.flow.Flow

class PhraseRepository(private val phraseDao: PhraseDao) {
    fun getAllPhrases(): Flow<List<PhraseEntity>> = phraseDao.getAllPhrases()

    fun getPhrasesForDate(date: String): Flow<List<PhraseEntity>> = phraseDao.getPhrasesForDate(date)

    fun getPhraseById(id: String): Flow<PhraseEntity?> = phraseDao.getPhraseById(id)

    suspend fun savePhrase(phrase: PhraseEntity) = phraseDao.insertPhrase(phrase)

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = phraseDao.setFavorite(id, isFavorite)

    suspend fun setCompleted(id: String, isCompleted: Boolean) = phraseDao.setCompleted(id, isCompleted)

    suspend fun updateNote(id: String, note: String) = phraseDao.updateNote(id, note)

    suspend fun deletePhrase(id: String) = phraseDao.deletePhrase(id)

    suspend fun deletePhrasesForDate(date: String) = phraseDao.deletePhrasesForDate(date)

    suspend fun deleteAllPhrases() = phraseDao.deleteAllPhrases()

    fun getFavoritePhrases(): Flow<List<PhraseEntity>> = phraseDao.getFavoritePhrases()

    fun searchPhrases(query: String): Flow<List<PhraseEntity>> = phraseDao.searchPhrases(query)

    suspend fun importPhrases(items: List<PhraseImportItem>, date: String): Int {
        val entities = items.map { item ->
            PhraseEntity(
                id = "phrase_${date}_${item.phrase.lowercase().trim().replace(Regex("[^a-z0-9]"), "_")}",
                date = date,
                phrase = item.phrase.trim(),
                type = item.type,
                meaning = item.meaning.trim(),
                hindiMeaning = item.hindiMeaning.trim(),
                example = item.example.trim(),
                category = item.category.trim(),
                difficulty = item.difficulty,
                source = item.source.trim()
            )
        }
        phraseDao.insertPhrases(entities)
        return entities.size
    }
}
