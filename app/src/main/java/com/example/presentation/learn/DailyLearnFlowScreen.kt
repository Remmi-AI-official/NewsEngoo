package com.example.presentation.learn

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLearnFlowScreen(
    date: String,
    onNavigateBack: () -> Unit,
    onNavigateToReader: (String) -> Unit,
    onNavigateToTest: (String) -> Unit,
    viewModel: DailyLearnViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(date) {
        viewModel.loadDay(date)
    }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Daily Learning Flow", style = MaterialTheme.typography.titleMedium)
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Step Content
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
                when (uiState.currentStep) {
                    LearnStep.EDITORIAL -> StepEditorial(
                        uiState = uiState,
                        onOpenReader = { onNavigateToReader(uiState.editorial?.id ?: "ed_$date") },
                        onNext = { viewModel.setStep(LearnStep.VOCABULARY) }
                    )
                    LearnStep.VOCABULARY -> StepVocabulary(
                        uiState = uiState,
                        onNextVocab = { viewModel.nextVocab() },
                        onPrevVocab = { viewModel.prevVocab() },
                        onMarkReviewed = { id, remembered -> viewModel.markWordReviewed(id, remembered) }
                    )
                    LearnStep.GRAMMAR -> StepGrammar(
                        uiState = uiState,
                        onNextRule = { viewModel.nextGrammar() }
                    )
                    LearnStep.EXPRESSIONS -> StepExpressions(
                        uiState = uiState,
                        onNextPhrase = { viewModel.nextPhrase() }
                    )
                    LearnStep.PRACTICE -> StepPractice(
                        uiState = uiState,
                        onSelectAnswer = { qId, ans -> viewModel.selectPracticeAnswer(qId, ans) },
                        onSubmit = { viewModel.submitPractice() },
                        onProceedToTest = { viewModel.setStep(LearnStep.TEST) }
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
}

@Composable
fun StepEditorial(
    uiState: DailyLearnUiState,
    onOpenReader: () -> Unit,
    onNext: () -> Unit
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
                    Text("Immerse yourself in context before examining vocabulary and rules.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (editorial != null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = editorial.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
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

@Composable
fun StepVocabulary(
    uiState: DailyLearnUiState,
    onNextVocab: () -> Unit,
    onPrevVocab: () -> Unit,
    onMarkReviewed: (String, Boolean) -> Unit
) {
    val words = uiState.vocabulary
    if (words.isEmpty()) {
        Text("No vocabulary found for today.")
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

        // Flashcard Presentation
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
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

@Composable
fun StepGrammar(
    uiState: DailyLearnUiState,
    onNextRule: () -> Unit
) {
    val rules = uiState.grammarRules
    if (rules.isEmpty()) {
        Text("No grammar rules for today.")
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
        Text("Rule ${index + 1} of ${rules.size}: ${rule.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

@Composable
fun StepExpressions(
    uiState: DailyLearnUiState,
    onNextPhrase: () -> Unit
) {
    val phrases = uiState.phrases
    if (phrases.isEmpty()) {
        Text("No expressions recorded for today.")
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
        Text("Expression ${index + 1} of ${phrases.size}", style = MaterialTheme.typography.titleMedium)

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
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

@Composable
fun StepPractice(
    uiState: DailyLearnUiState,
    onSelectAnswer: (String, Int) -> Unit,
    onSubmit: () -> Unit,
    onProceedToTest: () -> Unit
) {
    val questions = uiState.questions
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step 5: Practice Questions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        questions.forEachIndexed { qIdx, q ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Q${qIdx + 1}. ${q.question}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
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
