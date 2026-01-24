package com.projectapp.tempus.data.ai.dto

import com.google.gson.annotations.SerializedName

/**
 * Response DTO for Gemini API
 * Reference: https://ai.google.dev/api/generate-content
 */
data class GeminiResponse(
    val candidates: List<Candidate>?,
    val promptFeedback: PromptFeedback? = null,
    val usageMetadata: UsageMetadata? = null
)

data class Candidate(
    val content: Content,
    val finishReason: String? = null,
    val index: Int = 0,
    val safetyRatings: List<SafetyRating>? = null
)

data class PromptFeedback(
    val safetyRatings: List<SafetyRating>? = null,
    val blockReason: String? = null
)

data class SafetyRating(
    val category: String,
    val probability: String
)

data class UsageMetadata(
    val promptTokenCount: Int = 0,
    val candidatesTokenCount: Int = 0,
    val totalTokenCount: Int = 0
)

/**
 * Error response from Gemini API
 */
data class GeminiErrorResponse(
    val error: GeminiError
)

data class GeminiError(
    val code: Int,
    val message: String,
    val status: String
)
