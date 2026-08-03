package com.example.data

import com.example.network.GeminiSpeechEvaluator
import com.example.network.SpeechAnalysisResult
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StimulerRepository(
    private val speechDao: SpeechDao,
    private val vocabularyDao: VocabularyDao,
    private val userStatsDao: UserStatsDao
) {
    private val speechEvaluator = GeminiSpeechEvaluator()

    val allSessions: Flow<List<SpeechSessionEntity>> = speechDao.getAllSessions()
    val recentSessions: Flow<List<SpeechSessionEntity>> = speechDao.getRecentSessions()
    val allVocabulary: Flow<List<VocabularyEntity>> = vocabularyDao.getAllVocabulary()
    val userStats: Flow<UserStatsEntity?> = userStatsDao.getUserStats()

    suspend fun evaluateAndSaveSpeech(
        topicTitle: String,
        category: String,
        userTranscript: String,
        durationSeconds: Int
    ): SpeechSessionEntity {
        val analysis = speechEvaluator.evaluateSpeech(
            topic = topicTitle,
            category = category,
            userTranscript = userTranscript,
            durationSeconds = durationSeconds
        )

        val session = SpeechSessionEntity(
            topicTitle = topicTitle,
            category = category,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            overallScore = analysis.overallScore,
            ieltsBandScore = analysis.ieltsBandScore,
            fluencyScore = analysis.fluencyScore,
            pronunciationScore = analysis.pronunciationScore,
            grammarScore = analysis.grammarScore,
            vocabularyScore = analysis.vocabularyScore,
            wordsPerMinute = analysis.estimatedWpm,
            fillerWordCount = analysis.fillerWordCount,
            userTranscript = userTranscript,
            correctedTranscript = analysis.correctedTranscript,
            feedbackSummary = analysis.feedbackSummary,
            strengthsJson = analysis.strengths.joinToString("|"),
            improvementsJson = analysis.improvements.joinToString("|"),
            recommendedVocabJson = analysis.recommendedVocabulary.joinToString("||") {
                "${it.word}#${it.phonetic}#${it.definition}#${it.exampleSentence}"
            }
        )

        val insertedId = speechDao.insertSession(session)
        val savedSession = session.copy(id = insertedId)

        // Update User Daily Streak & Stats
        updateStatsAfterSession(durationSeconds, analysis.overallScore)

        return savedSession
    }

    private suspend fun updateStatsAfterSession(durationSeconds: Int, sessionScore: Int) {
        val currentStats = userStatsDao.getUserStatsSync() ?: UserStatsEntity()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val newStreak = if (currentStats.lastPracticeDate == todayStr) {
            currentStats.streakDays
        } else if (isYesterday(currentStats.lastPracticeDate)) {
            currentStats.streakDays + 1
        } else {
            1
        }

        val totalCount = currentStats.totalSessionsCount + 1
        val newAvg = ((currentStats.averageScore * currentStats.totalSessionsCount) + sessionScore) / totalCount.toFloat()

        val updated = currentStats.copy(
            streakDays = newStreak,
            lastPracticeDate = todayStr,
            totalSpeakingSeconds = currentStats.totalSpeakingSeconds + durationSeconds,
            totalSessionsCount = totalCount,
            averageScore = newAvg
        )

        userStatsDao.insertOrUpdateStats(updated)
    }

    private fun isYesterday(dateStr: String): Boolean {
        if (dateStr.isBlank()) return false
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val lastDate = sdf.parse(dateStr) ?: return false
            val diff = System.currentTimeMillis() - lastDate.time
            diff in (12 * 3600 * 1000)..(36 * 3600 * 1000)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveVocabularyWord(
        word: String,
        phonetic: String,
        definition: String,
        exampleSentence: String,
        category: String = "AI Recommendation"
    ) {
        if (!vocabularyDao.containsWord(word)) {
            val entity = VocabularyEntity(
                word = word,
                phonetic = phonetic,
                definition = definition,
                exampleSentence = exampleSentence,
                category = category
            )
            vocabularyDao.insertVocabulary(entity)
        }
    }

    suspend fun toggleVocabularyMastered(id: Long, isMastered: Boolean) {
        vocabularyDao.updateMastered(id, isMastered)
    }

    suspend fun deleteVocabulary(id: Long) {
        vocabularyDao.deleteVocabulary(id)
    }

    suspend fun getSessionById(sessionId: Long): SpeechSessionEntity? {
        return speechDao.getSessionById(sessionId)
    }
}
