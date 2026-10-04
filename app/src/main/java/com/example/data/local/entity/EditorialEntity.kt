package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "editorials")
data class EditorialEntity(
    @PrimaryKey val id: String,
    val date: String, // "YYYY-MM-DD"
    val title: String,
    val source: String,
    val contentMarkdown: String,
    val readTimeMinutes: Int = 5,
    val readingPosition: Int = 0, // Scroll index or offset
    val isCompleted: Boolean = false,
    val isFavorite: Boolean = false,
    val bookmarkParagraph: Int = 0,
    val personalNote: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
