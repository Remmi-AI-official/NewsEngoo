package com.example.presentation.dictionary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WordDetailScreen(
    wordId: String,
    onNavigateBack: () -> Unit,
    viewModel: WordDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(wordId) {
        viewModel.loadWord(wordId)
    }

    val word = uiState.word
    var isEditingNote by remember { mutableStateOf(false) }
    var noteInput by remember(word?.personalNote) { mutableStateOf(word?.personalNote ?: "") }
    var showCategorySelector by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showDeleteWordDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var newFolderDesc by remember { mutableStateOf("") }

    if (showDeleteWordDialog && word != null) {
        AlertDialog(
            onDismissRequest = { showDeleteWordDialog = false },
            title = { Text("Delete '${word.word}'?") },
            text = { Text("Are you sure you want to delete this word from your dictionary? This will also remove it from any assigned folders.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteWordDialog = false
                        viewModel.deleteWord { onNavigateBack() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteWordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(word?.word ?: "Word Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        modifier = Modifier.testTag("word_detail_fav_btn")
                    ) {
                        Icon(
                            imageVector = if (word?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (word?.isFavorite == true) Color(0xFFE53E3E) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showDeleteWordDialog = true },
                        modifier = Modifier.testTag("word_detail_delete_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Word",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (word != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Word Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = word.word,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = word.partOfSpeech.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (word.pronunciation.isNotBlank()) {
                            Text(
                                text = word.pronunciation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Text(
                            text = "English Meaning",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = word.meaning,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        if (word.hindiMeaning.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Hindi Meaning",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = word.hindiMeaning,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        if (word.wordFamily.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Word Family",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = word.wordFamily,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Editorial Context Sentence
                if (word.exampleSentence.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Editorial Context Example",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "“${word.exampleSentence}”",
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (word.sourceArticle.isNotBlank()) {
                                Text(
                                    text = "— Source: ${word.sourceArticle}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Synonyms & Antonyms Card
                if (word.synonyms.isNotEmpty() || word.antonyms.isNotEmpty()) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (word.synonyms.isNotEmpty()) {
                                Text(
                                    text = "Synonyms",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    word.synonyms.forEach { syn ->
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(syn) }
                                        )
                                    }
                                }
                            }

                            if (word.antonyms.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Antonyms",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    word.antonyms.forEach { ant ->
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(ant) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Categories Management Section
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "📁 Subject Folders",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row {
                                TextButton(
                                    onClick = {
                                        newFolderName = ""
                                        newFolderDesc = ""
                                        showCreateFolderDialog = true
                                    }
                                ) {
                                    Text("+ New Folder", fontSize = 12.sp)
                                }
                                IconButton(onClick = { showCategorySelector = !showCategorySelector }) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add to Folder")
                                }
                            }
                        }

                        if (uiState.assignedCategories.isEmpty()) {
                            Text(
                                text = "Not assigned to any subject folder. Tap '+' to organize.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                uiState.assignedCategories.forEach { cat ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📁 ${cat.name}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { viewModel.removeCategory(cat.id) },
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Category Selector Dropdown/List
                        if (showCategorySelector) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Add to Existing Folders:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val assignedIds = uiState.assignedCategories.map { it.id }.toSet()
                            val unassigned = uiState.allCategories.filter { it.id !in assignedIds }

                            if (unassigned.isEmpty()) {
                                Text(
                                    text = "All existing folders assigned. Tap '+ New Folder' to create another.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    unassigned.forEach { cat ->
                                        OutlinedButton(
                                            onClick = { viewModel.assignCategory(cat.id) },
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("+ ${cat.name}", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Dialog: Create New Folder from Word Detail
                if (showCreateFolderDialog) {
                    AlertDialog(
                        onDismissRequest = { showCreateFolderDialog = false },
                        title = { Text("New Subject Folder") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Create a folder and automatically assign '${word.word}' to it.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = newFolderName,
                                    onValueChange = { newFolderName = it },
                                    label = { Text("Folder Name *") },
                                    placeholder = { Text("e.g. Economy, Handpicked, Polity") },
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
                                        viewModel.createFolderAndAssign(newFolderName, newFolderDesc)
                                        newFolderName = ""
                                        newFolderDesc = ""
                                        showCreateFolderDialog = false
                                    }
                                },
                                enabled = newFolderName.isNotBlank()
                            ) {
                                Text("Create & Add Word")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCreateFolderDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // Spaced Repetition & Learning Progress
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Spaced Revision Status",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Level:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                word.learningStatus,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Review Count:", style = MaterialTheme.typography.bodyMedium)
                            Text("${word.reviewCount} times", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Next Review Due:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (word.nextReviewDate.isNotBlank()) DateUtils.formatShortDate(word.nextReviewDate) else "None",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Confidence: ${word.confidence}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                        LinearProgressIndicator(
                            progress = { word.confidence / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Practice Word Check
                        Text(
                            text = "Quick Practice",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Do you remember the meaning and usage of this word?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.recordPractice(false) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Still Learning")
                            }
                            Button(
                                onClick = { viewModel.recordPractice(true) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("I Know This ✓")
                            }
                        }
                        if (uiState.practiceResult != null) {
                            Text(
                                text = if (uiState.practiceResult == true) "Progress recorded! Word scheduled for future review." else "Marked for review tomorrow.",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.practiceResult == true) Color(0xFF22543D) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }

                // Personal Note Box
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Personal Notes & Mnemonics",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { isEditingNote = !isEditingNote }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }

                        if (isEditingNote) {
                            OutlinedTextField(
                                value = noteInput,
                                onValueChange = { noteInput = it },
                                placeholder = { Text("Write personal memory hooks or usages...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(onClick = {
                                    viewModel.saveNote(noteInput)
                                    isEditingNote = false
                                }) {
                                    Text("Save Note")
                                }
                            }
                        } else if (word.personalNote.isNotBlank()) {
                            Text(
                                text = word.personalNote,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            Text(
                                text = "No personal note yet. Tap edit to add a memory hook.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
