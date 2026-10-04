package com.example.presentation.dictionary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.VocabularyEntity
import kotlinx.coroutines.launch

data class SubjectPreset(
    val title: String,
    val description: String,
    val iconEmoji: String,
    val colorHex: String
)

val SUBJECT_PRESETS = listOf(
    SubjectPreset("⭐ Hand-picked / Chune hue", "My personal collection of chosen words", "⭐", "#D97706"),
    SubjectPreset("📈 Economy & Finance", "Banking, inflation, GDP, budget, trade policies", "📈", "#2563EB"),
    SubjectPreset("🏛️ Polity & Governance", "Constitution, parliament, judiciary, governance", "🏛️", "#7C3AED"),
    SubjectPreset("🌍 Geopolitics & Foreign Affairs", "International diplomacy, summits, treaties, conflicts", "🌍", "#059669"),
    SubjectPreset("🔬 Science, Tech & Climate", "Artificial Intelligence, space, climate change, energy", "🔬", "#0284C7"),
    SubjectPreset("🎓 Exam & High-Frequency", "Crucial words for competitive exams, reading & comprehension", "🎓", "#DC2626"),
    SubjectPreset("💡 Idioms & Phrasal Terms", "Expressions, idioms, and phrases from news articles", "💡", "#B45309")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    onSelectCategory: (String) -> Unit,
    viewModel: DictionaryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var folderForWordPicker by remember { mutableStateOf<CategoryEntity?>(null) }
    var expandedFolderId by remember { mutableStateOf<String?>(null) }

    // Create folder input fields
    var newCatName by remember { mutableStateOf("") }
    var newCatDesc by remember { mutableStateOf("") }
    var newCatColor by remember { mutableStateOf("#1A365D") }

    // Listen to user messages
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Subject Folders",
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif)
                        )
                        Text(
                            text = "Organize & Hand-pick Vocabulary",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            newCatName = ""
                            newCatDesc = ""
                            newCatColor = "#1A365D"
                            showCreateDialog = true
                        },
                        modifier = Modifier.testTag("create_folder_top_btn")
                    ) {
                        Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "New Folder")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newCatName = ""
                    newCatDesc = ""
                    newCatColor = "#1A365D"
                    showCreateDialog = true
                },
                modifier = Modifier.testTag("add_category_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add New Folder")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Explanatory Banner Card
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "📁", fontSize = 28.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Create Custom & Subject-wise Folders",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Group words by subjects (Economy, Polity) or hand-pick personal lists to revise together.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.categories.size} Folders Available",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = {
                        newCatName = ""
                        newCatDesc = ""
                        showCreateDialog = true
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Folder", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Folders List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.categories, key = { it.id }) { cat ->
                    val wordCount = uiState.categoryWordCounts[cat.id] ?: 0
                    val isExpanded = expandedFolderId == cat.id

                    FolderCard(
                        category = cat,
                        wordCount = wordCount,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedFolderId = if (isExpanded) null else cat.id
                        },
                        onOpenFolder = {
                            viewModel.setCategoryFilter(cat.id)
                            onSelectCategory(cat.id)
                        },
                        onAddWords = {
                            folderForWordPicker = cat
                        },
                        onEdit = {
                            editingCategory = cat
                        },
                        onDelete = {
                            viewModel.deleteCategory(cat.id)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Dialog: Create New Folder with Subject Presets
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Create Subject Folder")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Quick Presets (Tap to choose):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Preset chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SUBJECT_PRESETS.forEach { preset ->
                                FilterChip(
                                    selected = newCatName == preset.title,
                                    onClick = {
                                        newCatName = preset.title
                                        newCatDesc = preset.description
                                        newCatColor = preset.colorHex
                                    },
                                    label = { Text(preset.title, fontSize = 12.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = newCatName,
                            onValueChange = { newCatName = it },
                            label = { Text("Folder / Subject Name *") },
                            placeholder = { Text("e.g. Economy & Trade, Chune hue Words...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("folder_name_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newCatDesc,
                            onValueChange = { newCatDesc = it },
                            label = { Text("Description (Optional)") },
                            placeholder = { Text("e.g. Important words for fiscal policy") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCatName.isNotBlank()) {
                                viewModel.addCategory(newCatName, newCatDesc, newCatColor)
                                newCatName = ""
                                newCatDesc = ""
                                showCreateDialog = false
                            }
                        },
                        enabled = newCatName.isNotBlank(),
                        modifier = Modifier.testTag("confirm_create_folder_btn")
                    ) {
                        Text("Create Folder")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Dialog: Edit Folder
        editingCategory?.let { cat ->
            var editName by remember(cat) { mutableStateOf(cat.name) }
            var editDesc by remember(cat) { mutableStateOf(cat.description) }

            AlertDialog(
                onDismissRequest = { editingCategory = null },
                title = { Text("Edit Folder") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Folder Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editDesc,
                            onValueChange = { editDesc = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editName.isNotBlank()) {
                                viewModel.renameCategory(cat.id, editName, editDesc)
                                editingCategory = null
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingCategory = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Word Picker Bottom Sheet (Hand-pick / Chune Words into Folder)
        folderForWordPicker?.let { targetFolder ->
            WordPickerBottomSheet(
                folder = targetFolder,
                allWords = uiState.words,
                onDismiss = { folderForWordPicker = null },
                onAddWord = { wordId ->
                    viewModel.assignWordToCategory(wordId, targetFolder.id)
                },
                onRemoveWord = { wordId ->
                    viewModel.removeWordFromCategory(wordId, targetFolder.id)
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun FolderCard(
    category: CategoryEntity,
    wordCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenFolder: () -> Unit,
    onAddWords: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("folder_card_${category.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (category.description.isNotBlank()) {
                            Text(
                                text = category.description,
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
                        text = "$wordCount words",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Open / View Words Button
                    Button(
                        onClick = onOpenFolder,
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Words", fontSize = 12.sp)
                    }

                    // + Add Words Button
                    OutlinedButton(
                        onClick = onAddWords,
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Add Words", fontSize = 12.sp)
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Folder",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Folder",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordPickerBottomSheet(
    folder: CategoryEntity,
    allWords: List<VocabularyEntity>,
    onDismiss: () -> Unit,
    onAddWord: (String) -> Unit,
    onRemoveWord: (String) -> Unit,
    viewModel: DictionaryViewModel
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    // Words currently in this folder
    val wordsInFolder = uiState.wordCategories.filterValues { cats -> cats.any { it.id == folder.id } }.keys

    val filteredList = remember(allWords, query) {
        if (query.isBlank()) allWords
        else allWords.filter {
            it.word.contains(query, ignoreCase = true) ||
            it.meaning.contains(query, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add Words to '${folder.name}'",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Checkmark words to include in this folder",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search input
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search word or meaning...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${filteredList.size} words available • ${wordsInFolder.size} currently in folder",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredList, key = { it.id }) { word ->
                    val isChecked = word.id in wordsInFolder

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) onRemoveWord(word.id)
                                else onAddWord(word.id)
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = word.word,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = word.meaning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }

                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) onAddWord(word.id)
                                    else onRemoveWord(word.id)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done (${wordsInFolder.size} in folder)")
            }
        }
    }
}
