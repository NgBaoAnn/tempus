package com.projectapp.tempus.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale


class SpeechRecognitionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SpeechRecognition"
    }
    
    private var speechRecognizer: SpeechRecognizer? = null
    
    
    fun isAvailable(): Boolean {
        val available = SpeechRecognizer.isRecognitionAvailable(context)
        Log.d(TAG, "isAvailable: $available")
        return available
    }
    
    
    fun startListening(): Flow<SpeechResult> = callbackFlow {
        Log.d(TAG, "startListening called")
        
        if (!isAvailable()) {
            Log.e(TAG, "Speech recognition not available on this device")
            trySend(SpeechResult.Error("Speech recognition not available"))
            close()
            return@callbackFlow
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        Log.d(TAG, "SpeechRecognizer created")
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN") 
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        Log.d(TAG, "Intent created for vi-VN")
        
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "onReadyForSpeech - Microphone ready")
                trySend(SpeechResult.Listening)
            }
            
            override fun onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech - User started speaking")
                trySend(SpeechResult.Speaking)
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                
                trySend(SpeechResult.RmsChanged(rmsdB))
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d(TAG, "onBufferReceived - buffer size: ${buffer?.size ?: 0}")
            }
            
            override fun onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech - User stopped speaking")
                trySend(SpeechResult.Processing)
            }
            
            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error"
                }
                Log.e(TAG, "onError: code=$error message=$message")
                trySend(SpeechResult.Error(message))
                close()
            }
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                Log.d(TAG, "onResults: '$text' (${matches?.size ?: 0} matches)")
                trySend(SpeechResult.Success(text))
                close()
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotEmpty()) {
                    Log.d(TAG, "onPartialResults: '$text'")
                    trySend(SpeechResult.Partial(text))
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d(TAG, "onEvent: type=$eventType")
            }
        })
        
        Log.d(TAG, "Starting speech recognition...")
        speechRecognizer?.startListening(intent)
        
        awaitClose {
            Log.d(TAG, "Flow closed - stopping recognition")
            stopListening()
        }
    }
    
    
    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}


sealed class SpeechResult {
    object Listening : SpeechResult()
    object Speaking : SpeechResult()
    object Processing : SpeechResult()
    data class RmsChanged(val rms: Float) : SpeechResult()
    data class Partial(val text: String) : SpeechResult()
    data class Success(val text: String) : SpeechResult()
    data class Error(val message: String) : SpeechResult()
}
