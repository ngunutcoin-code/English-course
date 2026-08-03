package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speech_sessions")
data class SpeechSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicTitle: String,
    val category: String, // e.g., "60s Pitch", "IELTS Speaking", "Job Interview", "Daily Scenario", "Free Talk"
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int,
    val overallScore: Int, // 0 - 100
    val ieltsBandScore: Float, // e.g. 7.5
    val fluencyScore: Int, // 0 - 100
    val pronunciationScore: Int, // 0 - 100
    val grammarScore: Int, // 0 - 100
    val vocabularyScore: Int, // 0 - 100
    val wordsPerMinute: Int,
    val fillerWordCount: Int,
    val userTranscript: String,
    val correctedTranscript: String,
    val feedbackSummary: String,
    val strengthsJson: String, // Pipe or JSON separated list
    val improvementsJson: String, // Pipe or JSON separated list
    val recommendedVocabJson: String // Pipe or JSON separated list
)
