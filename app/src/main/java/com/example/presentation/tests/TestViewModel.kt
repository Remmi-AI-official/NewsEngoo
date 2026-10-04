package com.example.presentation.tests

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.EditorialApplication
import com.example.data.local.entity.MistakeEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.TestAttemptEntity
import com.example.data.local.entity.TestEntity
import com.example.data.repository.TestEvaluationResult
import com.example.domain.model.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TestsListUiState(
    val tests: List<TestEntity> = emptyList(),
    val attempts: List<TestAttemptEntity> = emptyList(),
    val mistakes: List<MistakeEntity> = emptyList(),
    val isGeneratingWeekly: Boolean = false,
    val isLoading: Boolean = true
)

data class ActiveTestUiState(
    val test: TestEntity? = null,
    val questions: List<QuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val userAnswers: Map<String, Int> = emptyMap(),
    val remainingSeconds: Int = 900,
    val isSubmitted: Boolean = false,
    val result: TestEvaluationResult? = null
)

class TestViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorialApplication
    private val testRepo = app.practiceTestRepository
    private val progressRepo = app.progressRepository
    val todayDate = DateUtils.getTodayDate()

    val testsListState: StateFlow<TestsListUiState> = kotlinx.coroutines.flow.combine(
        testRepo.getAllTests(),
        testRepo.getAllAttempts(),
        testRepo.getAllMistakes()
    ) { tests, attempts, mistakes ->
        TestsListUiState(
            tests = tests,
            attempts = attempts,
            mistakes = mistakes,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TestsListUiState(isLoading = true))

    private val _activeTestState = MutableStateFlow(ActiveTestUiState())
    val activeTestState: StateFlow<ActiveTestUiState> = _activeTestState.asStateFlow()

    private var startedAt: Long = 0L

    fun loadTest(testId: String) {
        viewModelScope.launch {
            val test = testRepo.getTestByIdSync(testId)
            val questions = testRepo.getQuestionsForTestSync(testId)
            val durationSec = (test?.durationMinutes ?: 15) * 60
            startedAt = System.currentTimeMillis()

            _activeTestState.value = ActiveTestUiState(
                test = test,
                questions = questions,
                currentQuestionIndex = 0,
                userAnswers = emptyMap(),
                remainingSeconds = durationSec,
                isSubmitted = false,
                result = null
            )
        }
    }

    fun selectAnswer(questionId: String, optionIndex: Int) {
        val curr = _activeTestState.value.userAnswers.toMutableMap()
        curr[questionId] = optionIndex
        _activeTestState.value = _activeTestState.value.copy(userAnswers = curr)
    }

    fun navigateToQuestion(index: Int) {
        if (index in 0 until _activeTestState.value.questions.size) {
            _activeTestState.value = _activeTestState.value.copy(currentQuestionIndex = index)
        }
    }

    fun submitActiveTest() {
        val state = _activeTestState.value
        val test = state.test ?: return
        viewModelScope.launch {
            val timeSpent = (System.currentTimeMillis() - startedAt).toInt() / 1000
            val eval = testRepo.submitTest(
                testId = test.id,
                testTitle = test.title,
                date = test.date,
                startedAt = startedAt,
                timeSpentSeconds = timeSpent,
                questions = state.questions,
                userAnswers = state.userAnswers
            )
            // Update daily progress record
            progressRepo.markTestCompleted(test.date, eval.score, eval.totalQuestions)

            _activeTestState.value = state.copy(
                isSubmitted = true,
                result = eval
            )
        }
    }

    fun generateWeeklyTest(onGenerated: (String) -> Unit) {
        viewModelScope.launch {
            val test = testRepo.generateWeeklyTest(todayDate)
            onGenerated(test.id)
        }
    }

    fun markMistakeImproved(mistakeId: String, isImproved: Boolean) {
        viewModelScope.launch {
            testRepo.markMistakeImproved(mistakeId, isImproved)
        }
    }
}
