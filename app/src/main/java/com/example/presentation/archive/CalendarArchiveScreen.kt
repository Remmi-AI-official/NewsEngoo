package com.example.presentation.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.DateUtils
import com.example.presentation.components.EditorialTopBar
import com.example.presentation.components.EmptyStateView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarArchiveScreen(
    onNavigateToReader: (String) -> Unit,
    onNavigateToLearnFlow: (String) -> Unit,
    viewModel: ArchiveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isCalendarView) "Calendar Archive" else "Chronological Archive") },
                actions = {
                    TextButton(onClick = { viewModel.jumpToToday() }) {
                        Icon(imageVector = Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Today")
                    }
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (uiState.isCalendarView) Icons.Default.List else Icons.Default.CalendarMonth,
                            contentDescription = "Toggle View"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (uiState.isCalendarView) {
            CalendarViewContent(
                uiState = uiState,
                onSelectDate = { viewModel.selectDate(it) },
                onChangeMonth = { viewModel.changeMonth(it) },
                onNavigateToReader = onNavigateToReader,
                onNavigateToLearnFlow = onNavigateToLearnFlow,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            ListViewContent(
                uiState = uiState,
                onToggleFavoriteFilter = { viewModel.toggleFavoriteFilter() },
                onToggleCompletedFilter = { viewModel.toggleCompletedFilter() },
                onSelectEditorial = { onNavigateToReader(it.id) },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun CalendarViewContent(
    uiState: ArchiveUiState,
    onSelectDate: (String) -> Unit,
    onChangeMonth: (Int) -> Unit,
    onNavigateToReader: (String) -> Unit,
    onNavigateToLearnFlow: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Month Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onChangeMonth(-1) }) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }

            Text(
                text = DateUtils.formatMonthYear(uiState.currentMonthCalendar),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(onClick = { onChangeMonth(1) }) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Days of week row
        val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid Days
        val cal = uiState.currentMonthCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-based Sunday
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthStr = String.format("%02d", cal.get(Calendar.MONTH) + 1)
        val year = cal.get(Calendar.YEAR)

        val totalCells = (firstDayOfWeek + maxDays + 6) / 7 * 7

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (row in 0 until (totalCells / 7)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNum = cellIndex - firstDayOfWeek + 1

                        if (dayNum in 1..maxDays) {
                            val dateStr = String.format("%04d-%s-%02d", year, monthStr, dayNum)
                            val isSelected = dateStr == uiState.selectedDate
                            val hasContent = dateStr in uiState.availableDatesWithContent
                            val isToday = DateUtils.isToday(dateStr)

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onSelectDate(dateStr) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNum",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (hasContent) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else MaterialTheme.colorScheme.secondary)
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.size(42.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Selected Date Details Card
        val summary = uiState.selectedDateSummary
        Text(
            text = "Package for ${DateUtils.formatDate(uiState.selectedDate)}",
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (summary != null && (summary.editorial != null || summary.wordsCount > 0)) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (summary.editorial != null) {
                        Text(
                            text = summary.editorial.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${summary.editorial.source} • ${summary.editorial.readTimeMinutes} min read",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    }

                    // Availability Checklist
                    AvailabilityRow("Editorial Article", summary.editorial != null)
                    AvailabilityRow("Vocabulary Words", summary.wordsCount > 0, detail = "${summary.wordsCount} words")
                    AvailabilityRow("Grammar Concepts", summary.grammarCount > 0, detail = "${summary.grammarCount} rules")
                    AvailabilityRow("Phrases / Idioms", summary.phrasesCount > 0, detail = "${summary.phrasesCount} phrases")
                    AvailabilityRow("Practice Questions", summary.questionsCount > 0, detail = "${summary.questionsCount} questions")
                    AvailabilityRow("Daily Test", summary.testsCount > 0, detail = if (summary.testsCount > 0) "Ready" else "None")

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (summary.editorial != null) {
                            Button(
                                onClick = { onNavigateToReader(summary.editorial.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Read Editorial")
                            }
                        }
                        Button(
                            onClick = { onNavigateToLearnFlow(summary.date) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Open Learning")
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No content available for this date.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ListViewContent(
    uiState: ArchiveUiState,
    onToggleFavoriteFilter: () -> Unit,
    onToggleCompletedFilter: () -> Unit,
    onSelectEditorial: (com.example.data.local.entity.EditorialEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val filtered = uiState.allEditorials.filter { ed ->
        (!uiState.filterFavoriteOnly || ed.isFavorite) &&
        (!uiState.filterCompletedOnly || ed.isCompleted)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.filterFavoriteOnly,
                onClick = onToggleFavoriteFilter,
                label = { Text("⭐ Favorites Only") }
            )
            FilterChip(
                selected = uiState.filterCompletedOnly,
                onClick = onToggleCompletedFilter,
                label = { Text("✓ Completed Only") }
            )
        }

        if (filtered.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.FilterList,
                title = "No Articles Found",
                message = "No articles match the current filter selection."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { ed ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectEditorial(ed) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = DateUtils.formatDate(ed.date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (ed.isFavorite) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFE53E3E), modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ed.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${ed.source} • ${ed.readTimeMinutes} min read",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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

@Composable
fun AvailabilityRow(
    title: String,
    isAvailable: Boolean,
    detail: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isAvailable) "✓" else "○",
                fontWeight = FontWeight.Bold,
                color = if (isAvailable) Color(0xFF22543D) else MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (detail.isNotBlank()) {
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
