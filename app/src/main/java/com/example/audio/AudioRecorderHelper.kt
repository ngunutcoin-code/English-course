package com.example.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds

    private val _amplitudeList = MutableStateFlow<List<Float>>(emptyList())
    val amplitudeList: StateFlow<List<Float>> = _amplitudeList

    private var outputFile: File? = null

    fun startRecording() {
        try {
            stopRecording()

            val file = File(context.cacheDir, "speech_rec_${System.currentTimeMillis()}.mp3")
            outputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(file.absolutePath)
            recorder.prepare()
            recorder.start()

            mediaRecorder = recorder
            _isRecording.value = true
            _elapsedSeconds.value = 0
            _amplitudeList.value = emptyList()

            startMonitoring()
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to start MediaRecorder: ${e.message}", e)
            // Fallback simulation mode for emulator when MIC is unavailable or restricted
            startSimulatedRecording()
        }
    }

    private fun startSimulatedRecording() {
        _isRecording.value = true
        _elapsedSeconds.value = 0
        _amplitudeList.value = emptyList()
        startMonitoring()
    }

    private fun startMonitoring() {
        recordingJob?.cancel()
        recordingJob = scope.launch {
            while (_isRecording.value) {
                delay(200)
                _elapsedSeconds.value += 0 // update seconds on 1000ms boundary
                val amp = try {
                    mediaRecorder?.maxAmplitude?.toFloat() ?: (1000..8000).random().toFloat()
                } catch (e: Exception) {
                    (1000..8000).random().toFloat()
                }
                
                val normalized = (amp / 10000f).coerceIn(0.1f, 1.0f)
                val current = _amplitudeList.value.toMutableList()
                if (current.size >= 30) {
                    current.removeAt(0)
                }
                current.add(normalized)
                _amplitudeList.value = current

                // Increment seconds every 5 ticks (1000ms)
                if ((current.size % 5) == 0) {
                    _elapsedSeconds.value += 1
                }
            }
        }
    }

    fun stopRecording(): File? {
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error stopping recorder: ${e.message}")
        } finally {
            mediaRecorder = null
        }

        return outputFile
    }

    fun cancel() {
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            mediaRecorder = null
        }
    }
}
