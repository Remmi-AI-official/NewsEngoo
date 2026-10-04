package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.EditorialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EditorialDao {
    @Query("SELECT * FROM editorials ORDER BY date DESC, createdAt DESC")
    fun getAllEditorials(): Flow<List<EditorialEntity>>

    @Query("SELECT * FROM editorials WHERE date = :date LIMIT 1")
    fun getEditorialByDate(date: String): Flow<EditorialEntity?>

    @Query("SELECT * FROM editorials WHERE id = :id LIMIT 1")
    fun getEditorialById(id: String): Flow<EditorialEntity?>

    @Query("SELECT * FROM editorials WHERE id = :id LIMIT 1")
    suspend fun getEditorialByIdSync(id: String): EditorialEntity?

    @Query("SELECT * FROM editorials WHERE date = :date LIMIT 1")
    suspend fun getEditorialByDateSync(date: String): EditorialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditorial(editorial: EditorialEntity)

    @Update
    suspend fun updateEditorial(editorial: EditorialEntity)

    @Query("UPDATE editorials SET readingPosition = :position WHERE id = :id")
    suspend fun updateReadingPosition(id: String, position: Int)

    @Query("UPDATE editorials SET bookmarkParagraph = :paragraph, personalNote = :note WHERE id = :id")
    suspend fun updateBookmark(id: String, paragraph: Int, note: String)

    @Query("UPDATE editorials SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE editorials SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setCompleted(id: String, isCompleted: Boolean)

    @Query("DELETE FROM editorials WHERE id = :id")
    suspend fun deleteEditorial(id: String)

    @Query("SELECT * FROM editorials WHERE isFavorite = 1 ORDER BY date DESC")
    fun getFavoriteEditorials(): Flow<List<EditorialEntity>>

    @Query("SELECT * FROM editorials WHERE title LIKE '%' || :query || '%' OR contentMarkdown LIKE '%' || :query || '%'")
    fun searchEditorials(query: String): Flow<List<EditorialEntity>>
}
