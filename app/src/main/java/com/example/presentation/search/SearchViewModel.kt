package com.example.presentation.search

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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchResults(
    val query: String = "",
    val words: List<VocabularyEntity> = emptyList(),
    val editorials: List<EditorialEntity> = emptyList(),
    val grammarRules: List<GrammarRuleEntity> = emptyList(),
    val phrases: List<PhraseEntity> = emptyList(),
    val questions: List<QuestionEntity> = emptyList(),
    val isSearching: Boolean = false
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorialApplication
    private val editorialRepo = app.editorialRepository
    private val vocabRepo = app.vocabularyRepository
    private val grammarRepo = app.grammarRepository
    private val phraseRepo = app.phraseRepository
    private val testRepo = app.practiceTestRepository

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow(SearchResults())
    val results: StateFlow<SearchResults> = _results.asStateFlow()

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        val trimmed = newQuery.trim()
        if (trimmed.isEmpty()) {
            _results.value = SearchResults()
            return
        }

        viewModelScope.launch {
            _results.value = _results.value.copy(isSearching = true)
            val words = vocabRepo.searchWords(trimmed)
            val editorials = editorialRepo.searchEditorials(trimmed)
            val rules = grammarRepo.searchRules(trimmed)
            val phrases = phraseRepo.searchPhrases(trimmed)
            val questions = testRepo.searchQuestions(trimmed)

            kotlinx.coroutines.flow.combine(words, editorials, rules, phrases, questions) { w, ed, r, p, q ->
                SearchResults(
                    query = trimmed,
                    words = w,
                    editorials = ed,
                    grammarRules = r,
                    phrases = p,
                    questions = q,
                    isSearching = false
                )
            }.collect { combined ->
                _results.value = combined
            }
        }
    }
}
