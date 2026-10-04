package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "word_category_cross_ref",
    primaryKeys = ["wordId", "categoryId"],
    indices = [Index(value = ["categoryId"]), Index(value = ["wordId"])]
)
data class WordCategoryCrossRef(
    val wordId: String,
    val categoryId: String
)

@Entity(
    tableName = "editorial_vocabulary_cross_ref",
    primaryKeys = ["editorialId", "wordId"],
    indices = [Index(value = ["wordId"]), Index(value = ["editorialId"]), Index(value = ["date"])]
)
data class EditorialVocabularyCrossRef(
    val editorialId: String,
    val wordId: String,
    val date: String
)

@Entity(
    tableName = "test_question_cross_ref",
    primaryKeys = ["testId", "questionId"],
    indices = [Index(value = ["questionId"]), Index(value = ["testId"])]
)
data class TestQuestionCrossRef(
    val testId: String,
    val questionId: String,
    val orderIndex: Int = 0
)
