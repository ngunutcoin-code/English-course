package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val streakDays: Int = 1,
    val lastPracticeDate: String = "",
    val totalSpeakingSeconds: Long = 0,
    val totalSessionsCount: Int = 0,
    val averageScore: Float = 0f,
    val targetMinutesPerDay: Int = 10
)
