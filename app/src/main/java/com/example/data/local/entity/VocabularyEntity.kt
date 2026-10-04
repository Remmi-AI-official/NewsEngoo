package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary_words")
data class VocabularyEntity(
    @PrimaryKey val id: String, // Normalized lowercase word (e.g. "pragmatic")
    val word: String,
    val pronunciation: String = "",
    val partOfSpeech: String = "word",
    val meaning: String,
    val hindiMeaning: String = "",
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val exampleSentence: String = "",
    val wordFamily: String = "",
    val difficulty: String = "medium", // easy, medium, hard
    val sourceArticle: String = "",
    val isFavorite: Boolean = false,
    val personalNote: String = "",
    // Spaced repetition status: NEW, LEARNING, REVIEW, FAMILIAR, MASTERED
    val learningStatus: String = "NEW",
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val confidence: Int = 0, // 0 - 100
    val lastReviewedAt: Long = 0L,
    val nextReviewDate: String = "", // "YYYY-MM-DD"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vocabulary_categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val colorHex: String = "#1A365D",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
