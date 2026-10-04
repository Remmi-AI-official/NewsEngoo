package com.example.presentation.dictionary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.VocabularyEntity
import com.example.presentation.components.EmptyStateView

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    onNavigateToWordDetail: (String) -> Unit,
    onNavigateToCategories: () -> Unit,
    viewModel: DictionaryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog & Sheet States
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showBatchFolderPicker by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }
    var wordToDelete by remember { mutableStateOf<VocabularyEntity?>(null) }
    var wordForQuickFolder by remember { mutableStateOf<VocabularyEntity?>(null) }

    // New folder form state
    var newFolderName by remember { mutableStateOf("") }
    var newFolderDesc by remember { mutableStateOf("") }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    val activeCategory = uiState.categories.find { it.id == uiState.selectedCategoryId }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (uiState.isMultiSelectMode) {
                // Multi-selection Top Bar ("Chune" Words Mode)
                TopAppBar(
                    title = {
                        Text(
                            text = "${uiState.selectedWordIds.size} words selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.exitMultiSelect() },
                            modifier = Modifier.testTag("exit_multi_select_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel Selection")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (uiState.selectedWordIds.size == uiState.filteredWords.size) {
                                    viewModel.clearSelection()
                                } else {
                                    viewModel.selectAll(uiState.filteredWords)
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.SelectAll, contentDescription = "Select All")
                        }

                        Button(
                            onClick = { showBatchFolderPicker = true },
                            enabled = uiState.selectedWordIds.isNotEmpty(),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("batch_add_to_folder_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Folder", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showDeleteSelectedDialog = true },
                            enabled = uiState.selectedWordIds.isNotEmpty(),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("batch_delete_words_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete (${uiState.selectedWordIds.size})", fontSize = 12.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            } else {
                // Normal Top Bar
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Personal Dictionary",
                                style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif)
                            )
                            if (activeCategory != null) {
                                Text(
                                    text = "Folder: ${activeCategory.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        // Enter Multi-Select Mode ("Chune / Hand-pick" words)
                        IconButton(
                            onClick = { viewModel.startMultiSelect() },
                            modifier = Modifier.testTag("multi_select_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Checklist, contentDescription = "Select Words to Add to Folder")
                        }

                        // Quick Create Folder Action
                        IconButton(
                            onClick = {
                                newFolderName = ""
                                newFolderDesc = ""
                                showCreateFolderDialog = true
                            },
                            modifier = Modifier.testTag("quick_create_folder_btn")
                        ) {
                            Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "New Subject Folder")
                        }

                        // Manage all Folders screen
                        IconButton(
                            onClick = onNavigateToCategories,
                            modifier = Modifier.testTag("manage_categories_btn")
                        ) {
                            Icon(imageVector = Icons.Default.FolderSpecial, contentDescription = "All Subject Folders")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // View Mode Tabs: All Words vs Folders Overview
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    text = { Text("Words (${uiState.totalWordsCount})", fontWeight = FontWeight.Medium) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    text = { Text("📁 Subject Folders (${uiState.categories.size})", fontWeight = FontWeight.Medium) }
                )
            }

            if (uiState.selectedTab == 1) {
                // Folders Overview Tab
                FoldersOverviewTab(
                    categories = uiState.categories,
                    counts = uiState.categoryWordCounts,
                    onSelectFolder = { catId ->
                        viewModel.setCategoryFilter(catId)
                        viewModel.setSelectedTab(0)
                    },
                    onManageFolders = onNavigateToCategories,
                    onCreateFolder = {
                        newFolderName = ""
                        newFolderDesc = ""
                        showCreateFolderDialog = true
                    }
                )
            } else {
                // All Words Tab

                // Search TextField
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search word, meaning, hindi, synonym...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .testTag("dictionary_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )

                // Active Folder Banner if a folder is currently filtered
                if (activeCategory != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = "📁", fontSize = 16.sp)
                                Text(
                                    text = "Filtered by: ${activeCategory.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearCategoryFilter() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Folder Filter",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Horizontal Filter Chips (Status filters + Folder chips + "+ New Folder" chip)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Action: "+ New Folder" Chip
                    AssistChip(
                        onClick = {
                            newFolderName = ""
                            newFolderDesc = ""
                            showCreateFolderDialog = true
                        },
                        label = { Text("+ New Folder", fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            labelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Standard Filter Chips
                    DictionaryFilter.values().forEach { filter ->
                        val isSelected = uiState.currentFilter == filter && uiState.selectedCategoryId == null
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setFilter(filter) },
                            label = {
                                Text(
                                    text = when (filter) {
                                        DictionaryFilter.ALL -> "All (${uiState.totalWordsCount})"
                                        DictionaryFilter.REVISION_DUE -> "Revision Due (${uiState.revisionDueCount})"
                                        DictionaryFilter.MASTERED -> "Mastered (${uiState.masteredWordsCount})"
                                        else -> filter.label
                                    }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    // Custom Subject Folder Chips
                    uiState.categories.forEach { cat ->
                        val isCatSelected = uiState.selectedCategoryId == cat.id
                        val count = uiState.categoryWordCounts[cat.id] ?: 0
                        FilterChip(
                            selected = isCatSelected,
                            onClick = { viewModel.setCategoryFilter(if (isCatSelected) null else cat.id) },
                            label = { Text("📁 ${cat.name} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }

                // Multi-select Help Bar
                if (uiState.isMultiSelectMode) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "💡 Tap words to select them, then tap 'Add to Folder' above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Word List
                if (uiState.filteredWords.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Translate,
                        title = "No Words Found",
                        message = if (uiState.searchQuery.isNotEmpty()) {
                            "No dictionary entries match \"${uiState.searchQuery}\"."
                        } else if (activeCategory != null) {
                            "Folder '${activeCategory.name}' is empty. Select words from the dictionary to add to this folder."
                        } else {
                            "No words currently in this filter. Read editorials to build your vocabulary."
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${uiState.filteredWords.size} words listed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (!uiState.isMultiSelectMode) {
                                    TextButton(
                                        onClick = { viewModel.startMultiSelect() },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Select Words", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        items(uiState.filteredWords, key = { it.id }) { word ->
                            val isSelected = uiState.selectedWordIds.contains(word.id)
                            val assignedFolders = uiState.wordCategories[word.id] ?: emptyList()

                            DictionaryWordCard(
                                word = word,
                                assignedFolders = assignedFolders,
                                isMultiSelectMode = uiState.isMultiSelectMode,
                                isSelected = isSelected,
                                onWordClick = {
                                    if (uiState.isMultiSelectMode) {
                                        viewModel.toggleWordSelection(word.id)
                                    } else {
                                        onNavigateToWordDetail(word.id)
                                    }
                                },
                                onLongClick = {
                                    if (!uiState.isMultiSelectMode) {
                                        viewModel.startMultiSelect(word.id)
                                    }
                                },
                                onToggleSelect = {
                                    viewModel.toggleWordSelection(word.id)
                                },
                                onToggleFavorite = {
                                    viewModel.toggleFavorite(word.id, !word.isFavorite)
                                },
                                onManageFolderClick = {
                                    wordForQuickFolder = word
                                },
                                onDeleteClick = {
                                    wordToDelete = word
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        // Dialog: Delete Single Word
        if (wordToDelete != null) {
            val word = wordToDelete!!
            AlertDialog(
                onDismissRequest = { wordToDelete = null },
                title = { Text("Delete '${word.word}'?") },
                text = { Text("Are you sure you want to delete this word from your dictionary? This will also remove it from any assigned subject folders.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteWord(word.id)
                            wordToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { wordToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Dialog: Delete Selected Words Batch
        if (showDeleteSelectedDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteSelectedDialog = false },
                title = { Text("Delete ${uiState.selectedWordIds.size} Words?") },
                text = { Text("Are you sure you want to permanently delete these ${uiState.selectedWordIds.size} selected words from your dictionary?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteSelectedDialog = false
                            viewModel.deleteSelectedWords()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete All")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteSelectedDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Dialog: Quick Create Subject Folder
        if (showCreateFolderDialog) {
            AlertDialog(
                onDismissRequest = { showCreateFolderDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("New Subject Folder")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Preset Subjects:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SUBJECT_PRESETS.take(5).forEach { preset ->
                                FilterChip(
                                    selected = newFolderName == preset.title,
                                    onClick = {
                                        newFolderName = preset.title
                                        newFolderDesc = preset.description
                                    },
                                    label = { Text(preset.title, fontSize = 11.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            label = { Text("Folder Name *") },
                            placeholder = { Text("e.g. Economy & Trade, Handpicked...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newFolderDesc,
                            onValueChange = { newFolderDesc = it },
                            label = { Text("Description (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newFolderName.isNotBlank()) {
                                viewModel.addCategory(newFolderName, newFolderDesc)
                                newFolderName = ""
                                newFolderDesc = ""
                                showCreateFolderDialog = false
                            }
                        },
                        enabled = newFolderName.isNotBlank()
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateFolderDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Modal Bottom Sheet: Batch Assign Selected Words to a Folder
        if (showBatchFolderPicker) {
            BatchFolderPickerSheet(
                categories = uiState.categories,
                selectedWordCount = uiState.selectedWordIds.size,
                onDismiss = { showBatchFolderPicker = false },
                onSelectFolder = { catId, catName ->
                    viewModel.assignSelectedWordsToCategory(catId, catName)
                    showBatchFolderPicker = false
                },
                onCreateFolderAndAssign = { name, desc ->
                    viewModel.createFolderAndAssignSelectedWords(name, desc, "#1A365D")
                    showBatchFolderPicker = false
                }
            )
        }

        // Modal Dialog: Quick Folder Management for a Single Word
        wordForQuickFolder?.let { word ->
            val assigned = uiState.wordCategories[word.id] ?: emptyList()
            QuickWordFolderDialog(
                word = word,
                allCategories = uiState.categories,
                assignedCategories = assigned,
                onDismiss = { wordForQuickFolder = null },
                onToggleCategory = { cat, isAssigned ->
                    viewModel.toggleWordCategory(word.id, cat, isAssigned)
                },
                onCreateFolderAndAssign = { name, desc ->
                    viewModel.addCategory(name, desc) { newCatId ->
                        viewModel.assignWordToCategory(word.id, newCatId)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun DictionaryWordCard(
    word: VocabularyEntity,
    assignedFolders: List<CategoryEntity>,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onWordClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onManageFolderClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onWordClick,
                onLongClick = onLongClick
            )
            .testTag("word_card_${word.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox in Multi-Select Mode
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = word.word,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = word.partOfSpeech.lowercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Folder Button on each Card
                        IconButton(
                            onClick = onManageFolderClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (assignedFolders.isNotEmpty()) Icons.Default.Folder else Icons.Default.FolderOpen,
                                contentDescription = "Add to Folder",
                                tint = if (assignedFolders.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (word.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (word.isFavorite) Color(0xFFE53E3E) else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Word",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (word.pronunciation.isNotBlank()) {
                    Text(
                        text = word.pronunciation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = word.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )

                if (word.hindiMeaning.isNotBlank()) {
                    Text(
                        text = "Hindi: ${word.hindiMeaning}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Assigned Folder Badges
                if (assignedFolders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        assignedFolders.forEach { folder ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Synonyms
                if (word.synonyms.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        word.synonyms.take(3).forEach { syn ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = syn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoldersOverviewTab(
    categories: List<CategoryEntity>,
    counts: Map<String, Int>,
    onSelectFolder: (String) -> Unit,
    onManageFolders: () -> Unit,
    onCreateFolder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Subject & Custom Folders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Organize words into focused subject lists",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onCreateFolder,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Folder", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories, key = { it.id }) { cat ->
                val count = counts[cat.id] ?: 0

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectFolder(cat.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (cat.description.isNotBlank()) {
                                    Text(
                                        text = cat.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$count words",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onManageFolders,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.FolderSpecial, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Manage & Edit All Folders")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchFolderPickerSheet(
    categories: List<CategoryEntity>,
    selectedWordCount: Int,
    onDismiss: () -> Unit,
    onSelectFolder: (String, String) -> Unit,
    onCreateFolderAndAssign: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCreateInline by remember { mutableStateOf(false) }
    var inlineName by remember { mutableStateOf("") }
    var inlineDesc by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Add $selectedWordCount words to folder",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose a destination subject folder",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (!showCreateInline) {
                OutlinedButton(
                    onClick = { showCreateInline = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Create New Folder")
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Create New Folder", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = inlineName,
                            onValueChange = { inlineName = it },
                            label = { Text("Folder Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = inlineDesc,
                            onValueChange = { inlineDesc = it },
                            label = { Text("Description (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (inlineName.isNotBlank()) {
                                        onCreateFolderAndAssign(inlineName, inlineDesc)
                                    }
                                },
                                enabled = inlineName.isNotBlank()
                            ) {
                                Text("Create & Add")
                            }
                            TextButton(onClick = { showCreateInline = false }) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it.id }) { cat ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectFolder(cat.id, cat.name) },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = cat.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                if (cat.description.isNotBlank()) {
                                    Text(text = cat.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickWordFolderDialog(
    word: VocabularyEntity,
    allCategories: List<CategoryEntity>,
    assignedCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onToggleCategory: (CategoryEntity, Boolean) -> Unit,
    onCreateFolderAndAssign: (String, String) -> Unit
) {
    var showCreateInline by remember { mutableStateOf(false) }
    var inlineName by remember { mutableStateOf("") }
    var inlineDesc by remember { mutableStateOf("") }

    val assignedIds = remember(assignedCategories) { assignedCategories.map { it.id }.toSet() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Folders for '${word.word}'")
                Text(
                    text = "Select which folders this word belongs to",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!showCreateInline) {
                    TextButton(
                        onClick = { showCreateInline = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Create New Folder")
                    }
                } else {
                    OutlinedTextField(
                        value = inlineName,
                        onValueChange = { inlineName = it },
                        label = { Text("New Folder Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateInline = false }) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (inlineName.isNotBlank()) {
                                    onCreateFolderAndAssign(inlineName, inlineDesc)
                                    inlineName = ""
                                    showCreateInline = false
                                }
                            },
                            enabled = inlineName.isNotBlank()
                        ) {
                            Text("Create")
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allCategories, key = { it.id }) { cat ->
                        val isChecked = cat.id in assignedIds
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleCategory(cat, isChecked) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        onToggleCategory(cat, !checked)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
