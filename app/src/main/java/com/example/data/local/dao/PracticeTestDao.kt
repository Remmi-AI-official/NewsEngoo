package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.TestAttemptEntity
import com.example.data.local.entity.TestEntity
import com.example.data.local.entity.TestQuestionCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeTestDao {
    @Query("SELECT * FROM questions WHERE date = :date ORDER BY createdAt ASC")
    fun getQuestionsForDate(date: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions ORDER BY date DESC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions ORDER BY date DESC")
    suspend fun getAllQuestionsSync(): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    fun getQuestionById(id: String): Flow<QuestionEntity?>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionByIdSync(id: String): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("UPDATE questions SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setQuestionFavorite(id: String, isFavorite: Boolean)

    @Query("SELECT * FROM questions WHERE isFavorite = 1 ORDER BY date DESC")
    fun getFavoriteQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM tests WHERE id = :testId LIMIT 1")
    fun getTestById(testId: String): Flow<TestEntity?>

    @Query("SELECT * FROM tests WHERE id = :testId LIMIT 1")
    suspend fun getTestByIdSync(testId: String): TestEntity?

    @Query("SELECT * FROM tests WHERE date = :date")
    fun getTestsForDate(date: String): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests ORDER BY date DESC")
    fun getAllTests(): Flow<List<TestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkQuestionToTest(crossRef: TestQuestionCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkQuestionsToTest(crossRefs: List<TestQuestionCrossRef>)

    @Query("""
        SELECT q.* FROM questions q
        INNER JOIN test_question_cross_ref t ON q.id = t.questionId
        WHERE t.testId = :testId
        ORDER BY t.orderIndex ASC
    """)
    fun getQuestionsForTest(testId: String): Flow<List<QuestionEntity>>

    @Query("""
        SELECT q.* FROM questions q
        INNER JOIN test_question_cross_ref t ON q.id = t.questionId
        WHERE t.testId = :testId
        ORDER BY t.orderIndex ASC
    """)
    suspend fun getQuestionsForTestSync(testId: String): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: TestAttemptEntity)

    @Query("SELECT * FROM test_attempts ORDER BY completedAt DESC")
    fun getAllAttempts(): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts WHERE testId = :testId ORDER BY completedAt DESC")
    fun getAttemptsForTest(testId: String): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts WHERE id = :attemptId LIMIT 1")
    fun getAttemptById(attemptId: String): Flow<TestAttemptEntity?>

    @Query("SELECT * FROM questions WHERE question LIKE '%' || :query || '%' OR explanation LIKE '%' || :query || '%'")
    fun searchQuestions(query: String): Flow<List<QuestionEntity>>
}
