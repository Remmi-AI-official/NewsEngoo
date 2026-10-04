package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DailyProgressEntity
import com.example.data.local.entity.ImportHistoryEntity
import com.example.data.local.entity.MistakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MistakeDao {
    @Query("SELECT * FROM mistakes ORDER BY isImproved ASC, repeatCount DESC, lastMistakeDate DESC")
    fun getAllMistakes(): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistakes WHERE isImproved = 0 ORDER BY repeatCount DESC, lastMistakeDate DESC")
    fun getUnimprovedMistakes(): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistakes WHERE id = :id LIMIT 1")
    suspend fun getMistakeByIdSync(id: String): MistakeEntity?

    @Query("SELECT * FROM mistakes WHERE questionId = :questionId LIMIT 1")
    suspend fun getMistakeByQuestionIdSync(questionId: String): MistakeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMistake(mistake: MistakeEntity)

    @Query("UPDATE mistakes SET isImproved = :isImproved, lastReviewedAt = :timestamp WHERE id = :id")
    suspend fun markMistakeImproved(id: String, isImproved: Boolean, timestamp: Long)

    @Query("DELETE FROM mistakes WHERE id = :id")
    suspend fun deleteMistake(id: String)
}

@Dao
interface DailyProgressDao {
    @Query("SELECT * FROM daily_learning_progress WHERE date = :date LIMIT 1")
    fun getDailyProgress(date: String): Flow<DailyProgressEntity?>

    @Query("SELECT * FROM daily_learning_progress WHERE date = :date LIMIT 1")
    suspend fun getDailyProgressSync(date: String): DailyProgressEntity?

    @Query("SELECT * FROM daily_learning_progress ORDER BY date DESC")
    fun getAllDailyProgress(): Flow<List<DailyProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyProgress(progress: DailyProgressEntity)

    @Query("SELECT date FROM daily_learning_progress WHERE editorialRead = 1 OR testTaken = 1 OR vocabLearnedCount > 0 ORDER BY date DESC")
    fun getActiveDates(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM daily_learning_progress WHERE editorialRead = 1")
    fun getEditorialsReadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_learning_progress WHERE testTaken = 1")
    fun getTestsCompletedCount(): Flow<Int>
}

@Dao
interface ImportHistoryDao {
    @Query("SELECT * FROM import_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ImportHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: ImportHistoryEntity)

    @Query("DELETE FROM import_history WHERE id = :id")
    suspend fun deleteHistory(id: String)

    @Query("DELETE FROM import_history")
    suspend fun clearHistory()
}
