package com.example.presentation.learn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.EditorialApplication
import com.example.data.local.entity.DailyProgressEntity
import com.example.data.local.entity.EditorialEntity
import com.example.data.local.entity.GrammarRuleEntity
import com.example.data.local.entity.PhraseEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.VocabularyEntity
import com.example.domain.model.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class LearnStep(val stepNumber: Int, val title: String) {
    EDITORIAL(1, "Read Editorial"),
    VOCABULARY(2, "Learn Vocabulary"),
    GRAMMAR(3, "Study Grammar"),
    EXPRESSIONS(4, "Expressions & Idioms"),
    PRACTICE(5, "Practice Questions"),
    TEST(6, "Daily Test"),
    COMPLETE(7, "Today's Review")
}

data class DailyLearnUiState(
    val date: String = DateUtils.getTodayDate(),
    val currentStep: LearnStep = LearnStep.EDITORIAL,
    val editorial: EditorialEntity? = null,
    val vocabulary: List<VocabularyEntity> = emptyList(),
    val grammarRules: List<GrammarRuleEntity> = emptyList(),
    val phrases: List<PhraseEntity> = emptyList(),
    val questions: List<QuestionEntity> = emptyList(),
    val activeVocabIndex: Int = 0,
    val activeGrammarIndex: Int = 0,
    val activePhraseIndex: Int = 0,
    val practiceAnswers: Map<String, Int> = emptyMap(),
    val practiceSubmitted: Boolean = false,
    val practiceScore: Int = 0,
    val progress: DailyProgressEntity? = null,
    val isLoading: Boolean = true
)

class DailyLearnViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorialApplication
    private val editorialRepo = app.editorialRepository
    private val vocabRepo = app.vocabularyRepository
    private val grammarRepo = app.grammarRepository
    private val phraseRepo = app.phraseRepository
    private val testRepo = app.practiceTestRepository
    private val progressRepo = app.progressRepository

    private val _uiState = MutableStateFlow(DailyLearnUiState())
    val uiState: StateFlow<DailyLearnUiState> = _uiState.asStateFlow()

    fun loadDay(date: String) {
        viewModelScope.launch {
            val editorial = editorialRepo.getEditorialByDate(date).first()
            val words = vocabRepo.getWordsForDate(date).first()
            val rules = grammarRepo.getRulesForDate(date).first()
            val phrases = phraseRepo.getPhrasesForDate(date).first()
            val questions = testRepo.getQuestionsForDate(date).first()
            val progress = progressRepo.getDailyProgress(date).first()

            _uiState.value = DailyLearnUiState(
                date = date,
                editorial = editorial,
                vocabulary = words,
                grammarRules = rules,
                phrases = phrases,
                questions = questions,
                progress = progress,
                isLoading = false
            )
        }
    }

    fun setStep(step: LearnStep) {
        _uiState.value = _uiState.value.copy(currentStep = step)
    }

    fun nextVocab() {
        val curr = _uiState.value.activeVocabIndex
        if (curr < _uiState.value.vocabulary.size - 1) {
            _uiState.value = _uiState.value.copy(activeVocabIndex = curr + 1)
        } else {
            // Completed vocab
            viewModelScope.launch {
                progressRepo.updateVocabProgress(_uiState.value.date, _uiState.value.vocabulary.size, _uiState.value.vocabulary.size)
            }
            setStep(LearnStep.GRAMMAR)
        }
    }

    fun prevVocab() {
        val curr = _uiState.value.activeVocabIndex
        if (curr > 0) {
            _uiState.value = _uiState.value.copy(activeVocabIndex = curr - 1)
        }
    }

    fun nextGrammar() {
        val curr = _uiState.value.activeGrammarIndex
        if (curr < _uiState.value.grammarRules.size - 1) {
            _uiState.value = _uiState.value.copy(activeGrammarIndex = curr + 1)
        } else {
            viewModelScope.launch {
                progressRepo.updateGrammarProgress(_uiState.value.date, _uiState.value.grammarRules.size, _uiState.value.grammarRules.size)
            }
            setStep(LearnStep.EXPRESSIONS)
        }
    }

    fun nextPhrase() {
        val curr = _uiState.value.activePhraseIndex
        if (curr < _uiState.value.phrases.size - 1) {
            _uiState.value = _uiState.value.copy(activePhraseIndex = curr + 1)
        } else {
            viewModelScope.launch {
                progressRepo.updateExpressionsProgress(_uiState.value.date, _uiState.value.phrases.size, _uiState.value.phrases.size)
            }
            setStep(LearnStep.PRACTICE)
        }
    }

    fun selectPracticeAnswer(questionId: String, answerIndex: Int) {
        val currentAnswers = _uiState.value.practiceAnswers.toMutableMap()
        currentAnswers[questionId] = answerIndex
        _uiState.value = _uiState.value.copy(practiceAnswers = currentAnswers)
    }

    fun submitPractice() {
        val state = _uiState.value
        var score = 0
        state.questions.forEach { q ->
            val ans = state.practiceAnswers[q.id]
            if (ans == q.correctAnswerIndex) score++
        }
        _uiState.value = _uiState.value.copy(practiceSubmitted = true, practiceScore = score)
    }

    fun markWordReviewed(wordId: String, remembered: Boolean) {
        viewModelScope.launch {
            vocabRepo.recordReview(wordId, remembered, _uiState.value.date)
        }
    }
}
