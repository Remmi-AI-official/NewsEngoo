package com.example.presentation.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.EditorialApplication
import com.example.data.local.entity.EditorialEntity
import com.example.data.local.entity.VocabularyEntity
import com.example.data.repository.ReaderSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditorialReaderUiState(
    val editorial: EditorialEntity? = null,
    val knownWords: List<VocabularyEntity> = emptyList(),
    val selectedWordForSheet: VocabularyEntity? = null,
    val readerSettings: ReaderSettings = ReaderSettings(),
    val isEditingNote: Boolean = false,
    val isLoading: Boolean = true
)

class EditorialReaderViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorialApplication
    private val editorialRepo = app.editorialRepository
    private val vocabRepo = app.vocabularyRepository
    private val progressRepo = app.progressRepository
    private val userPrefsRepo = app.userPreferencesRepository

    private val _uiState = MutableStateFlow(EditorialReaderUiState())
    val uiState: StateFlow<EditorialReaderUiState> = _uiState.asStateFlow()

    private var currentEditorialId: String = ""

    fun loadEditorial(editorialId: String) {
        currentEditorialId = editorialId
        viewModelScope.launch {
            val editorial = editorialRepo.getEditorialById(editorialId).first()
            val prefs = userPrefsRepo.preferencesFlow.first()
            val words = if (editorial != null) {
                vocabRepo.getWordsForDate(editorial.date).first()
            } else emptyList()

            _uiState.value = EditorialReaderUiState(
                editorial = editorial,
                knownWords = words,
                readerSettings = prefs.readerSettings,
                isLoading = false
            )
        }
    }

    fun selectWord(wordText: String) {
        viewModelScope.launch {
            val normalized = wordText.lowercase().trim()
            val word = vocabRepo.getWordByIdSync(normalized) 
                ?: _uiState.value.knownWords.firstOrNull { it.id == normalized || it.word.equals(wordText, ignoreCase = true) }
            _uiState.value = _uiState.value.copy(selectedWordForSheet = word)
        }
    }

    fun dismissWordSheet() {
        _uiState.value = _uiState.value.copy(selectedWordForSheet = null)
    }

    fun toggleFavorite() {
        val current = _uiState.value.editorial ?: return
        viewModelScope.launch {
            val newFav = !current.isFavorite
            editorialRepo.toggleFavorite(current.id, newFav)
            _uiState.value = _uiState.value.copy(editorial = current.copy(isFavorite = newFav))
        }
    }

    fun bookmarkParagraph(paragraphIndex: Int) {
        val current = _uiState.value.editorial ?: return
        viewModelScope.launch {
            val newIndex = if (current.bookmarkParagraph == paragraphIndex) -1 else paragraphIndex
            editorialRepo.updateBookmark(current.id, newIndex, current.personalNote)
            _uiState.value = _uiState.value.copy(editorial = current.copy(bookmarkParagraph = newIndex))
        }
    }

    fun saveArticleNote(note: String) {
        val current = _uiState.value.editorial ?: return
        viewModelScope.launch {
            editorialRepo.updateBookmark(current.id, current.bookmarkParagraph, note)
            _uiState.value = _uiState.value.copy(
                editorial = current.copy(personalNote = note),
                isEditingNote = false
            )
        }
    }

    fun markCompleted() {
        val current = _uiState.value.editorial ?: return
        viewModelScope.launch {
            editorialRepo.setCompleted(current.id, true)
            progressRepo.markEditorialRead(current.date, true)
            _uiState.value = _uiState.value.copy(editorial = current.copy(isCompleted = true))
        }
    }

    fun saveScrollPosition(position: Int) {
        val current = _uiState.value.editorial ?: return
        viewModelScope.launch {
            editorialRepo.updateReadingPosition(current.id, position)
        }
    }

    fun adjustFontSize(delta: Float) {
        val current = _uiState.value.readerSettings
        val newSize = (current.fontSizeSp + delta).coerceIn(14f, 26f)
        val updated = current.copy(fontSizeSp = newSize)
        _uiState.value = _uiState.value.copy(readerSettings = updated)
        viewModelScope.launch {
            userPrefsRepo.setReaderFontSize(newSize)
        }
    }

    fun toggleWordFavorite(wordId: String, isFav: Boolean) {
        viewModelScope.launch {
            vocabRepo.toggleFavorite(wordId, isFav)
            val sheetWord = _uiState.value.selectedWordForSheet
            if (sheetWord?.id == wordId) {
                _uiState.value = _uiState.value.copy(
                    selectedWordForSheet = sheetWord.copy(isFavorite = isFav)
                )
            }
        }
    }

    fun saveWordNote(wordId: String, note: String) {
        viewModelScope.launch {
            vocabRepo.updateNote(wordId, note)
            val sheetWord = _uiState.value.selectedWordForSheet
            if (sheetWord?.id == wordId) {
                _uiState.value = _uiState.value.copy(
                    selectedWordForSheet = sheetWord.copy(personalNote = note)
                )
            }
        }
    }
}
