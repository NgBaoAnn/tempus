package com.projectapp.tempus.data.ai.repo

import com.projectapp.tempus.core.gemini.GeminiApiKeyManager
import com.projectapp.tempus.core.gemini.GeminiClientProvider
import com.projectapp.tempus.data.ai.dto.Content
import com.projectapp.tempus.data.ai.dto.GeminiRequest
import com.projectapp.tempus.data.ai.dto.GenerationConfig
import com.projectapp.tempus.data.ai.dto.Part
import com.projectapp.tempus.data.user.UserProfileCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository handling Ask Mode (Q&A) functionality.
 * Manages conversation history, voice commands, and chat titles.
 */
class AskModeRepository {
    
    private val geminiService = GeminiClientProvider.service
    private val apiKeyManager = GeminiApiKeyManager
    
    // Conversation history for multi-turn chat
    private val conversationHistory = mutableListOf<Content>()
    
    private val maxRetries = 8
    
    /**
     * Send message in Ask Mode with conversation context
     */
    suspend fun sendMessage(message: String): Result<String> = withContext(Dispatchers.IO) {
        val userContent = Content(
            role = "user",
            parts = listOf(Part(text = message))
        )
        conversationHistory.add(userContent)
        
        val result = executeWithRetry { apiKey ->
            val request = GeminiRequest(
                contents = conversationHistory.toList(),
                systemInstruction = AIPromptProvider.getAskModeInstruction(),
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 2048
                )
            )
            
            val response = geminiService.generateContent(apiKey, request)
            
            response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from AI")
        }
        
        result.onSuccess { responseText ->
            val aiContent = Content(
                role = "model",
                parts = listOf(Part(text = responseText))
            )
            conversationHistory.add(aiContent)
        }.onFailure {
            // Remove last message on failure
            if (conversationHistory.isNotEmpty()) {
                conversationHistory.removeAt(conversationHistory.size - 1)
            }
        }
        
        result
    }
    
    /**
     * Parse voice command to JSON format
     */
    suspend fun parseVoiceCommand(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        executeWithRetry { apiKey ->
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = prompt))
                    )
                ),
                systemInstruction = Content(
                    role = "user",
                    parts = listOf(
                        Part(text = """
                            |Bạn là JSON parser. CHỈ trả về JSON, KHÔNG có text khác.
                            |Output phải là valid JSON object bắt đầu bằng { và kết thúc bằng }
                        """.trimMargin())
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.3f,
                    maxOutputTokens = 512
                )
            )
            
            val response = geminiService.generateContent(apiKey, request)
            
            response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from AI")
        }
    }
    
    /**
     * Generate a short title for a chat conversation
     */
    suspend fun generateChatTitle(firstMessage: String): Result<String> = withContext(Dispatchers.IO) {
        val lang = UserProfileCache.getLanguage() ?: "vi"
        val fallbackTitle = if (lang == "en") "New Chat" else "Cuộc trò chuyện mới"
        
        try {
            android.util.Log.d("AskModeRepository", "Generating chat title for: ${firstMessage.take(50)}...")
            
            val prompt = if (lang == "en") {
                """Generate a very short title (max 5 words, under 30 characters) for a conversation that starts with:
                |"$firstMessage"
                |
                |Rules:
                |- Just return the title, no quotes, no explanation
                |- Be concise and descriptive
                |- Use the same language as the message
                """.trimMargin()
            } else {
                """Tạo một tiêu đề rất ngắn gọn (tối đa 5 từ, dưới 30 ký tự) cho cuộc trò chuyện bắt đầu với:
                |"$firstMessage"
                |
                |Quy tắc:
                |- Chỉ trả về tiêu đề, không dấu ngoặc kép, không giải thích
                |- Ngắn gọn và mô tả được nội dung
                |- Dùng cùng ngôn ngữ với tin nhắn
                """.trimMargin()
            }
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = prompt))
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.5f,
                    maxOutputTokens = 50
                )
            )
            
            val response = geminiService.generateContent(
                apiKey = apiKeyManager.getCurrentKey(),
                request = request
            )
            
            val title = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()
                ?.take(30)
            
            if (title.isNullOrBlank()) {
                Result.success(fallbackTitle)
            } else {
                Result.success(title)
            }
        } catch (e: Exception) {
            android.util.Log.e("AskModeRepository", "Title generation failed: ${e.message}", e)
            Result.success(fallbackTitle)
        }
    }
    
    /**
     * Clear conversation history
     */
    fun clearHistory() {
        conversationHistory.clear()
    }
    
    /**
     * Get conversation history as ChatMessage list
     */
    fun getHistory(): List<ChatMessage> {
        return conversationHistory.map { content ->
            ChatMessage(
                text = content.parts.firstOrNull()?.text ?: "",
                isFromUser = content.role == "user"
            )
        }
    }
    
    // Retry logic with API key rotation
    private suspend fun <T> executeWithRetry(
        apiCall: suspend (apiKey: String) -> T
    ): Result<T> {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
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
                
                val isRateLimitError = errorMessage.contains("429") || 
                                      errorMessage.contains("rate limit") ||
                                      errorMessage.contains("quota exceeded") ||
                                      errorMessage.contains("resource_exhausted")
                
                if (isRateLimitError) {
                    android.util.Log.w("AskModeRepository", "Rate limit hit on attempt ${attempt + 1}, rotating key...")
                } else {
                    android.util.Log.e("AskModeRepository", "Non-rate-limit error: ${e.message}")
                    return Result.failure(e)
                }
            }
        }
        
        return Result.failure(
            lastException ?: Exception("All API keys exhausted due to rate limits")
        )
    }
}
