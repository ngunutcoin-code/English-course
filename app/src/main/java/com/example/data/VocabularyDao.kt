package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary_items ORDER BY addedTimestamp DESC")
    fun getAllVocabulary(): Flow<List<VocabularyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(word: VocabularyEntity): Long

    @Query("UPDATE vocabulary_items SET isMastered = :isMastered WHERE id = :id")
    suspend fun updateMastered(id: Long, isMastered: Boolean)

    @Query("DELETE FROM vocabulary_items WHERE id = :id")
    suspend fun deleteVocabulary(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM vocabulary_items WHERE word = :word LIMIT 1)")
    suspend fun containsWord(word: String): Boolean
}
