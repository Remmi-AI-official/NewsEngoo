package com.example.presentation.learn

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.entity.EditorialEntity
import com.example.data.local.entity.GrammarRuleEntity
import com.example.data.local.entity.PhraseEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.VocabularyEntity
import com.example.domain.model.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DailyLearnFlowScreen(
    date: String,
    onNavigateBack: () -> Unit,
    onNavigateToReader: (String) -> Unit,
    onNavigateToTest: (String) -> Unit,
    viewModel: DailyLearnViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Dialog state holders for Edit & Delete on Long-Press
    var editingEditorial by remember { mutableStateOf(false) }
    var deletingEditorial by remember { mutableStateOf(false) }
    var editorialContextMenu by remember { mutableStateOf(false) }

    var wordToEdit by remember { mutableStateOf<VocabularyEntity?>(null) }
    var wordToDelete by remember { mutableStateOf<VocabularyEntity?>(null) }
    var wordContextMenu by remember { mutableStateOf<VocabularyEntity?>(null) }

    var grammarToEdit by remember { mutableStateOf<GrammarRuleEntity?>(null) }
    var grammarToDelete by remember { mutableStateOf<GrammarRuleEntity?>(null) }
    var grammarContextMenu by remember { mutableStateOf<GrammarRuleEntity?>(null) }

    var phraseToEdit by remember { mutableStateOf<PhraseEntity?>(null) }
    var phraseToDelete by remember { mutableStateOf<PhraseEntity?>(null) }
    var phraseContextMenu by remember { mutableStateOf<PhraseEntity?>(null) }

    var questionToEdit by remember { mutableStateOf<QuestionEntity?>(null) }
    var questionToDelete by remember { mutableStateOf<QuestionEntity?>(null) }
    var questionContextMenu by remember { mutableStateOf<QuestionEntity?>(null) }

    if (editorialContextMenu && uiState.editorial != null) {
        val ed = uiState.editorial!!
        AlertDialog(
            onDismissRequest = { editorialContextMenu = false },
            title = { Text("Editorial Options", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(ed.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    TextButton(
                        onClick = {
                            editorialContextMenu = false
                            editingEditorial = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Edit Editorial")
                        }
                    }
                    TextButton(
                        onClick = {
                            editorialContextMenu = false
                            deletingEditorial = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Editorial", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { editorialContextMenu = false }) { Text("Close") }
            }
        )
    }

    wordContextMenu?.let { w ->
        AlertDialog(
            onDismissRequest = { wordContextMenu = null },
            title = { Text("Word Options", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(w.word, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Meaning: ${w.meaning}", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    TextButton(
                        onClick = {
                            wordToEdit = w
                            wordContextMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Edit Word")
                        }
                    }
                    TextButton(
                        onClick = {
                            wordToDelete = w
                            wordContextMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Word", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { wordContextMenu = null }) { Text("Close") }
            }
        )
    }

    grammarContextMenu?.let { r ->
        AlertDialog(
            onDismissRequest = { grammarContextMenu = null },
            title = { Text("Grammar Rule Options", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(r.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Rule: ${r.rule}", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    TextButton(
                        onClick = {
                            grammarToEdit = r
                            grammarContextMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Edit Rule")
                        }
                    }
                    TextButton(
                        onClick = {
                            grammarToDelete = r
                            grammarContextMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Rule", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { grammarContextMenu = null }) { Text("Close") }
            }
        )
    }

    phraseContextMenu?.let { p ->
        AlertDialog(
            onDismissRequest = { phraseContextMenu = null },
            title = { Text("Expression Options", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(p.phrase, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text("Meaning: ${p.meaning}", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    TextButton(
                        onClick = {
                            phraseToEdit = p
                            phraseContextMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Edit Expression")
                        }
                    }
                    TextButton(
                        onClick = {
                            phraseToDelete = p
                            phraseContextMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Expression", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { phraseContextMenu = null }) { Text("Close") }
            }
        )
    }

    questionContextMenu?.let { q ->
        AlertDialog(
            onDismissRequest = { questionContextMenu = null },
            title = { Text("Question Options", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(q.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    TextButton(
                        onClick = {
                            questionToEdit = q
                            questionContextMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Edit Question")
                        }
                    }
                    TextButton(
                        onClick = {
                            questionToDelete = q
                            questionContextMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Question", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { questionContextMenu = null }) { Text("Close") }
            }
        )
    }

    LaunchedEffect(date) {
        viewModel.loadDay(date)
    }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Daily Learning Flow", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = DateUtils.formatDate(date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
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
            // Horizontal Step Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LearnStep.values().forEach { step ->
                    val isCurrent = uiState.currentStep == step
                    val isPast = uiState.currentStep.stepNumber > step.stepNumber

                    Surface(
                        color = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isPast -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { viewModel.setStep(step) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${step.stepNumber}. ${step.title}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isCurrent -> MaterialTheme.colorScheme.onPrimary
                                    isPast -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Step Content (STRICTLY DAILY ONLY)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                when (uiState.currentStep) {
                    LearnStep.EDITORIAL -> StepEditorial(
                        uiState = uiState,
                        onOpenReader = { onNavigateToReader(uiState.editorial?.id ?: "ed_$date") },
                        onNext = { viewModel.setStep(LearnStep.VOCABULARY) },
                        onOpenContextMenu = { editorialContextMenu = true }
                    )
                    LearnStep.VOCABULARY -> StepVocabulary(
                        uiState = uiState,
                        onNextVocab = { viewModel.nextVocab() },
                        onPrevVocab = { viewModel.prevVocab() },
                        onMarkReviewed = { id, remembered -> viewModel.markWordReviewed(id, remembered) },
                        onOpenContextMenu = { wordContextMenu = it }
                    )
                    LearnStep.GRAMMAR -> StepGrammar(
                        uiState = uiState,
                        onNextRule = { viewModel.nextGrammar() },
                        onOpenContextMenu = { grammarContextMenu = it }
                    )
                    LearnStep.EXPRESSIONS -> StepExpressions(
                        uiState = uiState,
                        onNextPhrase = { viewModel.nextPhrase() },
                        onOpenContextMenu = { phraseContextMenu = it }
                    )
                    LearnStep.PRACTICE -> StepPractice(
                        uiState = uiState,
                        onSelectAnswer = { qId, ans -> viewModel.selectPracticeAnswer(qId, ans) },
                        onSubmit = { viewModel.submitPractice() },
                        onReattempt = { viewModel.reattemptPractice() },
                        onProceedToTest = { viewModel.setStep(LearnStep.TEST) },
                        onOpenContextMenu = { questionContextMenu = it }
                    )
                    LearnStep.TEST -> StepTestLaunch(
                        uiState = uiState,
                        onTakeTest = { onNavigateToTest("daily_$date") },
                        onSkipToReview = { viewModel.setStep(LearnStep.COMPLETE) }
                    )
                    LearnStep.COMPLETE -> StepComplete(
                        uiState = uiState,
                        onFinish = onNavigateBack
                    )
                }
            }
        }
    }

    // --- DIALOGS FOR EDIT & DELETE (TRIGGERED VIA LONG-PRESS OR ACTION BUTTONS) ---

    // 1. Editorial Edit Dialog
    if (editingEditorial && uiState.editorial != null) {
        val ed = uiState.editorial!!
        var title by remember(ed) { mutableStateOf(ed.title) }
        var source by remember(ed) { mutableStateOf(ed.source) }
        var content by remember(ed) { mutableStateOf(ed.contentMarkdown) }

        AlertDialog(
            onDismissRequest = { editingEditorial = false },
            title = { Text("Edit Today's Editorial") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = source,
                        onValueChange = { source = it },
                        label = { Text("Source") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content (Markdown)") },
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateEditorial(title, source, content)
                        editingEditorial = false
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEditorial = false }) { Text("Cancel") }
            }
        )
    }

    // 1. Editorial Delete Dialog
    if (deletingEditorial && uiState.editorial != null) {
        AlertDialog(
            onDismissRequest = { deletingEditorial = false },
            title = { Text("Delete Today's Editorial?") },
            text = { Text("Are you sure you want to delete this editorial? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEditorial()
                        deletingEditorial = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEditorial = false }) { Text("Cancel") }
            }
        )
    }

    // 2. Vocabulary Edit Dialog
    wordToEdit?.let { word ->
        var editWord by remember(word) { mutableStateOf(word.word) }
        var editPronunciation by remember(word) { mutableStateOf(word.pronunciation) }
        var editMeaning by remember(word) { mutableStateOf(word.meaning) }
        var editHindiMeaning by remember(word) { mutableStateOf(word.hindiMeaning) }
        var editExample by remember(word) { mutableStateOf(word.exampleSentence) }

        AlertDialog(
            onDismissRequest = { wordToEdit = null },
            title = { Text("Edit Vocabulary Word") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editWord,
                        onValueChange = { editWord = it },
                        label = { Text("Word") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPronunciation,
                        onValueChange = { editPronunciation = it },
                        label = { Text("Pronunciation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editMeaning,
                        onValueChange = { editMeaning = it },
                        label = { Text("English Meaning") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editHindiMeaning,
                        onValueChange = { editHindiMeaning = it },
                        label = { Text("Hindi Meaning") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editExample,
                        onValueChange = { editExample = it },
                        label = { Text("Example Sentence") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateWord(
                            word.copy(
                                word = editWord.trim(),
                                pronunciation = editPronunciation.trim(),
                                meaning = editMeaning.trim(),
                                hindiMeaning = editHindiMeaning.trim(),
                                exampleSentence = editExample.trim()
                            )
                        )
                        wordToEdit = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { wordToEdit = null }) { Text("Cancel") }
            }
        )
    }

    // 2. Vocabulary Delete Dialog
    wordToDelete?.let { word ->
        AlertDialog(
            onDismissRequest = { wordToDelete = null },
            title = { Text("Delete '${word.word}'?") },
            text = { Text("Do you want to delete this vocabulary word from today's list?") },
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
                TextButton(onClick = { wordToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // 3. Grammar Edit Dialog
    grammarToEdit?.let { rule ->
        var editTitle by remember(rule) { mutableStateOf(rule.title) }
        var editRule by remember(rule) { mutableStateOf(rule.rule) }
        var editExplanation by remember(rule) { mutableStateOf(rule.explanation) }

        AlertDialog(
            onDismissRequest = { grammarToEdit = null },
            title = { Text("Edit Grammar Rule") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Rule Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editRule,
                        onValueChange = { editRule = it },
                        label = { Text("Grammar Rule Statement") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                    OutlinedTextField(
                        value = editExplanation,
                        onValueChange = { editExplanation = it },
                        label = { Text("Explanation") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateGrammarRule(
                            rule.copy(
                                title = editTitle.trim(),
                                rule = editRule.trim(),
                                explanation = editExplanation.trim()
                            )
                        )
                        grammarToEdit = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { grammarToEdit = null }) { Text("Cancel") }
            }
        )
    }

    // 3. Grammar Delete Dialog
    grammarToDelete?.let { rule ->
        AlertDialog(
            onDismissRequest = { grammarToDelete = null },
            title = { Text("Delete Grammar Rule?") },
            text = { Text("Do you want to delete '${rule.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGrammarRule(rule.id)
                        grammarToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { grammarToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // 4. Expression Edit Dialog
    phraseToEdit?.let { phrase ->
        var editPhrase by remember(phrase) { mutableStateOf(phrase.phrase) }
        var editMeaning by remember(phrase) { mutableStateOf(phrase.meaning) }
        var editHindiMeaning by remember(phrase) { mutableStateOf(phrase.hindiMeaning) }
        var editExample by remember(phrase) { mutableStateOf(phrase.example) }

        AlertDialog(
            onDismissRequest = { phraseToEdit = null },
            title = { Text("Edit Expression / Idiom") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editPhrase,
                        onValueChange = { editPhrase = it },
                        label = { Text("Expression / Phrase") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editMeaning,
                        onValueChange = { editMeaning = it },
                        label = { Text("Meaning") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editHindiMeaning,
                        onValueChange = { editHindiMeaning = it },
                        label = { Text("Hindi Meaning") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editExample,
                        onValueChange = { editExample = it },
                        label = { Text("Example Usage") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePhrase(
                            phrase.copy(
                                phrase = editPhrase.trim(),
                                meaning = editMeaning.trim(),
                                hindiMeaning = editHindiMeaning.trim(),
                                example = editExample.trim()
                            )
                        )
                        phraseToEdit = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { phraseToEdit = null }) { Text("Cancel") }
            }
        )
    }

    // 4. Expression Delete Dialog
    phraseToDelete?.let { phrase ->
        AlertDialog(
            onDismissRequest = { phraseToDelete = null },
            title = { Text("Delete Expression?") },
            text = { Text("Do you want to delete '${phrase.phrase}' from today's list?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePhrase(phrase.id)
                        phraseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { phraseToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // 5. Question Edit Dialog
    questionToEdit?.let { q ->
        var editQuestion by remember(q) { mutableStateOf(q.question) }
        var editExplanation by remember(q) { mutableStateOf(q.explanation) }
        var opt0 by remember(q) { mutableStateOf(q.options.getOrNull(0) ?: "") }
        var opt1 by remember(q) { mutableStateOf(q.options.getOrNull(1) ?: "") }
        var opt2 by remember(q) { mutableStateOf(q.options.getOrNull(2) ?: "") }
        var opt3 by remember(q) { mutableStateOf(q.options.getOrNull(3) ?: "") }
        var correctIdx by remember(q) { mutableIntStateOf(q.correctAnswerIndex) }

        AlertDialog(
            onDismissRequest = { questionToEdit = null },
            title = { Text("Edit Practice Question") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editQuestion,
                        onValueChange = { editQuestion = it },
                        label = { Text("Question Text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = opt0,
                        onValueChange = { opt0 = it },
                        label = { Text("Option 1 ${if (correctIdx == 0) "(Correct ✓)" else ""}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = opt1,
                        onValueChange = { opt1 = it },
                        label = { Text("Option 2 ${if (correctIdx == 1) "(Correct ✓)" else ""}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = opt2,
                        onValueChange = { opt2 = it },
                        label = { Text("Option 3 ${if (correctIdx == 2) "(Correct ✓)" else ""}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = opt3,
                        onValueChange = { opt3 = it },
                        label = { Text("Option 4 ${if (correctIdx == 3) "(Correct ✓)" else ""}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editExplanation,
                        onValueChange = { editExplanation = it },
                        label = { Text("Explanation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val opts = listOf(opt0, opt1, opt2, opt3).filter { it.isNotBlank() }
                        viewModel.updateQuestion(
                            q.copy(
                                question = editQuestion.trim(),
                                options = opts,
                                correctAnswerIndex = correctIdx.coerceIn(0, (opts.size - 1).coerceAtLeast(0)),
                                explanation = editExplanation.trim()
                            )
                        )
                        questionToEdit = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { questionToEdit = null }) { Text("Cancel") }
            }
        )
    }

    // 5. Question Delete Dialog
    questionToDelete?.let { q ->
        AlertDialog(
            onDismissRequest = { questionToDelete = null },
            title = { Text("Delete Question?") },
            text = { Text("Do you want to delete this practice question?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteQuestion(q.id)
                        questionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { questionToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StepEditorial(
    uiState: DailyLearnUiState,
    onOpenReader: () -> Unit,
    onNext: () -> Unit,
    onOpenContextMenu: () -> Unit
) {
    val editorial = uiState.editorial
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Step 1: Read Today's Editorial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Long press card for Edit / Delete options.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (editorial != null) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = onOpenReader,
                        onLongClick = onOpenContextMenu
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = editorial.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(text = "${editorial.readTimeMinutes} min read", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = editorial.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = editorial.contentMarkdown.take(300).replace("#", "").trim() + "...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOpenReader,
                        modifier = Modifier.fillMaxWidth().testTag("open_editorial_reader_flow_btn")
                    ) {
                        Text("Open Full Reader & Highlighting")
                    }
                }
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No Editorial for Today Yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Paste today's editorial in Content Manager to begin today's daily flow.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().testTag("next_to_vocab_btn")
        ) {
            Text("Proceed to Step 2: Vocabulary (${uiState.vocabulary.size} words) →")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StepVocabulary(
    uiState: DailyLearnUiState,
    onNextVocab: () -> Unit,
    onPrevVocab: () -> Unit,
    onMarkReviewed: (String, Boolean) -> Unit,
    onOpenContextMenu: (VocabularyEntity) -> Unit
) {
    val words = uiState.vocabulary
    if (words.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No vocabulary found for today.", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Import today's package or vocabulary to start learning.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val index = uiState.activeVocabIndex.coerceIn(0, words.size - 1)
    val word = words[index]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Word ${index + 1} of ${words.size}", style = MaterialTheme.typography.labelLarge)
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = word.learningStatus,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        LinearProgressIndicator(
            progress = { (index + 1).toFloat() / words.size.toFloat() },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // Flashcard Presentation with Long Press Support
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { /* Flip or read */ },
                    onLongClick = { onOpenContextMenu(word) }
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Hold to edit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                if (word.pronunciation.isNotBlank()) {
                    Text(text = word.pronunciation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(text = word.meaning, style = MaterialTheme.typography.bodyLarge)
                if (word.hindiMeaning.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hindi: ${word.hindiMeaning}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (word.exampleSentence.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "“${word.exampleSentence}”",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onPrevVocab,
                enabled = index > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text("Previous")
            }
            Button(
                onClick = {
                    onMarkReviewed(word.id, true)
                    onNextVocab()
                },
                modifier = Modifier.weight(1.3f).testTag("next_vocab_btn")
            ) {
                Text(if (index == words.size - 1) "Complete Words →" else "Next Word →")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StepGrammar(
    uiState: DailyLearnUiState,
    onNextRule: () -> Unit,
    onOpenContextMenu: (GrammarRuleEntity) -> Unit
) {
    val rules = uiState.grammarRules
    if (rules.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No grammar rules for today.", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Import today's grammar rules to practice.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val index = uiState.activeGrammarIndex.coerceIn(0, rules.size - 1)
    val rule = rules[index]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Rule ${index + 1} of ${rules.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Hold card for options", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = { onOpenContextMenu(rule) }
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = rule.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Rule: ${rule.rule}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(12.dp))
                }

                if (rule.explanation.isNotBlank()) {
                    Text(text = rule.explanation, style = MaterialTheme.typography.bodyMedium)
                }

                if (rule.correctExamples.isNotEmpty()) {
                    Text("✓ Correct Usage:", style = MaterialTheme.typography.labelLarge, color = Color(0xFF22543D))
                    rule.correctExamples.forEach { ex ->
                        Text("• $ex", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF22543D))
                    }
                }

                if (rule.incorrectExamples.isNotEmpty()) {
                    Text("✗ Common Error:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                    rule.incorrectExamples.forEach { ex ->
                        Text("• $ex", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Button(onClick = onNextRule, modifier = Modifier.fillMaxWidth().testTag("next_grammar_btn")) {
            Text(if (index == rules.size - 1) "Complete Grammar →" else "Next Grammar Rule →")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StepExpressions(
    uiState: DailyLearnUiState,
    onNextPhrase: () -> Unit,
    onOpenContextMenu: (PhraseEntity) -> Unit
) {
    val phrases = uiState.phrases
    if (phrases.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No expressions recorded for today.", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Import today's phrases & idioms to study.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val index = uiState.activePhraseIndex.coerceIn(0, phrases.size - 1)
    val phrase = phrases[index]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Expression ${index + 1} of ${phrases.size}", style = MaterialTheme.typography.titleMedium)
            Text("Hold card for options", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = { onOpenContextMenu(phrase) }
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = phrase.phrase,
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(text = "Meaning: ${phrase.meaning}", style = MaterialTheme.typography.bodyLarge)
                if (phrase.hindiMeaning.isNotBlank()) {
                    Text(text = "Hindi: ${phrase.hindiMeaning}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                }
                if (phrase.example.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "“${phrase.example}”", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }

        Button(onClick = onNextPhrase, modifier = Modifier.fillMaxWidth().testTag("next_expression_btn")) {
            Text(if (index == phrases.size - 1) "Proceed to Practice →" else "Next Expression →")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StepPractice(
    uiState: DailyLearnUiState,
    onSelectAnswer: (String, Int) -> Unit,
    onSubmit: () -> Unit,
    onReattempt: () -> Unit,
    onProceedToTest: () -> Unit,
    onOpenContextMenu: (QuestionEntity) -> Unit
) {
    val questions = uiState.questions
    if (questions.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No practice questions for today.", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Import questions for today to practice.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step 5: Practice Questions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (uiState.practiceSubmitted) {
                FilledTonalButton(
                    onClick = onReattempt,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reattempt")
                }
            }
        }

        // Previous Report Banner if submitted
        if (uiState.practiceSubmitted) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Practice Result Report",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val accuracyPercent = if (questions.isNotEmpty()) {
                                (uiState.practiceScore.toFloat() / questions.size) * 100f
                            } else 0f
                            Text(
                                text = "Score: ${uiState.practiceScore}/${questions.size} (${accuracyPercent.toInt()}% Accuracy)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            color = if (uiState.practiceScore >= (questions.size / 2)) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (uiState.practiceScore >= (questions.size / 2)) "COMPLETED" else "NEEDS REVIEW",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        questions.forEachIndexed { qIdx, q ->
            val selectedAns = uiState.practiceAnswers[q.id]
            val isAnswered = selectedAns != null
            val isCorrect = selectedAns == q.correctAnswerIndex

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { onOpenContextMenu(q) }
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = q.topic.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (uiState.practiceSubmitted) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Correct",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Correct",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                } else if (isAnswered) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Incorrect",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Incorrect",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text(
                                        text = "Skipped",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Q${qIdx + 1}. ${q.question}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    q.options.forEachIndexed { optIdx, opt ->
                        val isSelected = selectedAns == optIdx
                        val isCorrectOption = optIdx == q.correctAnswerIndex

                        val containerColor = when {
                            uiState.practiceSubmitted && isCorrectOption -> Color(0xFF22543D).copy(alpha = 0.2f)
                            uiState.practiceSubmitted && isSelected && !isCorrectOption -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }

                        Surface(
                            color = containerColor,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !uiState.practiceSubmitted) {
                                    onSelectAnswer(q.id, optIdx)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (uiState.practiceSubmitted) {
                                    if (isCorrectOption) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.practiceSubmitted && q.explanation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 Explanation: ${q.explanation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        if (!uiState.practiceSubmitted) {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth().testTag("submit_practice_btn")
            ) {
                Text("Check Answers & Save Result")
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Practice Complete: ${uiState.practiceScore}/${uiState.questions.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReattempt,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reattempt")
                        }
                        Button(
                            onClick = onProceedToTest,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Daily Test →")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepTestLaunch(
    uiState: DailyLearnUiState,
    onTakeTest: () -> Unit,
    onSkipToReview: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Step 6: Daily English Test", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Evaluate your retention across vocabulary, grammar, and comprehension under timed exam conditions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onTakeTest,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.8f).testTag("launch_daily_test_flow_btn")
        ) {
            Text("Launch Timed Daily Test")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onSkipToReview) {
            Text("Skip to Session Summary")
        }
    }
}

@Composable
fun StepComplete(
    uiState: DailyLearnUiState,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🎉", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Today's Learning Complete!", style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Outstanding commitment. Your knowledge base has expanded.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Editorial Read:")
                    Text("Completed ✓", fontWeight = FontWeight.Bold, color = Color(0xFF22543D))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Words Studied:")
                    Text("${uiState.vocabulary.size} words", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Grammar Rules:")
                    Text("${uiState.grammarRules.size} concepts", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Expressions Learned:")
                    Text("${uiState.phrases.size} idioms", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Return to Dashboard")
        }
    }
}
