package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tests")
data class TestEntity(
    @PrimaryKey val id: String,
    val date: String, // "YYYY-MM-DD"
    val title: String,
    val type: String = "daily", // daily, weekly, monthly, vocabulary, grammar, custom, revision
    val durationMinutes: Int = 15,
    val totalQuestions: Int = 10,
    val instructions: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "test_attempts")
data class TestAttemptEntity(
    @PrimaryKey val id: String,
    val testId: String,
    val testTitle: String,
    val date: String,
    val startedAt: Long,
    val completedAt: Long,
    val totalQuestions: Int,
    val score: Int,
    val accuracy: Float,
    val timeSpentSeconds: Int,
    val answersJson: String = "[]", // Detailed user choices and correctness
    val topicBreakdownJson: String = "{}"
)

@Entity(tableName = "mistakes")
data class MistakeEntity(
    @PrimaryKey val id: String,
    val questionId: String,
    val questionText: String,
    val topic: String,
    val userWrongAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val relatedConcept: String = "",
    val repeatCount: Int = 1,
    val isImproved: Boolean = false,
    val firstMistakeDate: String,
    val lastMistakeDate: String,
    val lastReviewedAt: Long = 0L
)

@Entity(tableName = "daily_learning_progress")
data class DailyProgressEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val editorialRead: Boolean = false,
    val vocabLearnedCount: Int = 0,
    val totalVocabCount: Int = 0,
    val grammarStudiedCount: Int = 0,
    val totalGrammarCount: Int = 0,
    val expressionsLearnedCount: Int = 0,
    val totalExpressionsCount: Int = 0,
    val practiceAnsweredCount: Int = 0,
    val totalPracticeCount: Int = 0,
    val testTaken: Boolean = false,
    val testScore: Int = 0,
    val testMaxScore: Int = 0,
    val isDayComplete: Boolean = false,
    val lastStudiedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "import_history")
data class ImportHistoryEntity(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String, // "YYYY-MM-DD"
    val type: String, // "editorial", "vocabulary", "grammar", "phrases", "test", "daily_package"
    val itemCount: Int,
    val newCount: Int,
    val mergedCount: Int,
    val description: String
)
