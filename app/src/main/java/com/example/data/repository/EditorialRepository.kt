package com.example.data.repository

import com.example.data.local.dao.EditorialDao
import com.example.data.local.entity.EditorialEntity
import kotlinx.coroutines.flow.Flow

class EditorialRepository(private val editorialDao: EditorialDao) {
    fun getAllEditorials(): Flow<List<EditorialEntity>> = editorialDao.getAllEditorials()

    fun getEditorialByDate(date: String): Flow<EditorialEntity?> = editorialDao.getEditorialByDate(date)

    fun getEditorialById(id: String): Flow<EditorialEntity?> = editorialDao.getEditorialById(id)

    suspend fun getEditorialByIdSync(id: String): EditorialEntity? = editorialDao.getEditorialByIdSync(id)

    suspend fun getEditorialByDateSync(date: String): EditorialEntity? = editorialDao.getEditorialByDateSync(date)

    suspend fun saveEditorial(editorial: EditorialEntity) = editorialDao.insertEditorial(editorial)

    suspend fun updateEditorial(editorial: EditorialEntity) = editorialDao.updateEditorial(editorial)

    suspend fun deleteEditorialByDate(date: String) = editorialDao.deleteEditorialByDate(date)

    suspend fun updateReadingPosition(id: String, position: Int) = editorialDao.updateReadingPosition(id, position)

    suspend fun updateBookmark(id: String, paragraphIndex: Int, note: String) =
        editorialDao.updateBookmark(id, paragraphIndex, note)

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = editorialDao.setFavorite(id, isFavorite)

    suspend fun setCompleted(id: String, isCompleted: Boolean) = editorialDao.setCompleted(id, isCompleted)

    suspend fun deleteEditorial(id: String) = editorialDao.deleteEditorial(id)

    fun getFavoriteEditorials(): Flow<List<EditorialEntity>> = editorialDao.getFavoriteEditorials()

    fun searchEditorials(query: String): Flow<List<EditorialEntity>> = editorialDao.searchEditorials(query)
}
