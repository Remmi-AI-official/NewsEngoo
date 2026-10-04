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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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

    var wordToEdit by remember { mutableStateOf<VocabularyEntity?>(null) }
    var wordToDelete by remember { mutableStateOf<VocabularyEntity?>(null) }

    var grammarToEdit by remember { mutableStateOf<GrammarRuleEntity?>(null) }
    var grammarToDelete by remember { mutableStateOf<GrammarRuleEntity?>(null) }

    var phraseToEdit by remember { mutableStateOf<PhraseEntity?>(null) }
    var phraseToDelete by remember { mutableStateOf<PhraseEntity?>(null) }

    var questionToEdit by remember { mutableStateOf<QuestionEntity?>(null) }
    var questionToDelete by remember { mutableStateOf<QuestionEntity?>(null) }

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
                        onEditEditorial = { editingEditorial = true },
                        onDeleteEditorial = { deletingEditorial = true }
                    )
                    LearnStep.VOCABULARY -> StepVocabulary(
                        uiState = uiState,
                        onNextVocab = { viewModel.nextVocab() },
                        onPrevVocab = { viewModel.prevVocab() },
                        onMarkReviewed = { id, remembered -> viewModel.markWordReviewed(id, remembered) },
                        onEditWord = { wordToEdit = it },
                        onDeleteWord = { wordToDelete = it }
                    )
                    LearnStep.GRAMMAR -> StepGrammar(
                        uiState = uiState,
                        onNextRule = { viewModel.nextGrammar() },
                        onEditGrammar = { grammarToEdit = it },
                        onDeleteGrammar = { grammarToDelete = it }
                    )
                    LearnStep.EXPRESSIONS -> StepExpressions(
                        uiState = uiState,
                        onNextPhrase = { viewModel.nextPhrase() },
                        onEditPhrase = { phraseToEdit = it },
                        onDeletePhrase = { phraseToDelete = it }
                    )
                    LearnStep.PRACTICE -> StepPractice(
                        uiState = uiState,
                        onSelectAnswer = { qId, ans -> viewModel.selectPracticeAnswer(qId, ans) },
                        onSubmit = { viewModel.submitPractice() },
                        onProceedToTest = { viewModel.setStep(LearnStep.TEST) },
                        onEditQuestion = { questionToEdit = it },
                        onDeleteQuestion = { questionToDelete = it }
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
    onEditEditorial: () -> Unit,
    onDeleteEditorial: () -> Unit
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
                        onLongClick = onEditEditorial
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
                        Row {
                            IconButton(onClick = onEditEditorial, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Editorial", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = onDeleteEditorial, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Editorial", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
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
    onEditWord: (VocabularyEntity) -> Unit,
    onDeleteWord: (VocabularyEntity) -> Unit
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                IconButton(onClick = { onEditWord(word) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Word", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { onDeleteWord(word) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Word", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
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
                    onLongClick = { onEditWord(word) }
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
    onEditGrammar: (GrammarRuleEntity) -> Unit,
    onDeleteGrammar: (GrammarRuleEntity) -> Unit
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
            Row {
                IconButton(onClick = { onEditGrammar(rule) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Rule", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { onDeleteGrammar(rule) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Rule", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = { onEditGrammar(rule) }
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
    onEditPhrase: (PhraseEntity) -> Unit,
    onDeletePhrase: (PhraseEntity) -> Unit
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
            Row {
                IconButton(onClick = { onEditPhrase(phrase) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Phrase", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { onDeletePhrase(phrase) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Phrase", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = { onEditPhrase(phrase) }
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
    onProceedToTest: () -> Unit,
    onEditQuestion: (QuestionEntity) -> Unit,
    onDeleteQuestion: (QuestionEntity) -> Unit
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
        Text(
            text = "Step 5: Practice Questions (Hold card to edit/delete)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        questions.forEachIndexed { qIdx, q ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { onEditQuestion(q) }
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
                        Text(
                            text = "Q${qIdx + 1}. ${q.question}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            IconButton(onClick = { onEditQuestion(q) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Question", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onDeleteQuestion(q) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Question", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    val selectedAns = uiState.practiceAnswers[q.id]
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
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }

                    if (uiState.practiceSubmitted && q.explanation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Explanation: ${q.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (!uiState.practiceSubmitted) {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth().testTag("submit_practice_btn")
            ) {
                Text("Check Answers")
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Practice Complete: ${uiState.practiceScore}/${uiState.questions.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onProceedToTest) {
                        Text("Proceed to Daily Test →")
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
