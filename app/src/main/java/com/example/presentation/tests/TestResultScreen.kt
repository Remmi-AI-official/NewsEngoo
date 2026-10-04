package com.example.presentation.tests

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.UserAnswerEvaluation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultScreen(
    attemptId: String,
    onNavigateBack: () -> Unit,
    onNavigateToMistakes: () -> Unit,
    viewModel: TestViewModel = viewModel()
) {
    val activeState by viewModel.activeTestState.collectAsState()
    val result = activeState.result

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Test Evaluation Analysis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (result != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Score Summary Card
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${result.score} / ${result.totalQuestions}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (result.accuracy >= 70f) Color(0xFF22543D) else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Score: ${result.accuracy.toInt()}% Accuracy",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Time Taken: ${result.timeSpentSeconds / 60}m ${result.timeSpentSeconds % 60}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            LinearProgressIndicator(
                                progress = { result.accuracy / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = if (result.accuracy >= 70f) Color(0xFF22543D) else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Topic Performance Breakdown
                item {
                    Text(
                        text = "Topic-wise Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            result.topicScores.forEach { (topic, pair) ->
                                val pct = if (pair.second > 0) (pair.first.toFloat() / pair.second.toFloat()) * 100f else 0f
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = topic.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${pair.first}/${pair.second} (${pct.toInt()}%)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pct >= 70f) Color(0xFF22543D) else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // Action to Mistake Book
                item {
                    val wrongCount = result.totalQuestions - result.score
                    if (wrongCount > 0) {
                        Button(
                            onClick = onNavigateToMistakes,
                            modifier = Modifier.fillMaxWidth().testTag("review_mistakes_btn")
                        ) {
                            Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Review $wrongCount Mistakes in Mistake Book")
                        }
                    }
                }

                // Detailed Answers Review
                item {
                    Text(
                        text = "Question-by-Question Review",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(result.detailedAnswers) { ans ->
                    QuestionReviewItem(ans = ans)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Return to Tests")
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = onNavigateBack) {
                    Text("Return to Tests")
                }
            }
        }
    }
}

@Composable
fun QuestionReviewItem(ans: UserAnswerEvaluation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ans.isCorrect) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (ans.isCorrect) Color(0xFF22543D).copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (ans.isCorrect) "CORRECT" else "INCORRECT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (ans.isCorrect) Color(0xFF22543D) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = ans.topic.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ans.questionText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!ans.isCorrect) {
                Text(
                    text = "Your Answer: Option ${ans.selectedOptionIndex + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Text(
                text = "Correct Answer: Option ${ans.correctOptionIndex + 1}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22543D)
            )

            if (ans.explanation.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = "Explanation: ${ans.explanation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
