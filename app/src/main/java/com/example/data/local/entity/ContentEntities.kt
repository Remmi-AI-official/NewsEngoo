package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grammar_rules")
data class GrammarRuleEntity(
    @PrimaryKey val id: String,
    val date: String, // "YYYY-MM-DD"
    val title: String,
    val rule: String,
    val explanation: String,
    val correctExamples: List<String> = emptyList(),
    val incorrectExamples: List<String> = emptyList(),
    val commonMistakes: String = "",
    val editorialExample: String = "",
    val difficulty: String = "medium", // easy, medium, hard
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val personalNote: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "phrases_expressions")
data class PhraseEntity(
    @PrimaryKey val id: String,
    val date: String, // "YYYY-MM-DD"
    val phrase: String,
    val type: String = "idiom", // idiom, phrasal_verb, collocation, editorial_expression, formal_expression, academic
    val meaning: String,
    val hindiMeaning: String = "",
    val example: String = "",
    val category: String = "General",
    val difficulty: String = "medium",
    val source: String = "",
    val isFavorite: Boolean = false,
    val personalNote: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val date: String, // "YYYY-MM-DD"
    val testId: String = "", // Optional direct test linkage
    val type: String = "mcq", // mcq, synonym, antonym, meaning, fill_blank, sentence_correction, error_detection, reading_comprehension
    val question: String,
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0, // 0-based index in options
    val correctAnswerText: String = "",
    val explanation: String = "",
    val topic: String = "vocabulary", // vocabulary, grammar, expressions, comprehension
    val difficulty: String = "medium",
    val relatedWordOrRule: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
