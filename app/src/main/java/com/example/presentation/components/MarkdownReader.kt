package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownEditorialView(
    markdownText: String,
    fontSizeSp: Float = 17f,
    lineSpacingMultiplier: Float = 1.6f,
    bookmarkedParagraph: Int = -1,
    vocabularyWords: Set<String> = emptySet(),
    onWordClick: (String) -> Unit = {},
    onBookmarkParagraph: (Int) -> Unit = {}
) {
    val paragraphs = markdownText.split("\n\n").filter { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        paragraphs.forEachIndexed { index, rawPara ->
            val trimmed = rawPara.trim()
            val isBookmarked = (index == bookmarkedParagraph)

            when {
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# ").trim(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = (fontSizeSp * 1.5f).sp,
                            lineHeight = (fontSizeSp * 1.8f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## ").trim(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = (fontSizeSp * 1.3f).sp,
                            lineHeight = (fontSizeSp * 1.6f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### ").trim(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = (fontSizeSp * 1.15f).sp,
                            lineHeight = (fontSizeSp * 1.4f).sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                trimmed.startsWith("> ") -> {
                    // Blockquote
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = trimmed.removePrefix("> ").trim(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = fontSizeSp.sp,
                                fontStyle = FontStyle.Italic,
                                lineHeight = (fontSizeSp * lineSpacingMultiplier).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    // Standard Paragraph with vocabulary detection and paragraph bookmarking
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            InteractiveParagraph(
                                text = trimmed,
                                fontSizeSp = fontSizeSp,
                                lineSpacingMultiplier = lineSpacingMultiplier,
                                knownWords = vocabularyWords,
                                onWordClick = onWordClick
                            )
                        }

                        IconButton(
                            onClick = { onBookmarkParagraph(index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark paragraph $index",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveParagraph(
    text: String,
    fontSizeSp: Float,
    lineSpacingMultiplier: Float,
    knownWords: Set<String>,
    onWordClick: (String) -> Unit
) {
    // Clean markdown bold syntax **word**
    val cleanText = text.replace("**", "").replace("*", "")
    val words = cleanText.split(Regex("(?<=\\s)|(?=\\s)"))

    val primaryColor = MaterialTheme.colorScheme.primary
    val defaultColor = MaterialTheme.colorScheme.onBackground
    val highlightBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)

    // Check vocabulary words that appear in this paragraph
    val wordsInPara = knownWords.filter { kw ->
        cleanText.contains(kw, ignoreCase = true)
    }

    if (wordsInPara.isEmpty()) {
        Text(
            text = cleanText,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp * lineSpacingMultiplier).sp,
                letterSpacing = 0.2.sp
            ),
            color = defaultColor
        )
    } else {
        Column {
            Text(
                text = cleanText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * lineSpacingMultiplier).sp,
                    letterSpacing = 0.2.sp
                ),
                color = defaultColor
            )

            // Chips for vocabulary words occurring in this paragraph for immediate tap & study
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                wordsInPara.forEach { kw ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { onWordClick(kw) }
                    ) {
                        Text(
                            text = "✦ ${kw.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
