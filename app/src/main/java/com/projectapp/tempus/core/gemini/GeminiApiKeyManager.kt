package com.projectapp.tempus.core.gemini

import android.util.Log
import com.projectapp.tempus.BuildConfig


object GeminiApiKeyManager {
    
    private const val TAG = "GeminiApiKeyManager"
    
    
    private val apiKeys = listOf(
        BuildConfig.GEMINI_API_KEY_1,
        BuildConfig.GEMINI_API_KEY_2,
        BuildConfig.GEMINI_API_KEY_3,
        BuildConfig.GEMINI_API_KEY_4,
        BuildConfig.GEMINI_API_KEY_5,
        BuildConfig.GEMINI_API_KEY_6,
        BuildConfig.GEMINI_API_KEY_7,
        BuildConfig.GEMINI_API_KEY_8
    )
    
    
    @Volatile
    private var currentIndex = 0
    
    
    private val lock = Any()
    
    init {
        Log.d(TAG, "Initialized with ${apiKeys.size} API keys")
    }
    
    
    fun getCurrentKey(): String {
        synchronized(lock) {
            val key = apiKeys[currentIndex]
            Log.d(TAG, "Using API key #${currentIndex + 1}")
            return key
        }
    }
    
    
    fun rotateToNextKey(): String {
        synchronized(lock) {
            val previousIndex = currentIndex
            currentIndex = (currentIndex + 1) % apiKeys.size
            Log.w(TAG, "Rotating from key #${previousIndex + 1} to key #${currentIndex + 1}")
            return apiKeys[currentIndex]
        }
    }
    
    
    fun getKeyCount(): Int = apiKeys.size
    
    
    fun getCurrentKeyIndex(): Int {
        synchronized(lock) {
            return currentIndex + 1
        }
    }
    
    
    fun reset() {
        synchronized(lock) {
            currentIndex = 0
            Log.d(TAG, "Reset to first API key")
        }
    }
}
