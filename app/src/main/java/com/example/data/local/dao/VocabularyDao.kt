package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.EditorialVocabularyCrossRef
import com.example.data.local.entity.VocabularyEntity
import com.example.data.local.entity.WordCategoryCrossRef
import kotlinx.coroutines.flow.Flow

data class CategoryWordCountTuple(
    val categoryId: String,
    val count: Int
)

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary_words ORDER BY word ASC")
    fun getAllWords(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE id = :id LIMIT 1")
    fun getWordById(id: String): Flow<VocabularyEntity?>

    @Query("SELECT * FROM vocabulary_words WHERE id = :id LIMIT 1")
    suspend fun getWordByIdSync(id: String): VocabularyEntity?

    @Query("SELECT * FROM vocabulary_words WHERE word = :word COLLATE NOCASE LIMIT 1")
    suspend fun getWordByNameSync(word: String): VocabularyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: VocabularyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<VocabularyEntity>)

    @Update
    suspend fun updateWord(word: VocabularyEntity)

    @Query("DELETE FROM vocabulary_words WHERE id = :id")
    suspend fun deleteWord(id: String)

    @Query("DELETE FROM vocabulary_words WHERE id IN (:ids)")
    suspend fun deleteWords(ids: List<String>)

    @Query("""
        SELECT v.* FROM vocabulary_words v
        INNER JOIN editorial_vocabulary_cross_ref c ON v.id = c.wordId
        WHERE c.date = :date
        ORDER BY v.word ASC
    """)
    fun getWordsForDate(date: String): Flow<List<VocabularyEntity>>

    @Query("""
        SELECT v.* FROM vocabulary_words v
        INNER JOIN editorial_vocabulary_cross_ref c ON v.id = c.wordId
        WHERE c.editorialId = :editorialId
        ORDER BY v.word ASC
    """)
    fun getWordsForEditorial(editorialId: String): Flow<List<VocabularyEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkWordToEditorial(crossRef: EditorialVocabularyCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkWordToCategory(crossRef: WordCategoryCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkWordsToCategory(crossRefs: List<WordCategoryCrossRef>)

    @Query("UPDATE vocabulary_categories SET name = :newName, description = :newDesc WHERE id = :categoryId")
    suspend fun updateCategory(categoryId: String, newName: String, newDesc: String)

    @Query("SELECT categoryId, COUNT(*) as count FROM word_category_cross_ref GROUP BY categoryId")
    fun getCategoryWordCounts(): Flow<List<CategoryWordCountTuple>>

    @Query("SELECT * FROM word_category_cross_ref")
    fun getAllWordCategoryRefs(): Flow<List<WordCategoryCrossRef>>

    @Query("DELETE FROM word_category_cross_ref WHERE categoryId = :categoryId")
    suspend fun clearWordsForCategory(categoryId: String)

    @Query("DELETE FROM word_category_cross_ref WHERE wordId = :wordId AND categoryId = :categoryId")
    suspend fun unlinkWordFromCategory(wordId: String, categoryId: String)

    @Query("DELETE FROM word_category_cross_ref WHERE wordId = :wordId")
    suspend fun clearCategoriesForWord(wordId: String)

    @Query("""
        SELECT c.* FROM vocabulary_categories c
        INNER JOIN word_category_cross_ref w ON c.id = w.categoryId
        WHERE w.wordId = :wordId
    """)
    fun getCategoriesForWord(wordId: String): Flow<List<CategoryEntity>>

    @Query("""
        SELECT v.* FROM vocabulary_words v
        INNER JOIN word_category_cross_ref w ON v.id = w.wordId
        WHERE w.categoryId = :categoryId
        ORDER BY v.word ASC
    """)
    fun getWordsForCategory(categoryId: String): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("DELETE FROM vocabulary_categories WHERE id = :id")
    suspend fun deleteCategory(id: String)

    @Query("SELECT * FROM vocabulary_words WHERE isFavorite = 1 ORDER BY word ASC")
    fun getFavoriteWords(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE learningStatus = :status ORDER BY word ASC")
    fun getWordsByStatus(status: String): Flow<List<VocabularyEntity>>

    @Query("""
        SELECT * FROM vocabulary_words 
        WHERE nextReviewDate != '' AND nextReviewDate <= :todayDate 
        ORDER BY nextReviewDate ASC, word ASC
    """)
    fun getWordsDueForRevision(todayDate: String): Flow<List<VocabularyEntity>>

    @Query("""
        SELECT * FROM vocabulary_words 
        WHERE word LIKE '%' || :query || '%' 
           OR meaning LIKE '%' || :query || '%'
           OR hindiMeaning LIKE '%' || :query || '%'
           OR synonyms LIKE '%' || :query || '%'
        ORDER BY word ASC
    """)
    fun searchWords(query: String): Flow<List<VocabularyEntity>>

    @Query("""
        UPDATE vocabulary_words 
        SET learningStatus = :status, 
            nextReviewDate = :nextReviewDate,
            reviewCount = reviewCount + 1,
            correctCount = correctCount + :correctDelta,
            incorrectCount = incorrectCount + :incorrectDelta,
            confidence = :confidence,
            lastReviewedAt = :timestamp
        WHERE id = :wordId
    """)
    suspend fun updateReviewResult(
        wordId: String,
        status: String,
        nextReviewDate: String,
        correctDelta: Int,
        incorrectDelta: Int,
        confidence: Int,
        timestamp: Long
    )

    @Query("UPDATE vocabulary_words SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE vocabulary_words SET personalNote = :note WHERE id = :id")
    suspend fun updateNote(id: String, note: String)

    @Query("SELECT COUNT(*) FROM vocabulary_words")
    fun getTotalWordsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE learningStatus = 'MASTERED'")
    fun getMasteredWordsCount(): Flow<Int>

    @Query("DELETE FROM vocabulary_words")
    suspend fun deleteAllWords()

    @Query("DELETE FROM editorial_vocabulary_cross_ref WHERE date = :date")
    suspend fun deleteEditorialVocabCrossRefsForDate(date: String)
}
