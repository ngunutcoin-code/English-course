package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeechDao {
    @Query("SELECT * FROM speech_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SpeechSessionEntity>>

    @Query("SELECT * FROM speech_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): SpeechSessionEntity?

    @Query("SELECT * FROM speech_sessions ORDER BY timestamp DESC LIMIT 5")
    fun getRecentSessions(): Flow<List<SpeechSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SpeechSessionEntity): Long

    @Query("DELETE FROM speech_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("SELECT COUNT(*) FROM speech_sessions")
    fun getSessionCount(): Flow<Int>
}
