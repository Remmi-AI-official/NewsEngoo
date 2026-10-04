package com.example.presentation.reader

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.components.MarkdownEditorialView
import com.example.presentation.components.VocabWordBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorialReaderScreen(
    editorialId: String,
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (String) -> Unit,
    onPracticeWord: (String) -> Unit,
    viewModel: EditorialReaderViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(editorialId) {
        viewModel.loadEditorial(editorialId)
    }

    // Restore reading position when loaded
    LaunchedEffect(uiState.editorial) {
        val pos = uiState.editorial?.readingPosition ?: 0
        if (pos > 0 && scrollState.value == 0) {
            scrollState.scrollTo(pos)
        }
    }

    // Save reading position when exiting
    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveScrollPosition(scrollState.value)
        }
    }

    BackHandler {
        viewModel.saveScrollPosition(scrollState.value)
        onNavigateBack()
    }

    val readingProgress by remember {
        derivedStateOf {
            if (scrollState.maxValue > 0) {
                scrollState.value.toFloat() / scrollState.maxValue.toFloat()
            } else 0f
        }
    }

    var showNoteDialog by remember { mutableStateOf(false) }
    var noteInput by remember(uiState.editorial?.personalNote) {
        mutableStateOf(uiState.editorial?.personalNote ?: "")
    }

    val editorial = uiState.editorial

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = editorial?.title ?: "Editorial Reader",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.saveScrollPosition(scrollState.value)
                            onNavigateBack()
                        }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Font Size Decrease
                        IconButton(onClick = { viewModel.adjustFontSize(-1f) }) {
                            Text("A-", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        // Font Size Increase
                        IconButton(onClick = { viewModel.adjustFontSize(1f) }) {
                            Text("A+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        // Favorite
                        IconButton(
                            onClick = { viewModel.toggleFavorite() },
                            modifier = Modifier.testTag("reader_favorite_btn")
                        ) {
                            Icon(
                                imageVector = if (editorial?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (editorial?.isFavorite == true) Color(0xFFE53E3E) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Share
                        IconButton(onClick = {
                            if (editorial != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, editorial.title)
                                    putExtra(Intent.EXTRA_TEXT, "${editorial.title}\n\n${editorial.contentMarkdown}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Editorial"))
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // Reading Progress Bar
                LinearProgressIndicator(
                    progress = { readingProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    ) { innerPadding ->
        if (editorial != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                // Editorial Meta Header
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = editorial.source,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Date: ${editorial.date} • ${editorial.readTimeMinutes} min read",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showNoteDialog = !showNoteDialog }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Notes",
                                    tint = if (editorial.personalNote.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Personal Note Box (if expanded or note present)
                if (showNoteDialog) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Editorial Personal Note", style = MaterialTheme.typography.titleSmall)
                            OutlinedTextField(
                                value = noteInput,
                                onValueChange = { noteInput = it },
                                placeholder = { Text("Add takeaways, key insights...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(onClick = {
                                    viewModel.saveArticleNote(noteInput)
                                    showNoteDialog = false
                                }) {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }

                // Markdown Body
                MarkdownEditorialView(
                    markdownText = editorial.contentMarkdown,
                    fontSizeSp = uiState.readerSettings.fontSizeSp,
                    lineSpacingMultiplier = uiState.readerSettings.lineSpacingMultiplier,
                    bookmarkedParagraph = editorial.bookmarkParagraph,
                    vocabularyWords = uiState.knownWords.map { it.id }.toSet(),
                    onWordClick = { word -> viewModel.selectWord(word) },
                    onBookmarkParagraph = { para -> viewModel.bookmarkParagraph(para) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Complete Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (editorial.isCompleted) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Editorial Completed",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.markCompleted() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mark_editorial_complete_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark Editorial as Read")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Vocabulary Word Bottom Sheet when a word is tapped in the article
        VocabWordBottomSheet(
            word = uiState.selectedWordForSheet,
            onDismiss = { viewModel.dismissWordSheet() },
            onToggleFavorite = { wordId, fav -> viewModel.toggleWordFavorite(wordId, fav) },
            onSaveNote = { wordId, note -> viewModel.saveWordNote(wordId, note) },
            onViewFullDetail = { wordId ->
                viewModel.dismissWordSheet()
                onNavigateToWordDetail(wordId)
            },
            onPracticeWord = { wordId ->
                viewModel.dismissWordSheet()
                onPracticeWord(wordId)
            }
        )
    }
}
