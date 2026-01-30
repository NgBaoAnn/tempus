package com.projectapp.tempus.data.ai

import com.projectapp.tempus.data.ai.dto.GeminiRequest
import com.projectapp.tempus.data.ai.dto.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query


interface GeminiService {
    
    
    @POST("v1beta/models/gemini-flash-latest:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
    
    
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContentWithFlash15(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
    
    
    @POST("v1beta/models/gemini-1.5-pro:generateContent")
    suspend fun generateContentWithPro(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
