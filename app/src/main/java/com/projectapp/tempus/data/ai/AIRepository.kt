package com.projectapp.tempus.data.ai

import com.projectapp.tempus.BuildConfig
import com.projectapp.tempus.core.gemini.GeminiClientProvider
import com.projectapp.tempus.data.ai.dto.Content
import com.projectapp.tempus.data.ai.dto.GeminiRequest
import com.projectapp.tempus.data.ai.dto.GenerationConfig
import com.projectapp.tempus.data.ai.dto.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for AI operations using Gemini API
 * 
 * Provides methods to send messages and maintain conversation history
 * for the AI chat feature in the app.
 */
class AIRepository {
    
    private val geminiService = GeminiClientProvider.service
    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    // Conversation history for multi-turn chat
    private val conversationHistory = mutableListOf<Content>()
    
    // System instruction for the AI assistant
    private val systemInstruction = Content(
        role = "user",
        parts = listOf(
            Part(
                text = """Bạn là Tiramisu AI, một trợ lý lập kế hoạch thông minh. 
                |Nhiệm vụ của bạn là giúp người dùng:
                |1. Lên lịch và quản lý công việc hàng ngày
                |2. Đề xuất thời gian phù hợp cho các hoạt động
                |3. Nhắc nhở về deadline và ưu tiên công việc
                |4. Đưa ra lời khuyên về quản lý thời gian
                |
                |Hãy trả lời ngắn gọn, thân thiện và hữu ích.
                |Khi đề xuất lịch trình, hãy dùng format rõ ràng với thời gian cụ thể.""".trimMargin()
            )
        )
    )
    
    // Special prompt for schedule suggestions (returns JSON format)
    private val scheduleSystemInstruction = Content(
        role = "user",
        parts = listOf(
            Part(
                text = """Bạn là Tiramisu AI, trợ lý lập kế hoạch. 
                |Khi người dùng yêu cầu lên lịch, hãy trả về JSON array với format:
                |[{"name": "Tên công việc", "start": "HH:MM", "end": "HH:MM", "duration": số_phút}]
                |
                |Ví dụ:
                |[
                |  {"name": "Học Toán", "start": "08:00", "end": "09:30", "duration": 90},
                |  {"name": "Nghỉ giải lao", "start": "09:30", "end": "09:45", "duration": 15},
                |  {"name": "Học Lý", "start": "09:45", "end": "11:00", "duration": 75}
                |]
                |
                |Chỉ trả về JSON, không thêm text giải thích.""".trimMargin()
            )
        )
    )
    
    /**
     * Send a single message and get AI response
     * 
     * @param message User's message text
     * @return Result containing AI response text or error
     */
    suspend fun sendMessage(message: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Add user message to history
            val userContent = Content(
                role = "user",
                parts = listOf(Part(text = message))
            )
            conversationHistory.add(userContent)
            
            // Build request with history
            val request = GeminiRequest(
                contents = conversationHistory.toList(),
                systemInstruction = systemInstruction,
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 2048
                )
            )
            
            // Call API
            val response = geminiService.generateContent(apiKey, request)
            
            // Extract response text
            val responseText = response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Empty response from AI"))
            
            // Add AI response to history
            val aiContent = Content(
                role = "model",
                parts = listOf(Part(text = responseText))
            )
            conversationHistory.add(aiContent)
            
            Result.success(responseText)
        } catch (e: Exception) {
            // Remove the failed user message from history
            if (conversationHistory.isNotEmpty()) {
                conversationHistory.removeAt(conversationHistory.size - 1)
            }
            Result.failure(e)
        }
    }
    
    /**
     * Send a message with custom conversation history
     * Useful for providing context from previous sessions
     * 
     * @param messages List of ChatMessage representing conversation history
     * @return Result containing AI response text or error
     */
    suspend fun sendMessageWithHistory(messages: List<ChatMessage>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contents = messages.map { msg ->
                Content(
                    role = if (msg.isFromUser) "user" else "model",
                    parts = listOf(Part(text = msg.text))
                )
            }
            
            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction,
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 2048
                )
            )
            
            val response = geminiService.generateContent(apiKey, request)
            
            val responseText = response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Empty response from AI"))
            
            Result.success(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear conversation history
     * Call this when starting a new chat session
     */
    fun clearHistory() {
        conversationHistory.clear()
    }
    
    /**
     * Get current conversation history
     */
    fun getHistory(): List<ChatMessage> {
        return conversationHistory.map { content ->
            ChatMessage(
                text = content.parts.firstOrNull()?.text ?: "",
                isFromUser = content.role == "user"
            )
        }
    }
    
    /**
     * Request schedule suggestions from AI
     * Uses special prompt to get JSON formatted response
     * 
     * @param userRequest User's scheduling request (e.g., "Lên lịch học bài cho tôi")
     * @return Result containing raw AI response for parsing
     */
    suspend fun requestScheduleSuggestions(userRequest: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = userRequest))
                )
            )
            
            val request = GeminiRequest(
                contents = contents,
                systemInstruction = scheduleSystemInstruction,
                generationConfig = GenerationConfig(
                    temperature = 0.5f,  // Lower temperature for more structured output
                    maxOutputTokens = 1024
                )
            )
            
            val response = geminiService.generateContent(apiKey, request)
            
            val responseText = response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Empty response from AI"))
            
            Result.success(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Simple chat message model for UI layer
 */
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
