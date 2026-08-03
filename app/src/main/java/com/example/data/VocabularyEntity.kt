package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary_items")
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val phonetic: String,
    val definition: String,
    val exampleSentence: String,
    val category: String,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val isMastered: Boolean = false
)
