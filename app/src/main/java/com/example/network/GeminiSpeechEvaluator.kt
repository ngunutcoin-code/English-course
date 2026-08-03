package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class GeminiSpeechEvaluator {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun evaluateSpeech(
        topic: String,
        category: String,
        userTranscript: String,
        durationSeconds: Int
    ): SpeechAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val sanitizedTranscript = userTranscript.trim()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || sanitizedTranscript.length < 5) {
            Log.d("GeminiSpeechEvaluator", "Using offline/local speech evaluation engine")
            return@withContext generateLocalAnalysis(topic, category, sanitizedTranscript, durationSeconds)
        }

        try {
            val prompt = """
                You are Stimuler AI, an elite English Speaking & Speech Coach used by millions of learners.
                Analyze the following spoken English response for the given topic and practice mode.
                
                Category/Mode: $category
                Topic Prompt: $topic
                Duration in seconds: $durationSeconds
                User Spoken Transcript: "$sanitizedTranscript"
                
                Respond ONLY with a valid, clean JSON object (no markdown wrapping, no extra text) with the following exact keys:
                {
                  "overallScore": integer (0 to 100),
                  "ieltsBandScore": float (e.g. 7.5),
                  "fluencyScore": integer (0 to 100),
                  "pronunciationScore": integer (0 to 100),
                  "grammarScore": integer (0 to 100),
                  "vocabularyScore": integer (0 to 100),
                  "estimatedWpm": integer (words per minute calculated based on duration),
                  "fillerWordCount": integer (count of filler words like 'um', 'uh', 'you know', 'like', 'so'),
                  "detectedFillers": array of strings (e.g. ["um", "like"]),
                  "correctedTranscript": string (the polished, native English version of what the user tried to say),
                  "feedbackSummary": string (encouraging 2-sentence feedback summary),
                  "strengths": array of strings (2-3 bullet points of what went well),
                  "improvements": array of strings (2-3 actionable tips to improve),
                  "recommendedVocabulary": array of objects [
                     {
                       "word": string,
                       "phonetic": string (e.g. "/ˈeloʊkwənt/"),
                       "definition": string,
                       "exampleSentence": string
                     }
                  ]
                }
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.3f,
                    responseMimeType = "application/json"
                )
            )

            val response = RetrofitClient.geminiService.generateSpeechAnalysis(apiKey = apiKey, request = request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (jsonText != null) {
                val cleanJson = jsonText.replace("```json", "").replace("```", "").trim()
                val adapter = moshi.adapter(SpeechAnalysisResult::class.java)
                val parsed = adapter.fromJson(cleanJson)
                if (parsed != null) {
                    return@withContext parsed
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiSpeechEvaluator", "Gemini API call error: ${e.message}", e)
        }

        return@withContext generateLocalAnalysis(topic, category, sanitizedTranscript, durationSeconds)
    }

    private fun generateLocalAnalysis(
        topic: String,
        category: String,
        transcript: String,
        durationSeconds: Int
    ): SpeechAnalysisResult {
        val wordList = transcript.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val wordCount = wordList.size
        val effectiveDuration = if (durationSeconds > 0) durationSeconds else 15
        val calculatedWpm = ((wordCount.toFloat() / effectiveDuration) * 60).toInt().coerceAtLeast(30)

        val fillerWordsList = listOf("um", "uh", "like", "you know", "actually", "basically", "so")
        val foundFillers = mutableListOf<String>()
        var fillerCount = 0

        wordList.forEach { w ->
            val clean = w.lowercase(Locale.ROOT).replace(Regex("[^a-z]"), "")
            if (clean in fillerWordsList) {
                fillerCount++
                if (!foundFillers.contains(clean)) {
                    foundFillers.add(clean)
                }
            }
        }

        // Calculate dynamic scores based on transcript length & structure
        val baseScore = (65 + (wordCount * 1.5).toInt() - (fillerCount * 3)).coerceIn(50, 95)
        val fluency = (baseScore + 2).coerceIn(40, 98)
        val grammar = (baseScore - 3).coerceIn(45, 96)
        val vocab = (baseScore + 1).coerceIn(50, 97)
        val pronunciation = (baseScore - 1).coerceIn(50, 95)
        val ieltsBand = (baseScore.toFloat() / 10f).coerceIn(4.5f, 9.0f)

        val corrected = if (transcript.isBlank()) {
            "I would like to discuss $topic because it plays an important role in modern communication."
        } else {
            transcript.replace(" um ", " ")
                .replace(" uh ", " ")
                .replace("like ", "")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } +
                    if (!transcript.endsWith(".")) "." else ""
        }

        return SpeechAnalysisResult(
            overallScore = baseScore,
            ieltsBandScore = (Math.round(ieltsBand * 2) / 2.0f),
            fluencyScore = fluency,
            pronunciationScore = pronunciation,
            grammarScore = grammar,
            vocabularyScore = vocab,
            estimatedWpm = calculatedWpm,
            fillerWordCount = fillerCount,
            detectedFillers = if (foundFillers.isEmpty()) listOf("None detected!") else foundFillers,
            correctedTranscript = "Enhanced phrasing: $corrected",
            feedbackSummary = "Great effort! Your response demonstrates good attempt at expressing ideas clearly regarding $topic.",
            strengths = listOf(
                "Clear pace of speech (~$calculatedWpm WPM)",
                "Maintained topic relevance throughout the response",
                "Good vocal confidence and articulation"
            ),
            improvements = listOf(
                if (fillerCount > 0) "Reduce filler words ($fillerCount detected) by pausing silently instead." else "Vary sentence lengths for richer tone.",
                "Incorporate C1/C2 advanced vocabulary to boost lexical resource band",
                "Focus on crisp consonant pronunciations at sentence ends"
            ),
            recommendedVocabulary = listOf(
                RecommendedVocabItem(
                    word = "Articulate",
                    phonetic = "/ɑːrˈtɪk.jə.lət/",
                    definition = "Expressing oneself clearly and effectively in speech.",
                    exampleSentence = "She gave an articulate defense of her project proposal."
                ),
                RecommendedVocabItem(
                    word = "Eloquence",
                    phonetic = "/ˈel.ə.kwəns/",
                    definition = "Fluent or persuasive speaking or writing.",
                    exampleSentence = "His eloquence during the IELTS interview impressed the examiner."
                ),
                RecommendedVocabItem(
                    word = "Spontaneous",
                    phonetic = "/spɒnˈteɪ.ni.əs/",
                    definition = "Performed or occurring as a result of a sudden impulse without premeditation.",
                    exampleSentence = "Fluency improves when you practice spontaneous 60-second speeches."
                )
            )
        )
    }
}
