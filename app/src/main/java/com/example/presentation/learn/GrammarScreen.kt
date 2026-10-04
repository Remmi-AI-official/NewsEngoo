package com.example.presentation.learn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.EditorialApplication
import com.example.data.local.entity.GrammarRuleEntity
import com.example.presentation.components.EmptyStateView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GrammarViewModel(private val app: EditorialApplication) : ViewModel() {
    private val grammarRepo = app.grammarRepository
    private val _searchQuery = MutableStateFlow("")
    private val _onlyFavorites = MutableStateFlow(false)

    val rules: StateFlow<List<GrammarRuleEntity>> = combine(
        grammarRepo.getAllRules(),
        _searchQuery,
        _onlyFavorites
    ) { all, query, favOnly ->
        var res = all
        if (favOnly) res = res.filter { it.isFavorite }
        if (query.isNotBlank()) {
            res = res.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.rule.contains(query, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }
        res
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearch(q: String) { _searchQuery.value = q }
    fun setFavoritesOnly(fav: Boolean) { _onlyFavorites.value = fav }

    fun toggleFavorite(id: String, fav: Boolean) {
        viewModelScope.launch {
            grammarRepo.toggleFavorite(id, fav)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarScreen(
    onNavigateBack: () -> Unit,
    app: EditorialApplication
) {
    val viewModel = remember { GrammarViewModel(app) }
    val rules by viewModel.rules.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    var filterFavorites by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Grammar Library") },
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
            OutlinedTextField(
                value = searchInput,
                onValueChange = {
                    searchInput = it
                    viewModel.setSearch(it)
                },
                placeholder = { Text("Search grammar rules or tags...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !filterFavorites,
                    onClick = {
                        filterFavorites = false
                        viewModel.setFavoritesOnly(false)
                    },
                    label = { Text("All Rules") }
                )
                FilterChip(
                    selected = filterFavorites,
                    onClick = {
                        filterFavorites = true
                        viewModel.setFavoritesOnly(true)
                    },
                    label = { Text("⭐ Favorites") }
                )
            }

            if (rules.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.AutoAwesome,
                    title = "No Grammar Rules Found",
                    message = "No rules match current search or filters."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(rules, key = { it.id }) { rule ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
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
                                        text = rule.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { viewModel.toggleFavorite(rule.id, !rule.isFavorite) }) {
                                        Icon(
                                            imageVector = if (rule.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (rule.isFavorite) Color(0xFFE53E3E) else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = rule.rule,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                if (rule.explanation.isNotBlank()) {
                                    Text(
                                        text = rule.explanation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (rule.correctExamples.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("✓ Correct:", style = MaterialTheme.typography.labelMedium, color = Color(0xFF22543D))
                                    rule.correctExamples.forEach { ex ->
                                        Text("• $ex", style = MaterialTheme.typography.bodySmall, color = Color(0xFF22543D))
                                    }
                                }

                                if (rule.incorrectExamples.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("✗ Incorrect:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                                    rule.incorrectExamples.forEach { ex ->
                                        Text("• $ex", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
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
