package com.example.presentation.manager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.presentation.components.EmptyStateView
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.DateUtils
import com.example.domain.model.ImportMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContentManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: ContentManagerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Import, 1: Manage & Delete, 2: Backup & Restore, 3: History

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Content Manager & Import") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Import") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Manage & Delete") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Backup") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("History") })
            }

            when (selectedTab) {
                0 -> ImportTabContent(uiState = uiState, viewModel = viewModel)
                1 -> ManageDeleteTabContent(uiState = uiState, viewModel = viewModel)
                2 -> BackupTabContent(uiState = uiState, viewModel = viewModel, context = context)
                3 -> HistoryTabContent(uiState = uiState)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportTabContent(
    uiState: ContentManagerUiState,
    viewModel: ContentManagerViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Content Type Horizontal Chips
        Text("Select Content Type to Import", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImportContentType.values().forEach { type ->
                FilterChip(
                    selected = uiState.selectedType == type,
                    onClick = { viewModel.setContentType(type) },
                    label = { Text(type.label) }
                )
            }
        }

        // Date selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.targetDate,
                onValueChange = { viewModel.setTargetDate(it) },
                label = { Text("Target Date (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedButton(onClick = { viewModel.setTargetDate(DateUtils.getTodayDate()) }) {
                Text("Today")
            }
        }

        // Extra fields if Editorial
        if (uiState.selectedType == ImportContentType.EDITORIAL) {
            OutlinedTextField(
                value = uiState.editorialTitle,
                onValueChange = { viewModel.setEditorialTitle(it) },
                label = { Text("Editorial Title") },
                placeholder = { Text("e.g. The Fiscal Path Forward") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.editorialSource,
                onValueChange = { viewModel.setEditorialSource(it) },
                label = { Text("Source Publication") },
                placeholder = { Text("e.g. The Hindu, Indian Express") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // Template Helper Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Content (JSON or Markdown)", style = MaterialTheme.typography.labelLarge)
            OutlinedButton(
                onClick = {
                    val sample = getSampleTemplate(uiState.selectedType, uiState.targetDate)
                    viewModel.setRawInput(sample)
                },
                modifier = Modifier.testTag("load_sample_template_btn")
            ) {
                Text("Load Sample", fontSize = 12.sp)
            }
        }

        // Main Raw Input TextField
        OutlinedTextField(
            value = uiState.rawInput,
            onValueChange = { viewModel.setRawInput(it) },
            placeholder = { Text("Paste JSON or Markdown content here...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("content_raw_input"),
            shape = RoundedCornerShape(12.dp)
        )

        // Validate Button
        Button(
            onClick = { viewModel.validateInput() },
            modifier = Modifier.fillMaxWidth().testTag("validate_input_btn")
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Validate & Preview")
        }

        // Validation Preview Result
        val preview = uiState.validationPreview
        if (preview != null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (preview.isValid) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (preview.isValid) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (preview.isValid) Color(0xFF22543D) else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (preview.isValid) "Validation Passed (${preview.totalCount} items detected)" else "Import Validation Failed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (preview.isValid) Color(0xFF22543D) else MaterialTheme.colorScheme.error
                        )
                    }

                    if (preview.isValid) {
                        Text(preview.sampleSummary, style = MaterialTheme.typography.bodyMedium)
                        if (preview.duplicateCount > 0) {
                            Text(
                                text = "⚠️ ${preview.duplicateCount} existing entries found. Choose deduplication strategy:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Duplicate Strategy Selector
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ImportMode.values().forEach { mode ->
                                    FilterChip(
                                        selected = uiState.selectedImportMode == mode,
                                        onClick = { viewModel.setImportMode(mode) },
                                        label = {
                                            Text(
                                                when (mode) {
                                                    ImportMode.MERGE_DUPLICATES -> "Merge (Recommended)"
                                                    ImportMode.IMPORT_NEW -> "Import New Only"
                                                    ImportMode.SKIP_DUPLICATES -> "Skip Duplicates"
                                                    ImportMode.REPLACE_EXISTING -> "Replace Existing"
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.executeImport() },
                            modifier = Modifier.fillMaxWidth().testTag("execute_import_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22543D))
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Execute Import to Database")
                        }
                    } else {
                        Text(
                            text = preview.errorMessage ?: "Malformed format",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Messages
        if (uiState.lastSuccessMessage != null) {
            Surface(
                color = Color(0xFF22543D).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.lastSuccessMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF22543D),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (uiState.lastErrorMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.lastErrorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BackupTabContent(
    uiState: ContentManagerUiState,
    viewModel: ContentManagerViewModel,
    context: Context
) {
    var restoreInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Export Full Learning Library", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Export all your articles, vocabulary, categories, grammar, phrases, test attempts, and progress into a portable, offline JSON format.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { viewModel.exportFullBackup() },
                    modifier = Modifier.fillMaxWidth().testTag("export_backup_btn")
                ) {
                    Text("Generate Full Backup JSON")
                }

                if (uiState.exportedJsonString != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Editorial English Backup", uiState.exportedJsonString))
                            Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("copy_backup_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Backup to Clipboard (${uiState.exportedJsonString.length} chars)")
                    }
                }
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Restore from Backup JSON", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Paste a previously exported JSON backup to restore all words, grammar rules, and learning data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = restoreInput,
                    onValueChange = { restoreInput = it },
                    placeholder = { Text("Paste backup JSON here...") },
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
                Button(
                    onClick = {
                        if (restoreInput.isNotBlank()) {
                            viewModel.restoreBackup(restoreInput)
                            restoreInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Restore Library")
                }
            }
        }
    }
}

@Composable
fun HistoryTabContent(uiState: ContentManagerUiState) {
    if (uiState.importHistory.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.History,
            title = "No Import History",
            message = "Any content you import will appear here with record counts and timestamps."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.importHistory, key = { it.id }) { item ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = item.type.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = DateUtils.formatDate(item.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item.description, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${item.itemCount} items (${item.newCount} new, ${item.mergedCount} merged)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun getSampleTemplate(type: ImportContentType, date: String): String {
    return when (type) {
        ImportContentType.DAILY_PACKAGE -> """{
  "date": "$date",
  "editorial": {
    "title": "A Paradigm Shift in Sustainable Growth",
    "contentMarkdown": "# A Paradigm Shift in Sustainable Growth\n\nPolicymakers must navigate a delicate dichotomy...",
    "source": "Financial Express",
    "readTimeMinutes": 5
  },
  "vocabulary": [
    {
      "word": "paradigm",
      "partOfSpeech": "noun",
      "meaning": "a typical example or model",
      "hindiMeaning": "प्रतिमान",
      "synonyms": ["model", "standard"],
      "antonyms": ["anomaly"],
      "example": "This marks a paradigm shift in environmental policy."
    }
  ],
  "grammar": [
    {
      "title": "Not only ... but also",
      "rule": "Maintain parallel grammatical structure after correlative conjunctions.",
      "explanation": "Ensure identical parts of speech follow both phrases.",
      "examples": ["He was not only intelligent but also diligent."]
    }
  ],
  "phrases": [
    {
      "phrase": "Turn the tide",
      "meaning": "Reverse the course of events completely.",
      "hindiMeaning": "पासा पलटना",
      "example": "The new investments turned the tide for the local economy."
    }
  ],
  "questions": [
    {
      "id": "q1",
      "type": "mcq",
      "question": "What is the synonym of paradigm?",
      "options": ["Model", "Deviation", "Error", "Anomaly"],
      "answer": 0,
      "explanation": "Paradigm means a model or archetype."
    }
  ]
}"""

        ImportContentType.VOCABULARY -> """{
  "date": "$date",
  "category": "Economy",
  "words": [
    {
      "word": "pragmatic",
      "partOfSpeech": "adjective",
      "meaning": "practical and realistic",
      "hindiMeaning": "व्यावहारिक",
      "synonyms": ["practical", "realistic"],
      "antonyms": ["idealistic"],
      "example": "The government adopted a pragmatic approach."
    }
  ]
}"""

        ImportContentType.EDITORIAL -> """# India's Emerging Frontiers in Global Trade

India's manufacturing sector is witnessing an unprecedented transformation...

### Strategic Advantages

By combining supply chain resilience with domestic consumption, the country is well-positioned...
"""

        ImportContentType.GRAMMAR -> """{
  "date": "$date",
  "rules": [
    {
      "title": "Despite vs Although",
      "rule": "Despite is followed by a noun or gerund; although is followed by a full clause.",
      "explanation": "Never write 'despite of' or attach a clause directly to despite.",
      "examples": ["Despite being tired, he persevered."]
    }
  ]
}"""

        ImportContentType.PHRASES -> """{
  "date": "$date",
  "phrases": [
    {
      "phrase": "Bite the bullet",
      "type": "idiom",
      "meaning": "Face a difficult situation with courage.",
      "hindiMeaning": "मुसीबत का डटकर सामना करना",
      "example": "The committee decided to bite the bullet."
    }
  ]
}"""

        ImportContentType.QUESTIONS -> """{
  "date": "$date",
  "questions": [
    {
      "id": "q1",
      "type": "mcq",
      "question": "What does ubiquitous mean?",
      "options": ["Rare", "Present everywhere", "Expensive", "Temporary"],
      "answer": 1,
      "explanation": "Ubiquitous means found or existing everywhere."
    }
  ]
}"""
    }
}

data class DeleteConfirmTarget(
    val title: String,
    val message: String,
    val onConfirm: () -> Unit
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManageDeleteTabContent(
    uiState: ContentManagerUiState,
    viewModel: ContentManagerViewModel
) {
    val editorials by viewModel.allEditorials.collectAsState()
    val words by viewModel.allVocabWords.collectAsState()
    val rules by viewModel.allGrammarRules.collectAsState()
    val phrases by viewModel.allPhrases.collectAsState()
    val tests by viewModel.allTests.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var dateToDelete by remember { mutableStateOf(DateUtils.getTodayDate()) }
    var confirmDialog by remember { mutableStateOf<DeleteConfirmTarget?>(null) }

    confirmDialog?.let { target ->
        AlertDialog(
            onDismissRequest = { confirmDialog = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(target.title) },
            text = { Text(target.message) },
            confirmButton = {
                Button(
                    onClick = {
                        target.onConfirm()
                        confirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Success / Error Banners
        if (uiState.lastSuccessMessage != null) {
            Surface(
                color = Color(0xFFE6FFFA),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF234E52), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = uiState.lastSuccessMessage, color = Color(0xFF234E52), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Section 1: Delete Complete Package by Date
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Content By Date", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Deletes editorial, linked words, grammar rules, expressions, and practice questions for the specified date in one go.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateToDelete,
                        onValueChange = { dateToDelete = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            confirmDialog = DeleteConfirmTarget(
                                title = "Delete Content for $dateToDelete?",
                                message = "Are you sure you want to delete all editorial, grammar, vocabulary links, phrases, and test questions for date $dateToDelete?",
                                onConfirm = { viewModel.deleteContentByDate(dateToDelete.trim()) }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text("Delete Date")
                    }
                }
            }
        }

        // Section 2: Interactive Individual Content Manager
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Browse & Delete Specific Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by title, word, rule...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Filter Chips
                val filterOptions = listOf("All", "Editorials (${editorials.size})", "Vocab (${words.size})", "Grammar (${rules.size})", "Phrases (${phrases.size})", "Tests (${tests.size})")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filterOptions.forEach { opt ->
                        val key = opt.substringBefore(" (")
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(opt, fontSize = 12.sp) }
                        )
                    }
                }

                HorizontalDivider()

                val q = searchQuery.trim().lowercase()

                // Editorials List
                if (selectedFilter == "All" || selectedFilter == "Editorials") {
                    val filteredEd = editorials.filter { q.isEmpty() || it.title.lowercase().contains(q) || it.date.contains(q) }
                    if (filteredEd.isNotEmpty()) {
                        Text("Editorials (${filteredEd.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        for (ed in filteredEd.take(15)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ed.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text("Date: ${ed.date} • ${ed.source}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = {
                                            confirmDialog = DeleteConfirmTarget(
                                                title = "Delete Editorial?",
                                                message = "Delete editorial '${ed.title}'?",
                                                onConfirm = { viewModel.deleteEditorial(ed.id) }
                                            )
                                        }
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // Vocab List
                if (selectedFilter == "All" || selectedFilter == "Vocab") {
                    val filteredWords = words.filter { q.isEmpty() || it.word.lowercase().contains(q) || it.meaning.lowercase().contains(q) }
                    if (filteredWords.isNotEmpty()) {
                        Text("Vocabulary (${filteredWords.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        for (w in filteredWords.take(15)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(w.word, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(w.meaning, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                        if (w.hindiMeaning.isNotBlank()) {
                                            Text("Hindi: ${w.hindiMeaning}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            confirmDialog = DeleteConfirmTarget(
                                                title = "Delete Word '${w.word}'?",
                                                message = "Delete word '${w.word}' from vocabulary?",
                                                onConfirm = { viewModel.deleteWord(w.id) }
                                            )
                                        }
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // Grammar List
                if (selectedFilter == "All" || selectedFilter == "Grammar") {
                    val filteredRules = rules.filter { q.isEmpty() || it.title.lowercase().contains(q) || it.rule.lowercase().contains(q) }
                    if (filteredRules.isNotEmpty()) {
                        Text("Grammar Rules (${filteredRules.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        for (r in filteredRules.take(15)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(r.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text(r.rule, style = MaterialTheme.typography.bodySmall, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = {
                                            confirmDialog = DeleteConfirmTarget(
                                                title = "Delete Grammar Rule?",
                                                message = "Delete rule '${r.title}'?",
                                                onConfirm = { viewModel.deleteGrammarRule(r.id) }
                                            )
                                        }
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // Phrases List
                if (selectedFilter == "All" || selectedFilter == "Phrases") {
                    val filteredPhrases = phrases.filter { q.isEmpty() || it.phrase.lowercase().contains(q) || it.meaning.lowercase().contains(q) }
                    if (filteredPhrases.isNotEmpty()) {
                        Text("Phrases & Idioms (${filteredPhrases.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        for (p in filteredPhrases.take(15)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(p.phrase, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(p.meaning, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                    }
                                    IconButton(
                                        onClick = {
                                            confirmDialog = DeleteConfirmTarget(
                                                title = "Delete Expression?",
                                                message = "Delete expression '${p.phrase}'?",
                                                onConfirm = { viewModel.deletePhrase(p.id) }
                                            )
                                        }
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // Tests List
                if (selectedFilter == "All" || selectedFilter == "Tests") {
                    val filteredTests = tests.filter { q.isEmpty() || it.title.lowercase().contains(q) || it.date.contains(q) }
                    if (filteredTests.isNotEmpty()) {
                        Text("Practice Tests (${filteredTests.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        for (t in filteredTests.take(15)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(t.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text("${t.type.uppercase()} • Date: ${t.date} • ${t.totalQuestions} Questions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = {
                                            confirmDialog = DeleteConfirmTarget(
                                                title = "Delete Practice Test?",
                                                message = "Delete test '${t.title}'?",
                                                onConfirm = { viewModel.deleteTest(t.id) }
                                            )
                                        }
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Bulk Reset / Clear Database Options
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Bulk Reset Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text("Use these buttons to wipe clean specific tables across all dates.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            confirmDialog = DeleteConfirmTarget(
                                title = "Clear All Editorials?",
                                message = "Are you sure you want to delete ALL editorials in the app?",
                                onConfirm = { viewModel.clearAllEditorials() }
                            )
                        }
                    ) {
                        Text("Clear Editorials", color = MaterialTheme.colorScheme.error)
                    }

                    OutlinedButton(
                        onClick = {
                            confirmDialog = DeleteConfirmTarget(
                                title = "Clear All Vocabulary Words?",
                                message = "Are you sure you want to delete ALL vocabulary words and folders?",
                                onConfirm = { viewModel.clearAllVocabulary() }
                            )
                        }
                    ) {
                        Text("Clear Vocabulary", color = MaterialTheme.colorScheme.error)
                    }

                    OutlinedButton(
                        onClick = {
                            confirmDialog = DeleteConfirmTarget(
                                title = "Clear All Grammar Rules?",
                                message = "Are you sure you want to delete ALL grammar rules in the library?",
                                onConfirm = { viewModel.clearAllGrammar() }
                            )
                        }
                    ) {
                        Text("Clear Grammar", color = MaterialTheme.colorScheme.error)
                    }

                    OutlinedButton(
                        onClick = {
                            confirmDialog = DeleteConfirmTarget(
                                title = "Clear All Phrases & Idioms?",
                                message = "Are you sure you want to delete ALL phrases and idioms?",
                                onConfirm = { viewModel.clearAllPhrases() }
                            )
                        }
                    ) {
                        Text("Clear Phrases", color = MaterialTheme.colorScheme.error)
                    }

                    OutlinedButton(
                        onClick = {
                            confirmDialog = DeleteConfirmTarget(
                                title = "Clear All Tests & Questions?",
                                message = "Are you sure you want to delete ALL practice tests and question banks?",
                                onConfirm = { viewModel.clearAllTests() }
                            )
                        }
                    ) {
                        Text("Clear Tests", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
