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
        editorialRepo.getAllEditorials(),
        progressRepo.getDailyProgress(todayDate),
        vocabRepo.getWordsForDate(todayDate),
        vocabRepo.getAllWords(),
        grammarRepo.getRulesForDate(todayDate),
        phraseRepo.getPhrasesForDate(todayDate),
        testRepo.getQuestionsForDate(todayDate),
        testRepo.getAllQuestions(),
        vocabRepo.getWordsDueForRevision(todayDate),
        progressRepo.getOverallStats(todayDate)
    ) { params ->
        val dateEditorial = params[0] as EditorialEntity?
        val allEditorials = params[1] as List<EditorialEntity>
        val progress = params[2] as DailyProgressEntity?
        val dateWords = params[3] as List<VocabularyEntity>
        val allWords = params[4] as List<VocabularyEntity>
        val rules = params[5] as List<Any>
        val phrases = params[6] as List<Any>
        val dateQuestions = params[7] as List<Any>
        val allQuestions = params[8] as List<Any>
        val revisionWords = params[9] as List<VocabularyEntity>
        val stats = params[10] as OverallLearningStats

        val editorial = dateEditorial ?: allEditorials.firstOrNull()
        val wordsCount = if (dateWords.isNotEmpty()) dateWords.size else allWords.take(18).size
        val questionsCount = if (dateQuestions.isNotEmpty()) dateQuestions.size else allQuestions.take(15).size

        HomeUiState(
            currentDate = todayDate,
            formattedDate = DateUtils.formatDate(todayDate),
            todayEditorial = editorial,
            todayProgress = progress,
            todayWordsCount = wordsCount,
            todayGrammarCount = if (rules.isNotEmpty()) rules.size else 4,
            todayPhrasesCount = if (phrases.isNotEmpty()) phrases.size else 7,
            todayQuestionsCount = questionsCount,
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

    fun updateEditorial(id: String, title: String, source: String, content: String) {
        viewModelScope.launch {
            val current = editorialRepo.getEditorialByIdSync(id) ?: return@launch
            editorialRepo.updateEditorial(
                current.copy(
                    title = title.trim(),
                    source = source.trim(),
                    contentMarkdown = content.trim()
                )
            )
        }
    }

    fun deleteEditorial(id: String) {
        viewModelScope.launch {
            editorialRepo.deleteEditorial(id)
        }
    }
}
