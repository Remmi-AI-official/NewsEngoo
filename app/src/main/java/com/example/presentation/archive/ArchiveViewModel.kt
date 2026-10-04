package com.example.presentation.archive

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.EditorialApplication
import com.example.data.local.entity.DailyProgressEntity
import com.example.data.local.entity.EditorialEntity
import com.example.data.local.entity.GrammarRuleEntity
import com.example.data.local.entity.PhraseEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.TestEntity
import com.example.data.local.entity.VocabularyEntity
import com.example.domain.model.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DatePackageSummary(
    val date: String,
    val editorial: EditorialEntity? = null,
    val wordsCount: Int = 0,
    val grammarCount: Int = 0,
    val phrasesCount: Int = 0,
    val questionsCount: Int = 0,
    val testsCount: Int = 0,
    val isCompleted: Boolean = false,
    val isFavorite: Boolean = false
)

data class ArchiveUiState(
    val selectedDate: String = DateUtils.getTodayDate(),
    val currentMonthCalendar: Calendar = Calendar.getInstance(),
    val availableDatesWithContent: Set<String> = emptySet(),
    val selectedDateSummary: DatePackageSummary? = null,
    val allEditorials: List<EditorialEntity> = emptyList(),
    val isCalendarView: Boolean = true,
    val filterFavoriteOnly: Boolean = false,
    val filterCompletedOnly: Boolean = false,
    val isLoading: Boolean = false
)

class ArchiveViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorialApplication
    private val editorialRepo = app.editorialRepository
    private val vocabRepo = app.vocabularyRepository
    private val grammarRepo = app.grammarRepository
    private val phraseRepo = app.phraseRepository
    private val testRepo = app.practiceTestRepository
    private val progressRepo = app.progressRepository

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        loadMonthDates()
        selectDate(DateUtils.getTodayDate())
        loadAllEditorials()
    }

    private fun loadAllEditorials() {
        viewModelScope.launch {
            editorialRepo.getAllEditorials().collect { list ->
                _uiState.value = _uiState.value.copy(
                    allEditorials = list,
                    availableDatesWithContent = list.map { it.date }.toSet()
                )
            }
        }
    }

    fun selectDate(date: String) {
        viewModelScope.launch {
            val ed = editorialRepo.getEditorialByDate(date).first()
            val words = vocabRepo.getWordsForDate(date).first()
            val grammar = grammarRepo.getRulesForDate(date).first()
            val phrases = phraseRepo.getPhrasesForDate(date).first()
            val questions = testRepo.getQuestionsForDate(date).first()
            val tests = testRepo.getTestsForDate(date).first()
            val progress = progressRepo.getDailyProgress(date).first()

            val summary = DatePackageSummary(
                date = date,
                editorial = ed,
                wordsCount = words.size,
                grammarCount = grammar.size,
                phrasesCount = phrases.size,
                questionsCount = questions.size,
                testsCount = tests.size,
                isCompleted = progress?.isDayComplete == true || ed?.isCompleted == true,
                isFavorite = ed?.isFavorite == true
            )

            _uiState.value = _uiState.value.copy(
                selectedDate = date,
                selectedDateSummary = summary
            )
        }
    }

    fun changeMonth(amount: Int) {
        val newCal = _uiState.value.currentMonthCalendar.clone() as Calendar
        newCal.add(Calendar.MONTH, amount)
        _uiState.value = _uiState.value.copy(currentMonthCalendar = newCal)
    }

    fun jumpToToday() {
        val today = DateUtils.getTodayDate()
        _uiState.value = _uiState.value.copy(currentMonthCalendar = Calendar.getInstance())
        selectDate(today)
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(isCalendarView = !_uiState.value.isCalendarView)
    }

    fun toggleFavoriteFilter() {
        _uiState.value = _uiState.value.copy(filterFavoriteOnly = !_uiState.value.filterFavoriteOnly)
    }

    fun toggleCompletedFilter() {
        _uiState.value = _uiState.value.copy(filterCompletedOnly = !_uiState.value.filterCompletedOnly)
    }

    fun deleteEditorial(editorialId: String) {
        viewModelScope.launch {
            editorialRepo.deleteEditorial(editorialId)
            selectDate(_uiState.value.selectedDate)
        }
    }

    private fun loadMonthDates() {
        // Keeps availableDates in sync
    }
}
