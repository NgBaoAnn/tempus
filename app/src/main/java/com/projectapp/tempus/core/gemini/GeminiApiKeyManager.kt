package com.projectapp.tempus.core.gemini

import android.util.Log
import com.projectapp.tempus.BuildConfig

/**
 * Singleton manager for Gemini API key rotation
 * 
 * Manages a pool of 8 API keys and provides automatic rotation
 * when rate limits are encountered.
 * 
 * Features:
 * - Round-robin key selection
 * - Thread-safe key rotation
 * - Automatic failover on rate limit errors
 * - Logging for debugging
 */
object GeminiApiKeyManager {
    
    private const val TAG = "GeminiApiKeyManager"
    
    // Pool of all available API keys
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
    
    // Current key index (thread-safe)
    @Volatile
    private var currentIndex = 0
    
    // Lock for thread-safe operations
    private val lock = Any()
    
    init {
        Log.d(TAG, "Initialized with ${apiKeys.size} API keys")
    }
    
    /**
     * Get the current API key
     * Thread-safe read operation
     */
    fun getCurrentKey(): String {
        synchronized(lock) {
            val key = apiKeys[currentIndex]
            Log.d(TAG, "Using API key #${currentIndex + 1}")
            return key
        }
    }
    
    /**
     * Rotate to the next API key
     * Called when current key hits rate limit
     * 
     * @return The next API key to use
     */
    fun rotateToNextKey(): String {
        synchronized(lock) {
            val previousIndex = currentIndex
            currentIndex = (currentIndex + 1) % apiKeys.size
            Log.w(TAG, "Rotating from key #${previousIndex + 1} to key #${currentIndex + 1}")
            return apiKeys[currentIndex]
        }
    }
    
    /**
     * Get total number of available keys
     */
    fun getKeyCount(): Int = apiKeys.size
    
    /**
     * Get current key index (1-based for logging)
     */
    fun getCurrentKeyIndex(): Int {
        synchronized(lock) {
            return currentIndex + 1
        }
    }
    
    /**
     * Reset to first key (useful for testing or manual reset)
     */
    fun reset() {
        synchronized(lock) {
            currentIndex = 0
            Log.d(TAG, "Reset to first API key")
        }
    }
}
