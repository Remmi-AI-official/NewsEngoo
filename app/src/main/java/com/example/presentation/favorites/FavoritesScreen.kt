package com.example.presentation.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.DateUtils
import com.example.presentation.components.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReader: (String) -> Unit,
    onNavigateToWordDetail: (String) -> Unit,
    viewModel: FavoritesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Starred & Favorites") },
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
                .padding(horizontal = 20.dp)
        ) {
            // Horizontal Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FavoriteTab.values().forEach { tab ->
                    FilterChip(
                        selected = uiState.currentTab == tab,
                        onClick = { viewModel.setTab(tab) },
                        label = {
                            val count = when (tab) {
                                FavoriteTab.ALL -> uiState.favoriteEditorials.size + uiState.favoriteWords.size + uiState.favoriteRules.size + uiState.favoritePhrases.size + uiState.favoriteQuestions.size
                                FavoriteTab.ARTICLES -> uiState.favoriteEditorials.size
                                FavoriteTab.WORDS -> uiState.favoriteWords.size
                                FavoriteTab.GRAMMAR -> uiState.favoriteRules.size
                                FavoriteTab.PHRASES -> uiState.favoritePhrases.size
                                FavoriteTab.QUESTIONS -> uiState.favoriteQuestions.size
                            }
                            Text("${tab.label} ($count)")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val showAll = uiState.currentTab == FavoriteTab.ALL
            val totalItems = uiState.favoriteEditorials.size + uiState.favoriteWords.size + uiState.favoriteRules.size + uiState.favoritePhrases.size + uiState.favoriteQuestions.size

            if (totalItems == 0) {
                EmptyStateView(
                    icon = Icons.Default.Star,
                    title = "No Favorites Yet",
                    message = "Tap the ⭐ icon on any editorial, vocabulary word, grammar rule, or idiom to preserve it here for rapid access."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Articles
                    if (showAll || uiState.currentTab == FavoriteTab.ARTICLES) {
                        items(uiState.favoriteEditorials, key = { "ed_${it.id}" }) { ed ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToReader(ed.id) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("ARTICLE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text(ed.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        Text("${ed.source} • ${DateUtils.formatDate(ed.date)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { viewModel.removeFavoriteEditorial(ed.id) }) {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFE53E3E))
                                    }
                                }
                            }
                        }
                    }

                    // Words
                    if (showAll || uiState.currentTab == FavoriteTab.WORDS) {
                        items(uiState.favoriteWords, key = { "w_${it.id}" }) { word ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToWordDetail(word.id) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("VOCABULARY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                        Text(word.word, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(word.meaning, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                        if (word.hindiMeaning.isNotBlank()) {
                                            Text("Hindi: ${word.hindiMeaning}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                    IconButton(onClick = { viewModel.removeFavoriteWord(word.id) }) {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFE53E3E))
                                    }
                                }
                            }
                        }
                    }

                    // Grammar
                    if (showAll || uiState.currentTab == FavoriteTab.GRAMMAR) {
                        items(uiState.favoriteRules, key = { "g_${it.id}" }) { rule ->
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("GRAMMAR RULE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text(rule.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        Text(rule.rule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { viewModel.removeFavoriteRule(rule.id) }) {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFE53E3E))
                                    }
                                }
                            }
                        }
                    }

                    // Phrases
                    if (showAll || uiState.currentTab == FavoriteTab.PHRASES) {
                        items(uiState.favoritePhrases, key = { "p_${it.id}" }) { phrase ->
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("EXPRESSION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                        Text(phrase.phrase, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(phrase.meaning, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    IconButton(onClick = { viewModel.removeFavoritePhrase(phrase.id) }) {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFE53E3E))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}
