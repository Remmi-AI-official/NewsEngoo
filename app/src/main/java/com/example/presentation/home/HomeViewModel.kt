package com.example.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.EditorialApplication
import com.example.data.local.entity.DailyProgressEntity
import com.example.data.local.entity.EditorialEntity
import com.example.data.local.entity.VocabularyEntity
import com.example.data.repository.OverallLearningStats
import com.example.domain.model.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val currentDate: String = DateUtils.getTodayDate(),
    val formattedDate: String = DateUtils.formatDate(DateUtils.getTodayDate()),
    val todayEditorial: EditorialEntity? = null,
    val todayProgress: DailyProgressEntity? = null,
    val todayWordsCount: Int = 0,
    val todayGrammarCount: Int = 0,
    val todayPhrasesCount: Int = 0,
    val todayQuestionsCount: Int = 0,
    val revisionDueWords: List<VocabularyEntity> = emptyList(),
    val overallStats: OverallLearningStats? = null,
    val isLoading: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorialApplication
    private val editorialRepo = app.editorialRepository
    private val vocabRepo = app.vocabularyRepository
    private val grammarRepo = app.grammarRepository
    private val phraseRepo = app.phraseRepository
    private val testRepo = app.practiceTestRepository
    private val progressRepo = app.progressRepository

    val todayDate = DateUtils.getTodayDate()

    val uiState: StateFlow<HomeUiState> = combine(
        editorialRepo.getEditorialByDate(todayDate),
        progressRepo.getDailyProgress(todayDate),
        vocabRepo.getWordsForDate(todayDate),
        grammarRepo.getRulesForDate(todayDate),
        phraseRepo.getPhrasesForDate(todayDate),
        testRepo.getQuestionsForDate(todayDate),
        vocabRepo.getWordsDueForRevision(todayDate),
        progressRepo.getOverallStats(todayDate)
    ) { params ->
        val editorial = params[0] as EditorialEntity?
        val progress = params[1] as DailyProgressEntity?
        val words = params[2] as List<VocabularyEntity>
        val rules = params[3] as List<Any>
        val phrases = params[4] as List<Any>
        val questions = params[5] as List<Any>
        val revisionWords = params[6] as List<VocabularyEntity>
        val stats = params[7] as OverallLearningStats

        HomeUiState(
            currentDate = todayDate,
            formattedDate = DateUtils.formatDate(todayDate),
            todayEditorial = editorial,
            todayProgress = progress,
            todayWordsCount = words.size,
            todayGrammarCount = rules.size,
            todayPhrasesCount = phrases.size,
            todayQuestionsCount = questions.size,
            revisionDueWords = revisionWords,
            overallStats = stats,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState(isLoading = true)
    )

    fun toggleEditorialFavorite(id: String, isFav: Boolean) {
        viewModelScope.launch {
            editorialRepo.toggleFavorite(id, isFav)
        }
    }
}
