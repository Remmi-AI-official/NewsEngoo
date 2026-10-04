package com.example.presentation.manager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.EditorialApplication
import com.example.data.local.entity.EditorialEntity
import com.example.data.local.entity.GrammarRuleEntity
import com.example.data.local.entity.ImportHistoryEntity
import com.example.data.local.entity.PhraseEntity
import com.example.data.local.entity.TestEntity
import com.example.data.local.entity.VocabularyEntity
import com.example.domain.model.ContentParser
import com.example.domain.model.DailyPackageImport
import com.example.domain.model.DateUtils
import com.example.domain.model.GrammarImportItem
import com.example.domain.model.ImportMode
import com.example.domain.model.PhraseImportItem
import com.example.domain.model.QuestionImportItem
import com.example.domain.model.VocabularyImportItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ImportContentType(val label: String) {
    DAILY_PACKAGE("Complete Daily Package"),
    EDITORIAL("Editorial Markdown"),
    VOCABULARY("Vocabulary JSON"),
    GRAMMAR("Grammar JSON"),
    PHRASES("Phrases JSON"),
    QUESTIONS("Questions JSON")
}

data class ImportValidationPreview(
    val isValid: Boolean,
    val type: ImportContentType,
    val date: String,
    val totalCount: Int,
    val newCount: Int,
    val duplicateCount: Int,
    val sampleSummary: String,
    val errorMessage: String? = null
)

data class ContentManagerUiState(
    val selectedType: ImportContentType = ImportContentType.DAILY_PACKAGE,
    val rawInput: String = "",
    val targetDate: String = DateUtils.getTodayDate(),
    val editorialTitle: String = "",
    val editorialSource: String = "The Daily Editorial",
    val selectedImportMode: ImportMode = ImportMode.MERGE_DUPLICATES,
    val validationPreview: ImportValidationPreview? = null,
    val importHistory: List<ImportHistoryEntity> = emptyList(),
    val isProcessing: Boolean = false,
    val lastSuccessMessage: String? = null,
    val lastErrorMessage: String? = null,
    val exportedJsonString: String? = null
)

class ContentManagerViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorialApplication
    private val importExportRepo = app.contentImportExportRepository
    private val vocabRepo = app.vocabularyRepository

    private val _uiState = MutableStateFlow(ContentManagerUiState())
    val uiState: StateFlow<ContentManagerUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            importExportRepo.getImportHistory().collect { history ->
                _uiState.value = _uiState.value.copy(importHistory = history)
            }
        }
    }

    fun setContentType(type: ImportContentType) {
        _uiState.value = _uiState.value.copy(
            selectedType = type,
            validationPreview = null,
            lastSuccessMessage = null,
            lastErrorMessage = null
        )
    }

    fun setRawInput(input: String) {
        _uiState.value = _uiState.value.copy(rawInput = input, validationPreview = null)
    }

    fun setTargetDate(date: String) {
        _uiState.value = _uiState.value.copy(targetDate = date)
    }

    fun setEditorialTitle(title: String) {
        _uiState.value = _uiState.value.copy(editorialTitle = title)
    }

    fun setEditorialSource(source: String) {
        _uiState.value = _uiState.value.copy(editorialSource = source)
    }

    fun setImportMode(mode: ImportMode) {
        _uiState.value = _uiState.value.copy(selectedImportMode = mode)
    }

    /**
     * Pre-import validation and duplicate analysis
     */
    fun validateInput() {
        val state = _uiState.value
        val input = state.rawInput.trim()
        if (input.isEmpty()) {
            _uiState.value = state.copy(lastErrorMessage = "Please paste or enter content to validate.")
            return
        }

        viewModelScope.launch {
            try {
                when (state.selectedType) {
                    ImportContentType.DAILY_PACKAGE -> {
                        val parsed = ContentParser.parseDailyPackageJson(input)
                        if (!parsed.isValid || parsed.items.isEmpty()) {
                            _uiState.value = state.copy(
                                validationPreview = ImportValidationPreview(
                                    isValid = false,
                                    type = state.selectedType,
                                    date = state.targetDate,
                                    totalCount = 0,
                                    newCount = 0,
                                    duplicateCount = 0,
                                    sampleSummary = "",
                                    errorMessage = parsed.errorMessage ?: "Malformed package JSON"
                                )
                            )
                        } else {
                            val pkg = parsed.items.first()
                            var dupCount = 0
                            pkg.vocabulary.forEach { v ->
                                if (vocabRepo.getWordByIdSync(v.word.lowercase().trim()) != null) dupCount++
                            }
                            val summaryStr = "Editorial: ${if (pkg.editorial != null) "Yes (\"${pkg.editorial.title}\")" else "No"} • Words: ${pkg.vocabulary.size} • Grammar: ${pkg.grammar.size} • Phrases: ${pkg.phrases.size} • Questions: ${pkg.questions.size}"

                            _uiState.value = state.copy(
                                validationPreview = ImportValidationPreview(
                                    isValid = true,
                                    type = state.selectedType,
                                    date = pkg.date,
                                    totalCount = (if (pkg.editorial != null) 1 else 0) + pkg.vocabulary.size + pkg.grammar.size + pkg.phrases.size + pkg.questions.size,
                                    newCount = pkg.vocabulary.size - dupCount,
                                    duplicateCount = dupCount,
                                    sampleSummary = summaryStr
                                ),
                                targetDate = pkg.date
                            )
                        }
                    }
                    ImportContentType.VOCABULARY -> {
                        val parsed = ContentParser.parseVocabularyJson(input)
                        if (!parsed.isValid) {
                            _uiState.value = state.copy(
                                validationPreview = ImportValidationPreview(
                                    isValid = false,
                                    type = state.selectedType,
                                    date = state.targetDate,
                                    totalCount = 0,
                                    newCount = 0,
                                    duplicateCount = 0,
                                    sampleSummary = "",
                                    errorMessage = parsed.errorMessage
                                )
                            )
                        } else {
                            var dupCount = 0
                            parsed.items.forEach { v ->
                                if (vocabRepo.getWordByIdSync(v.word.lowercase().trim()) != null) dupCount++
                            }
                            _uiState.value = state.copy(
                                validationPreview = ImportValidationPreview(
                                    isValid = true,
                                    type = state.selectedType,
                                    date = parsed.date,
                                    totalCount = parsed.items.size,
                                    newCount = parsed.items.size - dupCount,
                                    duplicateCount = dupCount,
                                    sampleSummary = "Sample words: ${parsed.items.take(4).joinToString { it.word }}"
                                ),
                                targetDate = parsed.date
                            )
                        }
                    }
                    ImportContentType.EDITORIAL -> {
                        if (state.editorialTitle.isBlank() && !input.startsWith("#")) {
                            _uiState.value = state.copy(lastErrorMessage = "Please specify an Editorial Title.")
                            return@launch
                        }
                        val title = state.editorialTitle.ifBlank {
                            input.lines().firstOrNull { it.startsWith("#") }?.removePrefix("#")?.trim() ?: "Editorial"
                        }
                        _uiState.value = state.copy(
                            validationPreview = ImportValidationPreview(
                                isValid = true,
                                type = state.selectedType,
                                date = state.targetDate,
                                totalCount = 1,
                                newCount = 1,
                                duplicateCount = 0,
                                sampleSummary = "Title: \"$title\" (${input.split("\\s+".toRegex()).size} words)"
                            )
                        )
                    }
                    ImportContentType.GRAMMAR -> {
                        val parsed = ContentParser.parseGrammarJson(input)
                        if (!parsed.isValid) {
                            _uiState.value = state.copy(
                                validationPreview = ImportValidationPreview(
                                    isValid = false,
                                    type = state.selectedType,
                                    date = state.targetDate,
                                    totalCount = 0,
                                    newCount = 0,
                                    duplicateCount = 0,
                                    sampleSummary = "",
                                    errorMessage = parsed.errorMessage
                                )
                            )
                        } else {
                            _uiState.value = state.copy(
                                validationPreview = ImportValidationPreview(
                                    isValid = true,
                                    type = state.selectedType,
                                    date = parsed.date,
                                    totalCount = parsed.items.size,
                                    newCount = parsed.items.size,
                                    duplicateCount = 0,
                                    sampleSummary = "Rules: ${parsed.items.joinToString { it.title }}"
                                ),
                                targetDate = parsed.date
                            )
                        }
                    }
                    ImportContentType.PHRASES -> {
                        val parsed = ContentParser.parsePhrasesJson(input)
                        if (!parsed.isValid) {
                            _uiState.value = state.copy(
                                validationPreview = ImportValidationPreview(
                                    isValid = false,
                                    type = state.selectedType,
                                    date = state.targetDate,
                                    totalCount = 0,
                                    newCount = 0,
                                    duplicateCount = 0,
                                    sampleSummary = "",
                                    errorMessage = parsed.errorMessage
                                )
                            )
                        } else {
                            _uiState.value = state.copy(
                                validationPreview = ImportValidationPreview(
                                    isValid = true,
                                    type = state.selectedType,
                                    date = parsed.date,
                                    totalCount = parsed.items.size,
                                    newCount = parsed.items.size,
                                    duplicateCount = 0,
                                    sampleSummary = "Phrases: ${parsed.items.take(4).joinToString { it.phrase }}"
                                ),
                                targetDate = parsed.date
                            )
                        }
                    }
                    ImportContentType.QUESTIONS -> {
                        val parsed = ContentParser.parseQuestionsJson(input)
                        if (!parsed.isValid) {
                            _uiState.value = state.copy(
                                validationPreview = ImportValidationPreview(
                                    isValid = false,
                                    type = state.selectedType,
                                    date = state.targetDate,
                                    totalCount = 0,
                                    newCount = 0,
                                    duplicateCount = 0,
                                    sampleSummary = "",
                                    errorMessage = parsed.errorMessage
                                )
                            )
                        } else {
                            _uiState.value = state.copy(
                                validationPreview = ImportValidationPreview(
                                    isValid = true,
                                    type = state.selectedType,
                                    date = parsed.date,
                                    totalCount = parsed.items.size,
                                    newCount = parsed.items.size,
                                    duplicateCount = 0,
                                    sampleSummary = "${parsed.items.size} practice questions verified"
                                ),
                                targetDate = parsed.date
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(lastErrorMessage = "Validation error: ${e.message}")
            }
        }
    }

    /**
     * Executes validated import
     */
    fun executeImport() {
        val state = _uiState.value
        val input = state.rawInput.trim()
        val date = state.targetDate

        viewModelScope.launch {
            _uiState.value = state.copy(isProcessing = true, lastErrorMessage = null)
            try {
                when (state.selectedType) {
                    ImportContentType.DAILY_PACKAGE -> {
                        val parsed = ContentParser.parseDailyPackageJson(input)
                        if (parsed.isValid && parsed.items.isNotEmpty()) {
                            val report = importExportRepo.importDailyPackage(parsed.items.first(), state.selectedImportMode)
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                lastSuccessMessage = "Successfully imported package for ${report.date}: ${report.vocabReport.totalProcessed} words (${report.vocabReport.newCount} new, ${report.vocabReport.mergedCount} merged), ${report.grammarCount} rules, ${report.phrasesCount} phrases, ${report.questionsCount} questions.",
                                rawInput = "",
                                validationPreview = null
                            )
                        }
                    }
                    ImportContentType.VOCABULARY -> {
                        val parsed = ContentParser.parseVocabularyJson(input)
                        if (parsed.isValid) {
                            val report = vocabRepo.importWords(parsed.items, date, "ed_$date", state.selectedImportMode)
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                lastSuccessMessage = "Imported ${report.totalProcessed} vocabulary items (${report.newCount} new, ${report.mergedCount} merged, ${report.skippedCount} skipped).",
                                rawInput = "",
                                validationPreview = null
                            )
                        }
                    }
                    ImportContentType.EDITORIAL -> {
                        val title = state.editorialTitle.ifBlank {
                            input.lines().firstOrNull { it.startsWith("#") }?.removePrefix("#")?.trim() ?: "Editorial for $date"
                        }
                        importExportRepo.importEditorial(
                            title = title,
                            contentMarkdown = input,
                            date = date,
                            source = state.editorialSource
                        )
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            lastSuccessMessage = "Editorial \"$title\" successfully saved for $date.",
                            rawInput = "",
                            validationPreview = null
                        )
                    }
                    ImportContentType.GRAMMAR -> {
                        val parsed = ContentParser.parseGrammarJson(input)
                        if (parsed.isValid) {
                            val count = app.grammarRepository.importRules(parsed.items, date)
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                lastSuccessMessage = "Successfully imported $count grammar rules.",
                                rawInput = "",
                                validationPreview = null
                            )
                        }
                    }
                    ImportContentType.PHRASES -> {
                        val parsed = ContentParser.parsePhrasesJson(input)
                        if (parsed.isValid) {
                            val count = app.phraseRepository.importPhrases(parsed.items, date)
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                lastSuccessMessage = "Successfully imported $count phrases / idioms.",
                                rawInput = "",
                                validationPreview = null
                            )
                        }
                    }
                    ImportContentType.QUESTIONS -> {
                        val parsed = ContentParser.parseQuestionsJson(input)
                        if (parsed.isValid) {
                            val count = app.practiceTestRepository.importQuestions(parsed.items, date, "test_$date")
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                lastSuccessMessage = "Successfully imported $count questions.",
                                rawInput = "",
                                validationPreview = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    lastErrorMessage = "Import failed: ${e.message}"
                )
            }
        }
    }

    fun exportFullBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            val json = importExportRepo.exportAllDataJson()
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                exportedJsonString = json,
                lastSuccessMessage = "Full backup JSON generated (${json.length} characters)."
            )
        }
    }

    fun restoreBackup(backupJson: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            try {
                val count = importExportRepo.restoreBackupJson(backupJson)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    lastSuccessMessage = "Successfully restored $count items from backup."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    lastErrorMessage = "Restore failed: ${e.message}"
                )
            }
        }
    }

    val allEditorials: StateFlow<List<EditorialEntity>> = app.editorialRepository.getAllEditorials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVocabWords: StateFlow<List<VocabularyEntity>> = app.vocabularyRepository.getAllWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGrammarRules: StateFlow<List<GrammarRuleEntity>> = app.grammarRepository.getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPhrases: StateFlow<List<PhraseEntity>> = app.phraseRepository.getAllPhrases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTests: StateFlow<List<TestEntity>> = app.practiceTestRepository.getAllTests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteEditorial(id: String) {
        viewModelScope.launch {
            app.editorialRepository.deleteEditorial(id)
            _uiState.value = _uiState.value.copy(lastSuccessMessage = "Editorial deleted successfully.")
        }
    }

    fun deleteWord(id: String) {
        viewModelScope.launch {
            app.vocabularyRepository.deleteWord(id)
            _uiState.value = _uiState.value.copy(lastSuccessMessage = "Word deleted successfully.")
        }
    }

    fun deleteGrammarRule(id: String) {
        viewModelScope.launch {
            app.grammarRepository.deleteRule(id)
            _uiState.value = _uiState.value.copy(lastSuccessMessage = "Grammar rule deleted successfully.")
        }
    }

    fun deletePhrase(id: String) {
        viewModelScope.launch {
            app.phraseRepository.deletePhrase(id)
            _uiState.value = _uiState.value.copy(lastSuccessMessage = "Phrase deleted successfully.")
        }
    }

    fun deleteTest(testId: String) {
        viewModelScope.launch {
            app.practiceTestRepository.deleteTest(testId)
            _uiState.value = _uiState.value.copy(lastSuccessMessage = "Test deleted successfully.")
        }
    }

    fun deleteContentByDate(date: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            app.editorialRepository.deleteEditorialByDate(date)
            app.vocabularyRepository.deleteWordsForDate(date)
            app.grammarRepository.deleteRulesForDate(date)
            app.phraseRepository.deletePhrasesForDate(date)
            app.practiceTestRepository.deleteTestsForDate(date)
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                lastSuccessMessage = "All content for date $date has been deleted."
            )
        }
    }

    fun clearAllEditorials() {
        viewModelScope.launch {
            app.editorialRepository.deleteAllEditorials()
            _uiState.value = _uiState.value.copy(lastSuccessMessage = "All editorials cleared.")
        }
    }

    fun clearAllVocabulary() {
        viewModelScope.launch {
            app.vocabularyRepository.deleteAllWords()
            _uiState.value = _uiState.value.copy(lastSuccessMessage = "All vocabulary words cleared.")
        }
    }

    fun clearAllGrammar() {
        viewModelScope.launch {
            app.grammarRepository.deleteAllRules()
            _uiState.value = _uiState.value.copy(lastSuccessMessage = "All grammar rules cleared.")
        }
    }

    fun clearAllPhrases() {
        viewModelScope.launch {
            app.phraseRepository.deleteAllPhrases()
            _uiState.value = _uiState.value.copy(lastSuccessMessage = "All phrases cleared.")
        }
    }

    fun clearAllTests() {
        viewModelScope.launch {
            app.practiceTestRepository.deleteAllTests()
            app.practiceTestRepository.deleteAllQuestions()
            _uiState.value = _uiState.value.copy(lastSuccessMessage = "All tests and questions cleared.")
        }
    }
}
