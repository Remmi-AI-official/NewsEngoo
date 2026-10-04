package com.example.presentation.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.EditorialApplication
import com.example.data.local.entity.EditorialEntity
import com.example.data.local.entity.GrammarRuleEntity
import com.example.data.local.entity.PhraseEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FavoriteTab(val label: String) {
    ALL("All"),
    ARTICLES("Articles"),
    WORDS("Words"),
    GRAMMAR("Grammar"),
    PHRASES("Phrases"),
    QUESTIONS("Questions")
}

data class FavoritesUiState(
    val currentTab: FavoriteTab = FavoriteTab.ALL,
    val favoriteEditorials: List<EditorialEntity> = emptyList(),
    val favoriteWords: List<VocabularyEntity> = emptyList(),
    val favoriteRules: List<GrammarRuleEntity> = emptyList(),
    val favoritePhrases: List<PhraseEntity> = emptyList(),
    val favoriteQuestions: List<QuestionEntity> = emptyList(),
    val isLoading: Boolean = false
)

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorialApplication
    private val editorialRepo = app.editorialRepository
    private val vocabRepo = app.vocabularyRepository
    private val grammarRepo = app.grammarRepository
    private val phraseRepo = app.phraseRepository
    private val testRepo = app.practiceTestRepository

    private val _currentTab = MutableStateFlow(FavoriteTab.ALL)

    val uiState: StateFlow<FavoritesUiState> = combine(
        _currentTab,
        editorialRepo.getFavoriteEditorials(),
        vocabRepo.getFavoriteWords(),
        grammarRepo.getFavoriteRules(),
        phraseRepo.getFavoritePhrases(),
        testRepo.getFavoriteQuestions()
    ) { params ->
        val tab = params[0] as FavoriteTab
        val eds = params[1] as List<EditorialEntity>
        val words = params[2] as List<VocabularyEntity>
        val rules = params[3] as List<GrammarRuleEntity>
        val phrases = params[4] as List<PhraseEntity>
        val questions = params[5] as List<QuestionEntity>

        FavoritesUiState(
            currentTab = tab,
            favoriteEditorials = eds,
            favoriteWords = words,
            favoriteRules = rules,
            favoritePhrases = phrases,
            favoriteQuestions = questions,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoritesUiState(isLoading = true))

    fun setTab(tab: FavoriteTab) {
        _currentTab.value = tab
    }

    fun removeFavoriteEditorial(id: String) {
        viewModelScope.launch { editorialRepo.toggleFavorite(id, false) }
    }

    fun removeFavoriteWord(id: String) {
        viewModelScope.launch { vocabRepo.toggleFavorite(id, false) }
    }

    fun removeFavoriteRule(id: String) {
        viewModelScope.launch { grammarRepo.toggleFavorite(id, false) }
    }

    fun removeFavoritePhrase(id: String) {
        viewModelScope.launch { phraseRepo.toggleFavorite(id, false) }
    }
}
