package com.example.presentation.tests

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeTestScreen(
    testId: String,
    onNavigateBack: () -> Unit,
    onTestFinished: (String) -> Unit,
    viewModel: TestViewModel = viewModel()
) {
    val activeState by viewModel.activeTestState.collectAsState()
    var showConfirmSubmit by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(900) }

    LaunchedEffect(testId) {
        viewModel.loadTest(testId)
    }

    // Countdown Timer
    LaunchedEffect(activeState.test) {
        timerSeconds = (activeState.test?.durationMinutes ?: 15) * 60
        while (timerSeconds > 0 && !activeState.isSubmitted) {
            delay(1000)
            timerSeconds--
        }
        if (timerSeconds <= 0 && !activeState.isSubmitted) {
            viewModel.submitActiveTest()
        }
    }

    // If submitted, navigate to result
    LaunchedEffect(activeState.isSubmitted, activeState.result) {
        val result = activeState.result
        if (activeState.isSubmitted && result != null) {
            onTestFinished(result.attemptId)
        }
    }

    BackHandler {
        showConfirmSubmit = true
    }

    val questions = activeState.questions
    val currentIndex = activeState.currentQuestionIndex.coerceIn(0, (questions.size - 1).coerceAtLeast(0))
    val currentQuestion = if (questions.isNotEmpty()) questions[currentIndex] else null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = activeState.test?.title ?: "Test Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showConfirmSubmit = true }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Test")
                    }
                },
                actions = {
                    // Timer Display
                    val minutes = timerSeconds / 60
                    val seconds = timerSeconds % 60
                    Surface(
                        color = if (timerSeconds < 120) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (timerSeconds < 120) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format("%02d:%02d", minutes, seconds),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (timerSeconds < 120) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (currentQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Horizontal Question Ribbon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    questions.forEachIndexed { idx, q ->
                        val isCurrent = idx == currentIndex
                        val isAnswered = activeState.userAnswers.containsKey(q.id)

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCurrent -> MaterialTheme.colorScheme.primary
                                        isAnswered -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .clickable { viewModel.navigateToQuestion(idx) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isCurrent || isAnswered -> Color.White
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Question Card
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
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = currentQuestion.topic.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "Question ${currentIndex + 1} of ${questions.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentQuestion.question,
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Options
                        val selectedOpt = activeState.userAnswers[currentQuestion.id]
                        currentQuestion.options.forEachIndexed { optIdx, optText ->
                            val isSelected = selectedOpt == optIdx
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable { viewModel.selectAnswer(currentQuestion.id, optIdx) }
                                    .testTag("option_${currentQuestion.id}_$optIdx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val letter = ('A'.code + optIdx).toChar()
                                    Surface(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$letter",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = optText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Navigation Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateToQuestion(currentIndex - 1) },
                        enabled = currentIndex > 0
                    ) {
                        Text("Previous")
                    }

                    if (currentIndex < questions.size - 1) {
                        Button(
                            onClick = { viewModel.navigateToQuestion(currentIndex + 1) },
                            modifier = Modifier.testTag("next_question_btn")
                        ) {
                            Text("Next Question")
                        }
                    } else {
                        Button(
                            onClick = { showConfirmSubmit = true },
                            modifier = Modifier.testTag("submit_test_btn")
                        ) {
                            Text("Submit Test")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Confirmation Dialog
        if (showConfirmSubmit) {
            val answeredCount = activeState.userAnswers.size
            val totalCount = questions.size

            AlertDialog(
                onDismissRequest = { showConfirmSubmit = false },
                title = { Text("Submit Examination?") },
                text = {
                    Text("You have answered $answeredCount out of $totalCount questions. Are you ready to submit and calculate your score?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmSubmit = false
                            viewModel.submitActiveTest()
                        }
                    ) {
                        Text("Yes, Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmSubmit = false }) {
                        Text("Continue Test")
                    }
                }
            )
        }
    }
}
