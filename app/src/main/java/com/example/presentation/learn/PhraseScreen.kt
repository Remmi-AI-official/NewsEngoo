package com.example.presentation.learn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.EditorialApplication
import com.example.data.local.entity.PhraseEntity
import com.example.presentation.components.EmptyStateView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PhraseViewModel(private val app: EditorialApplication) : ViewModel() {
    private val phraseRepo = app.phraseRepository
    private val _searchQuery = MutableStateFlow("")
    private val _onlyFavorites = MutableStateFlow(false)

    val phrases: StateFlow<List<PhraseEntity>> = combine(
        phraseRepo.getAllPhrases(),
        _searchQuery,
        _onlyFavorites
    ) { all, query, favOnly ->
        var res = all
        if (favOnly) res = res.filter { it.isFavorite }
        if (query.isNotBlank()) {
            res = res.filter {
                it.phrase.contains(query, ignoreCase = true) ||
                it.meaning.contains(query, ignoreCase = true) ||
                it.hindiMeaning.contains(query, ignoreCase = true)
            }
        }
        res
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearch(q: String) { _searchQuery.value = q }
    fun setFavoritesOnly(fav: Boolean) { _onlyFavorites.value = fav }

    fun toggleFavorite(id: String, fav: Boolean) {
        viewModelScope.launch {
            phraseRepo.toggleFavorite(id, fav)
        }
    }

    fun updatePhrase(phrase: PhraseEntity) {
        viewModelScope.launch {
            phraseRepo.updatePhrase(phrase)
        }
    }

    fun deletePhrase(id: String) {
        viewModelScope.launch {
            phraseRepo.deletePhrase(id)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PhraseScreen(
    onNavigateBack: () -> Unit,
    app: EditorialApplication
) {
    val viewModel = remember { PhraseViewModel(app) }
    val phrases by viewModel.phrases.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    var filterFavorites by remember { mutableStateOf(false) }

    var phraseToEdit by remember { mutableStateOf<PhraseEntity?>(null) }
    var phraseToDelete by remember { mutableStateOf<PhraseEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Phrases & Idioms") },
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
                placeholder = { Text("Search idioms, collocations, phrases...") },
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
                    label = { Text("All Expressions") }
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

            if (phrases.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Psychology,
                    title = "No Expressions Found",
                    message = "No phrases match your search or filter."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(phrases, key = { it.id }) { item ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { phraseToEdit = item }
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
                                        text = item.phrase,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { phraseToEdit = item }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Phrase", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { phraseToDelete = item }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Phrase", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                        }
                                        IconButton(onClick = { viewModel.toggleFavorite(item.id, !item.isFavorite) }, modifier = Modifier.size(32.dp)) {
                                            Icon(
                                                imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (item.isFavorite) Color(0xFFE53E3E) else MaterialTheme.colorScheme.outlineVariant
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = item.meaning,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                if (item.hindiMeaning.isNotBlank()) {
                                    Text(
                                        text = "Hindi: ${item.hindiMeaning}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                if (item.example.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "“${item.example}”",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(8.dp)
                                        )
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

        // Edit Phrase Dialog
        phraseToEdit?.let { phrase ->
            var editPhrase by remember(phrase) { mutableStateOf(phrase.phrase) }
            var editMeaning by remember(phrase) { mutableStateOf(phrase.meaning) }
            var editHindiMeaning by remember(phrase) { mutableStateOf(phrase.hindiMeaning) }
            var editExample by remember(phrase) { mutableStateOf(phrase.example) }

            androidx.compose.material3.AlertDialog(
                onDismissRequest = { phraseToEdit = null },
                title = { Text("Edit Phrase / Expression") },
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
                            label = { Text("Phrase / Expression") },
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
                            label = { Text("Example Sentence") },
                            modifier = Modifier.fillMaxWidth().height(100.dp)
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
                    androidx.compose.material3.TextButton(onClick = { phraseToEdit = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Phrase Dialog
        phraseToDelete?.let { phrase ->
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { phraseToDelete = null },
                title = { Text("Delete Expression?") },
                text = { Text("Are you sure you want to delete '${phrase.phrase}'?") },
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
                    androidx.compose.material3.TextButton(onClick = { phraseToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
