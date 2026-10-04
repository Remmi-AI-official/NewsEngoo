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
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
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
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Import, 1: Backup & Restore, 2: History

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
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Backup") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("History") })
            }

            when (selectedTab) {
                0 -> ImportTabContent(uiState = uiState, viewModel = viewModel)
                1 -> BackupTabContent(uiState = uiState, viewModel = viewModel, context = context)
                2 -> HistoryTabContent(uiState = uiState)
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
