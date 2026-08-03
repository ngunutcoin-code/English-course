package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioRecorderHelper
import com.example.audio.TextToSpeechHelper
import com.example.data.AppDatabase
import com.example.data.SpeechSessionEntity
import com.example.data.StimulerRepository
import com.example.data.UserStatsEntity
import com.example.data.VocabularyEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PracticeTopic(
    val id: String,
    val title: String,
    val category: String, // e.g. "60s Pitch", "IELTS Speaking", "Job Interview", "Daily Scenario"
    val level: String, // "Beginner", "Intermediate", "Advanced"
    val description: String,
    val targetTimeSeconds: Int,
    val sampleQuestions: List<String>,
    val targetVocabulary: List<String>
)

sealed interface EvaluationUiState {
    object Idle : EvaluationUiState
    data class Loading(val stepMessage: String) : EvaluationUiState
    data class Success(val session: SpeechSessionEntity) : EvaluationUiState
    data class Error(val message: String) : EvaluationUiState
}

class StimulerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StimulerRepository
    val audioRecorderHelper: AudioRecorderHelper
    val ttsHelper: TextToSpeechHelper

    init {
        val db = AppDatabase.getDatabase(application)
        repository = StimulerRepository(db.speechDao(), db.vocabularyDao(), db.userStatsDao())
        audioRecorderHelper = AudioRecorderHelper(application)
        ttsHelper = TextToSpeechHelper(application)
    }

    val userStats: StateFlow<UserStatsEntity?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStatsEntity())

    val allSessions: StateFlow<List<SpeechSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSessions: StateFlow<List<SpeechSessionEntity>> = repository.recentSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVocabulary: StateFlow<List<VocabularyEntity>> = repository.allVocabulary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active practice state
    private val _selectedTopic = MutableStateFlow<PracticeTopic?>(null)
    val selectedTopic: StateFlow<PracticeTopic?> = _selectedTopic

    private val _transcriptInput = MutableStateFlow("")
    val transcriptInput: StateFlow<String> = _transcriptInput

    private val _evaluationState = MutableStateFlow<EvaluationUiState>(EvaluationUiState.Idle)
    val evaluationState: StateFlow<EvaluationUiState> = _evaluationState

    private val _selectedSessionDetail = MutableStateFlow<SpeechSessionEntity?>(null)
    val selectedSessionDetail: StateFlow<SpeechSessionEntity?> = _selectedSessionDetail

    fun selectTopic(topic: PracticeTopic) {
        _selectedTopic.value = topic
        _transcriptInput.value = ""
        _evaluationState.value = EvaluationUiState.Idle
    }

    fun updateTranscriptInput(text: String) {
        _transcriptInput.value = text
    }

    fun startRecording() {
        audioRecorderHelper.startRecording()
    }

    fun stopRecordingAndSetTranscript() {
        audioRecorderHelper.stopRecording()
        // If transcript input is empty, provide sample speech or user typed transcript
        if (_transcriptInput.value.isBlank()) {
            val sampleResponses = listOf(
                "In my opinion, technology has transformed the way we communicate and work every single day. While it brings huge convenience, we must maintain a balance between digital interactions and real human connection.",
                "I strongly believe that learning a second language opens up incredible career opportunities and broadens our global perspective. It allows us to connect with diverse cultures and express ideas with greater empathy.",
                "One memorable experience I had was leading a cross-functional project at my previous company. We faced tight deadlines, but effective communication and clear delegation helped us achieve outstanding results."
            )
            _transcriptInput.value = sampleResponses.random()
        }
    }

    fun submitSpeechForEvaluation() {
        val topic = _selectedTopic.value ?: return
        val transcript = _transcriptInput.value
        val duration = audioRecorderHelper.elapsedSeconds.value.let { if (it <= 0) 45 else it }

        if (transcript.isBlank()) {
            _evaluationState.value = EvaluationUiState.Error("Please record or enter speech transcript first.")
            return
        }

        viewModelScope.launch {
            _evaluationState.value = EvaluationUiState.Loading("Analyzing speech audio & fluency...")
            try {
                val session = repository.evaluateAndSaveSpeech(
                    topicTitle = topic.title,
                    category = topic.category,
                    userTranscript = transcript,
                    durationSeconds = duration
                )
                _selectedSessionDetail.value = session
                _evaluationState.value = EvaluationUiState.Success(session)
            } catch (e: Exception) {
                _evaluationState.value = EvaluationUiState.Error("Evaluation failed: ${e.message}")
            }
        }
    }

    fun setSelectedSession(session: SpeechSessionEntity) {
        _selectedSessionDetail.value = session
    }

    fun loadSessionById(sessionId: Long) {
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId)
            if (session != null) {
                _selectedSessionDetail.value = session
            }
        }
    }

    fun saveWordToVault(word: String, phonetic: String, definition: String, example: String) {
        viewModelScope.launch {
            repository.saveVocabularyWord(word, phonetic, definition, example)
        }
    }

    fun toggleWordMastered(id: Long, currentMastered: Boolean) {
        viewModelScope.launch {
            repository.toggleVocabularyMastered(id, !currentMastered)
        }
    }

    fun deleteWord(id: Long) {
        viewModelScope.launch {
            repository.deleteVocabulary(id)
        }
    }

    fun speakText(text: String) {
        ttsHelper.speak(text)
    }

    fun stopSpeech() {
        ttsHelper.stop()
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorderHelper.cancel()
        ttsHelper.shutdown()
    }

    // Default practice topics
    companion object {
        val PRACTICE_TOPICS = listOf(
            PracticeTopic(
                id = "p1",
                title = "60s Elevator Pitch: Dream Career",
                category = "60s Pitch",
                level = "Intermediate",
                description = "Describe your ideal career, your passion, and what drives you to succeed in 60 seconds.",
                targetTimeSeconds = 60,
                sampleQuestions = listOf(
                    "What key skills make you stand out?",
                    "Where do you see yourself in 5 years?"
                ),
                targetVocabulary = listOf("Driven", "Strategic", "Cross-functional", "Proactive")
            ),
            PracticeTopic(
                id = "p2",
                title = "IELTS Part 2: A Memorable Journey",
                category = "IELTS Speaking",
                level = "Advanced",
                description = "Describe a trip or journey that had a profound impact on your worldview.",
                targetTimeSeconds = 120,
                sampleQuestions = listOf(
                    "Where did you go and with whom?",
                    "Why was this particular journey so memorable to you?"
                ),
                targetVocabulary = listOf("Exhilarating", "Unforgettable", "Enriching", "Picturesque")
            ),
            PracticeTopic(
                id = "p3",
                title = "Job Interview: Overcoming Challenges",
                category = "Job Interview",
                level = "Intermediate",
                description = "Tell me about a challenging situation at work or school and how you handled it.",
                targetTimeSeconds = 90,
                sampleQuestions = listOf(
                    "What was the core problem?",
                    "What specific actions did you take to resolve it?"
                ),
                targetVocabulary = listOf("Obstacle", "Mitigate", "Resolution", "Adaptability")
            ),
            PracticeTopic(
                id = "p4",
                title = "Daily Scenario: Salary Negotiation",
                category = "Daily Scenario",
                level = "Advanced",
                description = "Politely negotiate compensation with a hiring manager or reviewer.",
                targetTimeSeconds = 60,
                sampleQuestions = listOf(
                    "How do you justify your value?",
                    "What perks or growth opportunities are key for you?"
                ),
                targetVocabulary = listOf("Value proposition", "Benchmark", "Compensation", "Competitive")
            ),
            PracticeTopic(
                id = "p5",
                title = "Daily Debate: Remote vs In-Office Work",
                category = "Daily Debate",
                level = "Intermediate",
                description = "Argue your perspective on whether remote work improves overall productivity.",
                targetTimeSeconds = 60,
                sampleQuestions = listOf(
                    "What are the main pros and cons?",
                    "How does company culture adapt to hybrid teams?"
                ),
                targetVocabulary = listOf("Autonomy", "Collaboration", "Work-life balance", "Isolation")
            )
        )
    }
}
