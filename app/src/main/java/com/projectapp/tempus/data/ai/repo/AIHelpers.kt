package com.projectapp.tempus.data.ai.repo

import com.projectapp.tempus.core.gemini.GeminiApiKeyManager
import org.json.JSONObject

/**
 * Helper functions for AI operations.
 * Contains retry logic, label/color inference, and JSON parsing utilities.
 */
object AIHelpers {
    
    private const val TAG = "AIHelpers"
    private const val MAX_RETRIES = 8
    
    /**
     * Execute API call with retry logic and key rotation.
     * Automatically rotates to next API key when rate limit is hit.
     */
    suspend fun <T> executeWithRetry(
        apiKeyManager: GeminiApiKeyManager,
        apiCall: suspend (apiKey: String) -> T
    ): Result<T> {
        var lastException: Exception? = null
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                val apiKey = if (attempt == 0) {
                    apiKeyManager.getCurrentKey()
                } else {
                    apiKeyManager.rotateToNextKey()
                }
                
                val result = apiCall(apiKey)
                return Result.success(result)
                
            } catch (e: Exception) {
                lastException = e
                val errorMessage = e.message?.lowercase() ?: ""
                
                // Check if rate limit error
                val isRateLimitError = errorMessage.contains("429") || 
                                      errorMessage.contains("rate limit") ||
                                      errorMessage.contains("quota exceeded") ||
                                      errorMessage.contains("resource_exhausted")
                
                if (isRateLimitError) {
                    android.util.Log.w(TAG, "Rate limit hit on attempt ${attempt + 1}, rotating key...")
                } else {
                    // Non-rate-limit error, fail immediately
                    android.util.Log.e(TAG, "Non-rate-limit error: ${e.message}")
                    return Result.failure(e)
                }
            }
        }
        
        // All retries exhausted
        return Result.failure(
            lastException ?: Exception("All API keys exhausted due to rate limits")
        )
    }
    
    /**
     * Infer schedule label from task title using keyword matching.
     * Uses project's ScheduleLabel enum values.
     */
    fun inferLabelFromTitle(title: String): String {
        val lowerTitle = title.lowercase()
        return when {
            // Wakeup
            lowerTitle.contains("thức dậy") || lowerTitle.contains("wake") ||
            lowerTitle.contains("dậy") || lowerTitle.contains("morning") ||
            lowerTitle.contains("báo thức") -> "wakeup"
            
            // Eat
            lowerTitle.contains("ăn") || lowerTitle.contains("eat") ||
            lowerTitle.contains("bữa") || lowerTitle.contains("meal") ||
            lowerTitle.contains("sáng") || lowerTitle.contains("trưa") ||
            lowerTitle.contains("tối") || lowerTitle.contains("breakfast") ||
            lowerTitle.contains("lunch") || lowerTitle.contains("dinner") -> "eat"
            
            // Exercise
            lowerTitle.contains("tập") || lowerTitle.contains("gym") ||
            lowerTitle.contains("chạy") || lowerTitle.contains("run") ||
            lowerTitle.contains("yoga") || lowerTitle.contains("thể dục") ||
            lowerTitle.contains("exercise") || lowerTitle.contains("workout") -> "exercise"
            
            // Rest
            lowerTitle.contains("nghỉ") || lowerTitle.contains("rest") ||
            lowerTitle.contains("thư giãn") || lowerTitle.contains("relax") -> "rest"
            
            // Water
            lowerTitle.contains("uống nước") || lowerTitle.contains("water") ||
            lowerTitle.contains("hydrat") -> "water"
            
            // Sleep
            lowerTitle.contains("ngủ") || lowerTitle.contains("sleep") ||
            lowerTitle.contains("đi ngủ") -> "sleep"
            
            // Clean
            lowerTitle.contains("dọn") || lowerTitle.contains("clean") ||
            lowerTitle.contains("vệ sinh") || lowerTitle.contains("lau") -> "clean"
            
            // Cook
            lowerTitle.contains("nấu") || lowerTitle.contains("cook") ||
            lowerTitle.contains("chuẩn bị") -> "cook"
            
            // Garden
            lowerTitle.contains("vườn") || lowerTitle.contains("garden") ||
            lowerTitle.contains("cây") || lowerTitle.contains("plant") -> "garden"
            
            // Default: Book (study, work, etc.)
            lowerTitle.contains("học") || lowerTitle.contains("study") || 
            lowerTitle.contains("ôn") || lowerTitle.contains("đọc") ||
            lowerTitle.contains("nghiên cứu") || lowerTitle.contains("research") ||
            lowerTitle.contains("luyện") || lowerTitle.contains("practice") ||
            lowerTitle.contains("code") || lowerTitle.contains("lập trình") ||
            lowerTitle.contains("làm") || lowerTitle.contains("work") ||
            lowerTitle.contains("tiếng") || lowerTitle.contains("english") -> "book"
            
            // Fallback
            else -> "book"
        }
    }
    
    /**
     * Infer color from label
     */
    fun inferColorFromLabel(label: String): String {
        return when (label) {
            "wakeup" -> "#FF9800"    // Orange
            "eat" -> "#FFC107"       // Yellow
            "exercise" -> "#4CAF50"  // Green
            "rest" -> "#9C27B0"      // Purple
            "water" -> "#2196F3"     // Blue
            "book" -> "#3F51B5"      // Indigo
            "sleep" -> "#607D8B"     // Blue Grey
            "clean" -> "#00BCD4"     // Cyan
            "cook" -> "#E91E63"      // Pink
            "garden" -> "#8BC34A"    // Light Green
            else -> "#3F51B5"        // Default: Indigo
        }
    }
    
    /**
     * Extract JSON from response text
     * @return Pair of (jsonString, JSONObject?) or null if not found
     */
    fun extractJson(responseText: String): JSONObject? {
        return try {
            val jsonStart = responseText.indexOf("{")
            val jsonEnd = responseText.lastIndexOf("}") + 1
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                android.util.Log.w(TAG, "No JSON found in response")
                return null
            }
            
            val jsonString = responseText.substring(jsonStart, jsonEnd)
            JSONObject(jsonString)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error extracting JSON", e)
            null
        }
    }
}
