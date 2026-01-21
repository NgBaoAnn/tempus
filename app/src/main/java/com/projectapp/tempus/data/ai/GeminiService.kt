package com.projectapp.tempus.data.ai

import com.projectapp.tempus.data.ai.dto.GeminiRequest
import com.projectapp.tempus.data.ai.dto.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit service interface for Gemini API
 * 
 * Endpoint: https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
 * Documentation: https://ai.google.dev/api/generate-content
 */
interface GeminiService {
    
    /**
     * Generate content using Gemini Flash Latest (verified working)
     * 
     * @param apiKey API key passed as query parameter
     * @param request The request body containing conversation contents
     * @return GeminiResponse with generated content
     */
    @POST("v1beta/models/gemini-flash-latest:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
    
    /**
     * Generate content using Gemini 1.5 Flash model (fallback option)
     */
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContentWithFlash15(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
    
    /**
     * Generate content using Gemini 1.5 Pro model (higher quality)
     */
    @POST("v1beta/models/gemini-1.5-pro:generateContent")
    suspend fun generateContentWithPro(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
