package com.example.presentation.learn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.EditorialApplication
import com.example.data.local.entity.VocabularyEntity
import com.example.domain.model.DateUtils
import com.example.presentation.components.EmptyStateView
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RevisionViewModel(private val app: EditorialApplication) : ViewModel() {
    private val vocabRepo = app.vocabularyRepository
    val todayDate = DateUtils.getTodayDate()

    val dueWords: StateFlow<List<VocabularyEntity>> = vocabRepo.getWordsDueForRevision(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun recordReview(wordId: String, isCorrect: Boolean) {
        viewModelScope.launch {
            vocabRepo.recordReview(wordId, isCorrect, todayDate)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionScreen(
    onNavigateBack: () -> Unit,
    app: EditorialApplication
) {
    val viewModel = remember { RevisionViewModel(app) }
    val dueWords by viewModel.dueWords.collectAsState()

    var currentIndex by remember { mutableIntStateOf(0) }
    var isRevealed by remember { mutableStateOf(false) }
    var reviewedCount by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Spaced Revision Due") },
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (dueWords.isEmpty() || currentIndex >= dueWords.size) {
                // All reviewed
                EmptyStateView(
                    icon = Icons.Default.CheckCircle,
                    title = "Revision Complete!",
                    message = if (reviewedCount > 0) {
                        "Excellent work! You reviewed $reviewedCount vocabulary words today. Your retention schedule is updated."
                    } else {
                        "No words are currently due for revision. Enjoy your day or explore new editorials!"
                    },
                    actionButtonText = "Back to Home",
                    onActionClick = onNavigateBack
                )
            } else {
                val currentWord = dueWords[currentIndex]

                // Progress Bar & Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Card ${currentIndex + 1} of ${dueWords.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Level: ${currentWord.learningStatus}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / dueWords.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Flashcard
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentWord.word,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (currentWord.pronunciation.isNotBlank()) {
                            Text(
                                text = currentWord.pronunciation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (!isRevealed) {
                            Button(
                                onClick = { isRevealed = true },
                                modifier = Modifier.testTag("reveal_meaning_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reveal Meaning & Usage")
                            }
                        } else {
                            // Revealed Content
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Text(
                                text = currentWord.meaning,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (currentWord.hindiMeaning.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Hindi: ${currentWord.hindiMeaning}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            if (currentWord.exampleSentence.isNotBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "“${currentWord.exampleSentence}”",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontStyle = FontStyle.Italic,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rating Buttons when revealed
                if (isRevealed) {
                    Text(
                        text = "How well did you recall this word?",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.recordReview(currentWord.id, false)
                                reviewedCount++
                                isRevealed = false
                                currentIndex++
                            },
                            modifier = Modifier.weight(1f).testTag("forgot_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Forgot (Review Soon)")
                        }

                        Button(
                            onClick = {
                                viewModel.recordReview(currentWord.id, true)
                                reviewedCount++
                                isRevealed = false
                                currentIndex++
                            },
                            modifier = Modifier.weight(1f).testTag("recalled_btn")
                        ) {
                            Text("Recalled Well ✓")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
