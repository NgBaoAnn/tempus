package com.projectapp.tempus.data.ai.dto

import com.google.gson.annotations.SerializedName

/**
 * Request DTO for Gemini API
 * Reference: https://ai.google.dev/api/generate-content
 */
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

data class Content(
    val role: String,  // "user" or "model"
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GenerationConfig(
    val temperature: Float = 0.7f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val maxOutputTokens: Int = 2048
)
