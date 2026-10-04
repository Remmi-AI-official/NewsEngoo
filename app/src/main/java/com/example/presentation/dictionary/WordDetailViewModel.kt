package com.example.presentation.dictionary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.EditorialApplication
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.VocabularyEntity
import com.example.domain.model.WordLearningStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WordDetailUiState(
    val word: VocabularyEntity? = null,
    val assignedCategories: List<CategoryEntity> = emptyList(),
    val allCategories: List<CategoryEntity> = emptyList(),
    val isEditingNote: Boolean = false,
    val practiceResult: Boolean? = null,
    val isLoading: Boolean = true
)

class WordDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorialApplication
    private val vocabRepo = app.vocabularyRepository

    private val _uiState = MutableStateFlow(WordDetailUiState())
    val uiState: StateFlow<WordDetailUiState> = _uiState.asStateFlow()

    private var currentWordId: String = ""

    fun loadWord(wordId: String) {
        currentWordId = wordId
        viewModelScope.launch {
            val word = vocabRepo.getWordById(wordId).first()
            val assigned = vocabRepo.getCategoriesForWord(wordId).first()
            val all = vocabRepo.getAllCategories().first()
            _uiState.value = WordDetailUiState(
                word = word,
                assignedCategories = assigned,
                allCategories = all,
                isLoading = false
            )
        }
    }

    fun toggleFavorite() {
        val word = _uiState.value.word ?: return
        viewModelScope.launch {
            val newFav = !word.isFavorite
            vocabRepo.toggleFavorite(word.id, newFav)
            _uiState.value = _uiState.value.copy(word = word.copy(isFavorite = newFav))
        }
    }

    fun saveNote(note: String) {
        val word = _uiState.value.word ?: return
        viewModelScope.launch {
            vocabRepo.updateNote(word.id, note)
            _uiState.value = _uiState.value.copy(word = word.copy(personalNote = note))
        }
    }

    fun assignCategory(categoryId: String) {
        viewModelScope.launch {
            vocabRepo.assignWordToCategory(currentWordId, categoryId)
            val updated = vocabRepo.getCategoriesForWord(currentWordId).first()
            _uiState.value = _uiState.value.copy(assignedCategories = updated)
        }
    }

    fun removeCategory(categoryId: String) {
        viewModelScope.launch {
            vocabRepo.removeWordFromCategory(currentWordId, categoryId)
            val updated = vocabRepo.getCategoriesForWord(currentWordId).first()
            _uiState.value = _uiState.value.copy(assignedCategories = updated)
        }
    }

    fun createFolderAndAssign(name: String, description: String = "") {
        viewModelScope.launch {
            vocabRepo.addCategory(name, description)
            val normalized = name.lowercase().trim().replace(Regex("[^a-z0-9]"), "_")
            val id = "cat_${normalized}"
            vocabRepo.assignWordToCategory(currentWordId, id)
            val updatedAssigned = vocabRepo.getCategoriesForWord(currentWordId).first()
            val updatedAll = vocabRepo.getAllCategories().first()
            _uiState.value = _uiState.value.copy(
                assignedCategories = updatedAssigned,
                allCategories = updatedAll
            )
        }
    }

    fun recordPractice(isCorrect: Boolean) {
        viewModelScope.launch {
            vocabRepo.recordReview(currentWordId, isCorrect)
            val updated = vocabRepo.getWordById(currentWordId).first()
            _uiState.value = _uiState.value.copy(
                word = updated,
                practiceResult = isCorrect
            )
        }
    }
}
