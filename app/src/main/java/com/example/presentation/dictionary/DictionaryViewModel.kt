package com.example.presentation.dictionary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.EditorialApplication
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.VocabularyEntity
import com.example.data.local.entity.WordCategoryCrossRef
import com.example.domain.model.DateUtils
import com.example.domain.model.WordLearningStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DictionaryFilter(val label: String) {
    ALL("All Words"),
    TODAY("Today's Words"),
    FAVORITES("Favorites"),
    REVISION_DUE("Revision Due"),
    LEARNING("Learning"),
    MASTERED("Mastered"),
    DIFFICULT("Difficult")
}

data class DictionaryUiState(
    val words: List<VocabularyEntity> = emptyList(),
    val filteredWords: List<VocabularyEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val categoryWordCounts: Map<String, Int> = emptyMap(),
    val wordCategories: Map<String, List<CategoryEntity>> = emptyMap(),
    val currentFilter: DictionaryFilter = DictionaryFilter.ALL,
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val totalWordsCount: Int = 0,
    val masteredWordsCount: Int = 0,
    val revisionDueCount: Int = 0,
    val isMultiSelectMode: Boolean = false,
    val selectedWordIds: Set<String> = emptySet(),
    val selectedTab: Int = 0, // 0 = All Words / Filtered, 1 = Folders Overview
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

class DictionaryViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorialApplication
    private val vocabRepo = app.vocabularyRepository
    val todayDate = DateUtils.getTodayDate()

    private val _searchQuery = MutableStateFlow("")
    private val _currentFilter = MutableStateFlow(DictionaryFilter.ALL)
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _isMultiSelectMode = MutableStateFlow(false)
    private val _selectedWordIds = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedTab = MutableStateFlow(0)
    private val _userMessage = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            vocabRepo.ensureDefaultCategories()
        }
    }

    val uiState: StateFlow<DictionaryUiState> = combine(
        vocabRepo.getAllWords(),
        vocabRepo.getAllCategories(),
        vocabRepo.getCategoryWordCounts(),
        vocabRepo.getAllWordCategoryRefs(),
        _currentFilter,
        _selectedCategoryId,
        _searchQuery,
        _isMultiSelectMode,
        _selectedWordIds,
        _selectedTab,
        _userMessage
    ) { params ->
        val allWords = params[0] as List<VocabularyEntity>
        val categories = params[1] as List<CategoryEntity>
        val counts = params[2] as Map<String, Int>
        val crossRefs = params[3] as List<WordCategoryCrossRef>
        val filter = params[4] as DictionaryFilter
        val categoryId = params[5] as String?
        val query = params[6] as String
        val isMultiSelect = params[7] as Boolean
        val selectedIds = params[8] as Set<String>
        val tab = params[9] as Int
        val msg = params[10] as String?

        val trimmedQuery = query.trim().lowercase()

        // Build word -> categories mapping
        val categoriesById = categories.associateBy { it.id }
        val wordToCatIds = crossRefs.groupBy({ it.wordId }, { it.categoryId })
        val wordCategoriesMap = wordToCatIds.mapValues { (_, catIds) ->
            catIds.mapNotNull { categoriesById[it] }
        }

        var filtered = when (filter) {
            DictionaryFilter.ALL -> allWords
            DictionaryFilter.TODAY -> {
                val todayWords = vocabRepo.getWordsForDate(todayDate).first()
                todayWords
            }
            DictionaryFilter.FAVORITES -> allWords.filter { it.isFavorite }
            DictionaryFilter.REVISION_DUE -> allWords.filter {
                it.nextReviewDate.isNotBlank() && it.nextReviewDate <= todayDate
            }
            DictionaryFilter.LEARNING -> allWords.filter {
                it.learningStatus == WordLearningStatus.LEARNING.name || it.learningStatus == WordLearningStatus.REVIEW.name
            }
            DictionaryFilter.MASTERED -> allWords.filter {
                it.learningStatus == WordLearningStatus.MASTERED.name
            }
            DictionaryFilter.DIFFICULT -> allWords.filter {
                it.difficulty.equals("hard", ignoreCase = true)
            }
        }

        // Apply category/folder filter if selected
        if (categoryId != null) {
            val categoryWords = vocabRepo.getWordsByCategory(categoryId).first()
            val categoryWordIds = categoryWords.map { it.id }.toSet()
            filtered = filtered.filter { it.id in categoryWordIds }
        }

        // Apply search query
        if (trimmedQuery.isNotEmpty()) {
            filtered = filtered.filter { word ->
                word.word.contains(trimmedQuery, ignoreCase = true) ||
                word.meaning.contains(trimmedQuery, ignoreCase = true) ||
                word.hindiMeaning.contains(trimmedQuery, ignoreCase = true) ||
                word.synonyms.any { it.contains(trimmedQuery, ignoreCase = true) }
            }
        }

        val revisionDue = allWords.count { it.nextReviewDate.isNotBlank() && it.nextReviewDate <= todayDate }
        val mastered = allWords.count { it.learningStatus == WordLearningStatus.MASTERED.name }

        DictionaryUiState(
            words = allWords,
            filteredWords = filtered,
            categories = categories,
            categoryWordCounts = counts,
            wordCategories = wordCategoriesMap,
            currentFilter = filter,
            selectedCategoryId = categoryId,
            searchQuery = query,
            totalWordsCount = allWords.size,
            masteredWordsCount = mastered,
            revisionDueCount = revisionDue,
            isMultiSelectMode = isMultiSelect,
            selectedWordIds = selectedIds,
            selectedTab = tab,
            isLoading = false,
            userMessage = msg
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DictionaryUiState(isLoading = true)
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun setFilter(filter: DictionaryFilter) {
        _selectedCategoryId.value = null
        _currentFilter.value = filter
    }

    fun setCategoryFilter(categoryId: String?) {
        _selectedCategoryId.value = categoryId
        if (categoryId != null) {
            _selectedTab.value = 0
        }
    }

    fun clearCategoryFilter() {
        _selectedCategoryId.value = null
    }

    fun toggleFavorite(wordId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            vocabRepo.toggleFavorite(wordId, isFavorite)
        }
    }

    // --- Multi-Select ("Chune" Words) Mode ---
    fun startMultiSelect(initialWordId: String? = null) {
        _isMultiSelectMode.value = true
        _selectedWordIds.value = if (initialWordId != null) setOf(initialWordId) else emptySet()
    }

    fun exitMultiSelect() {
        _isMultiSelectMode.value = false
        _selectedWordIds.value = emptySet()
    }

    fun toggleWordSelection(wordId: String) {
        val current = _selectedWordIds.value.toMutableSet()
        if (current.contains(wordId)) {
            current.remove(wordId)
        } else {
            current.add(wordId)
        }
        _selectedWordIds.value = current
        if (current.isEmpty() && !_isMultiSelectMode.value) {
            _isMultiSelectMode.value = false
        }
    }

    fun selectAll(wordsToSelect: List<VocabularyEntity>) {
        _selectedWordIds.value = wordsToSelect.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedWordIds.value = emptySet()
    }

    // --- Category / Folder Operations ---
    fun addCategory(name: String, description: String = "", colorHex: String = "#1A365D", onCreated: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            vocabRepo.addCategory(name, description, colorHex)
            val normalized = name.lowercase().trim().replace(Regex("[^a-z0-9]"), "_")
            val id = "cat_${normalized}"
            onCreated?.invoke(id)
            _userMessage.value = "Folder '$name' created!"
        }
    }

    fun renameCategory(id: String, newName: String, newDescription: String) {
        viewModelScope.launch {
            vocabRepo.renameCategory(id, newName, newDescription)
            _userMessage.value = "Folder renamed to '$newName'"
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            vocabRepo.deleteCategory(id)
            if (_selectedCategoryId.value == id) {
                _selectedCategoryId.value = null
            }
            _userMessage.value = "Folder deleted."
        }
    }

    fun deleteWord(wordId: String) {
        viewModelScope.launch {
            vocabRepo.deleteWord(wordId)
            _userMessage.value = "Word deleted."
        }
    }

    fun deleteSelectedWords() {
        viewModelScope.launch {
            val toDelete = _selectedWordIds.value.toList()
            if (toDelete.isNotEmpty()) {
                vocabRepo.deleteWords(toDelete)
                exitMultiSelect()
                _userMessage.value = "${toDelete.size} words deleted."
            }
        }
    }

    fun assignWordToCategory(wordId: String, categoryId: String) {
        viewModelScope.launch {
            vocabRepo.assignWordToCategory(wordId, categoryId)
        }
    }

    fun removeWordFromCategory(wordId: String, categoryId: String) {
        viewModelScope.launch {
            vocabRepo.removeWordFromCategory(wordId, categoryId)
        }
    }

    fun toggleWordCategory(wordId: String, category: CategoryEntity, isCurrentlyAssigned: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyAssigned) {
                vocabRepo.removeWordFromCategory(wordId, category.id)
                _userMessage.value = "Removed from ${category.name}"
            } else {
                vocabRepo.assignWordToCategory(wordId, category.id)
                _userMessage.value = "Added to ${category.name}"
            }
        }
    }

    fun assignSelectedWordsToCategory(categoryId: String, categoryName: String) {
        val selected = _selectedWordIds.value.toList()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            vocabRepo.assignWordsToCategory(selected, categoryId)
            _userMessage.value = "Added ${selected.size} words to '$categoryName'!"
            exitMultiSelect()
        }
    }

    fun createFolderAndAssignSelectedWords(name: String, description: String, colorHex: String) {
        val selected = _selectedWordIds.value.toList()
        viewModelScope.launch {
            vocabRepo.addCategory(name, description, colorHex)
            val normalized = name.lowercase().trim().replace(Regex("[^a-z0-9]"), "_")
            val id = "cat_${normalized}"
            if (selected.isNotEmpty()) {
                vocabRepo.assignWordsToCategory(selected, id)
                _userMessage.value = "Created '$name' & added ${selected.size} words!"
            } else {
                _userMessage.value = "Created folder '$name'!"
            }
            exitMultiSelect()
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
