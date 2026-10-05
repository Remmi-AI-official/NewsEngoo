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
import org.json.JSONArray
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
    val practiceAccuracy: Float = 0f,
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

    private fun parseAnswersJson(jsonStr: String): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val qId = obj.optString("questionId")
                val sel = obj.optInt("selected", -1)
                if (qId.isNotBlank() && sel >= 0) {
                    result[qId] = sel
                }
            }
        } catch (_: Exception) {}
        return result
    }

    fun loadDay(date: String) {
        viewModelScope.launch {
            val editorial = editorialRepo.getEditorialByDate(date).first()
            val words = vocabRepo.getWordsForDate(date).first()
            val rules = grammarRepo.getRulesForDate(date).first()
            val phrases = phraseRepo.getPhrasesForDate(date).first()
            var questions = testRepo.getQuestionsForDate(date).first()
            if (questions.isEmpty()) {
                val allQ = testRepo.getAllQuestions().first()
                if (allQ.isNotEmpty()) {
                    questions = allQ.take(15)
                }
            }
            val progress = progressRepo.getDailyProgress(date).first()

            // Fetch previous saved practice attempt from database
            val latestPracticeAttempt = testRepo.getLatestAttemptForTestSync("practice_$date")
            val isSubmitted = latestPracticeAttempt != null
            val savedAnswers = if (latestPracticeAttempt != null) {
                parseAnswersJson(latestPracticeAttempt.answersJson)
            } else {
                emptyMap()
            }
            val score = latestPracticeAttempt?.score ?: 0
            val accuracy = latestPracticeAttempt?.accuracy ?: 0f

            _uiState.value = DailyLearnUiState(
                date = date,
                editorial = editorial,
                vocabulary = words,
                grammarRules = rules,
                phrases = phrases,
                questions = questions,
                practiceAnswers = savedAnswers,
                practiceSubmitted = isSubmitted,
                practiceScore = score,
                practiceAccuracy = accuracy,
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
        val questions = state.questions
        if (questions.isEmpty()) return

        viewModelScope.launch {
            val eval = testRepo.submitPracticeAttempt(
                date = state.date,
                questions = questions,
                userAnswers = state.practiceAnswers
            )
            progressRepo.updatePracticeProgress(state.date, questions.size, questions.size)

            _uiState.value = _uiState.value.copy(
                practiceSubmitted = true,
                practiceScore = eval.score,
                practiceAccuracy = eval.accuracy
            )
        }
    }

    fun reattemptPractice() {
        _uiState.value = _uiState.value.copy(
            practiceSubmitted = false,
            practiceAnswers = emptyMap(),
            practiceScore = 0,
            practiceAccuracy = 0f
        )
    }

    fun markWordReviewed(wordId: String, remembered: Boolean) {
        viewModelScope.launch {
            vocabRepo.recordReview(wordId, remembered, _uiState.value.date)
        }
    }

    // Editorial Operations
    fun updateEditorial(title: String, source: String, content: String) {
        val current = _uiState.value.editorial ?: return
        viewModelScope.launch {
            val updated = current.copy(
                title = title.trim(),
                source = source.trim(),
                contentMarkdown = content.trim()
            )
            editorialRepo.updateEditorial(updated)
            _uiState.value = _uiState.value.copy(editorial = updated)
        }
    }

    fun deleteEditorial() {
        val current = _uiState.value.editorial ?: return
        viewModelScope.launch {
            editorialRepo.deleteEditorial(current.id)
            _uiState.value = _uiState.value.copy(editorial = null)
        }
    }

    // Vocabulary Operations
    fun updateWord(word: VocabularyEntity) {
        viewModelScope.launch {
            vocabRepo.updateWord(word)
            loadDay(_uiState.value.date)
        }
    }

    fun deleteWord(wordId: String) {
        viewModelScope.launch {
            vocabRepo.deleteWord(wordId)
            loadDay(_uiState.value.date)
        }
    }

    // Grammar Operations
    fun updateGrammarRule(rule: GrammarRuleEntity) {
        viewModelScope.launch {
            grammarRepo.updateRule(rule)
            loadDay(_uiState.value.date)
        }
    }

    fun deleteGrammarRule(ruleId: String) {
        viewModelScope.launch {
            grammarRepo.deleteRule(ruleId)
            loadDay(_uiState.value.date)
        }
    }

    // Phrase Operations
    fun updatePhrase(phrase: PhraseEntity) {
        viewModelScope.launch {
            phraseRepo.updatePhrase(phrase)
            loadDay(_uiState.value.date)
        }
    }

    fun deletePhrase(phraseId: String) {
        viewModelScope.launch {
            phraseRepo.deletePhrase(phraseId)
            loadDay(_uiState.value.date)
        }
    }

    // Question Operations
    fun updateQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            testRepo.updateQuestion(question)
            loadDay(_uiState.value.date)
        }
    }

    fun deleteQuestion(questionId: String) {
        viewModelScope.launch {
            testRepo.deleteQuestion(questionId)
            loadDay(_uiState.value.date)
        }
    }
}
