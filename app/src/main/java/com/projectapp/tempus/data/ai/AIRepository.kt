package com.projectapp.tempus.data.ai

import com.projectapp.tempus.BuildConfig
import com.projectapp.tempus.core.gemini.GeminiClientProvider
import com.projectapp.tempus.data.ai.dto.Content
import com.projectapp.tempus.data.ai.dto.GeminiRequest
import com.projectapp.tempus.data.ai.dto.GenerationConfig
import com.projectapp.tempus.data.ai.dto.Part
import com.projectapp.tempus.domain.model.ActionType
import com.projectapp.tempus.domain.model.AgentProposal
import com.projectapp.tempus.domain.model.ExecutionResult
import com.projectapp.tempus.domain.model.ProposedAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository for AI operations using Gemini API
 * 
 * Supports two modes:
 * - ASK MODE: Q&A only, no database writes
 * - AGENT MODE: Proposals with Accept/Cancel flow
 */
class AIRepository {
    
    private val geminiService = GeminiClientProvider.service
    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    // Conversation history for multi-turn chat
    private val conversationHistory = mutableListOf<Content>()
    
    // ============================================
    // SYSTEM INSTRUCTIONS
    // ============================================
    
    // Ask Mode: Q&A only, no actions
    private val askModeInstruction = Content(
        role = "user",
        parts = listOf(
            Part(
                text = """Bạn là Tiramisu AI, trợ lý lập kế hoạch thông minh.
                |Chế độ: CHỈ HỎI-ĐÁP (Ask Mode)
                |
                |Quy tắc:
                |1. Trả lời thông tin, giải thích cách làm
                |2. KHÔNG thực hiện bất kỳ hành động nào
                |3. Nếu người dùng yêu cầu tạo/sửa/xóa lịch → Hướng dẫn họ chuyển sang Agent Mode
                |4. Trả lời ngắn gọn, thân thiện, hữu ích
                |
                |Ví dụ khi người dùng yêu cầu hành động:
                |"Để tôi giúp bạn tạo lịch, vui lòng chuyển sang chế độ Agent bằng cách nhấn nút 🤖 Agent ở trên."
                """.trimMargin()
            )
        )
    )
    
    // Agent Mode: Proposes actions in JSON format
    private val agentModeInstruction = Content(
        role = "user",
        parts = listOf(
            Part(
                text = """Bạn là Tiramisu AI, trợ lý lập kế hoạch.
                |Chế độ: AGENT (đề xuất hành động)
                |
                |Khi người dùng yêu cầu hành động, trả về JSON với format CHÍNH XÁC:
                |{
                |  "intent": "Mô tả ngắn gọn ý định của người dùng",
                |  "actions": [
                |    {"type": "CREATE_SCHEDULE", "name": "Tên công việc", "start": "HH:MM", "end": "HH:MM", "duration": số_phút}
                |  ],
                |  "impact": "Tóm tắt ảnh hưởng (VD: Thêm 3 công việc mới)"
                |}
                |
                |Các type hợp lệ: CREATE_SCHEDULE, UPDATE_SCHEDULE, DELETE_SCHEDULE
                |
                |QUAN TRỌNG: 
                |- Đây chỉ là ĐỀ XUẤT, chưa thực hiện
                |- CHỈ trả về JSON, không thêm text giải thích
                |- Nếu không phải yêu cầu hành động, trả lời bình thường (không JSON)
                """.trimMargin()
            )
        )
    )
    
    // ============================================
    // ASK MODE METHODS
    // ============================================
    
    /**
     * Send message in Ask Mode (Q&A only)
     */
    suspend fun sendAskModeMessage(message: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userContent = Content(
                role = "user",
                parts = listOf(Part(text = message))
            )
            conversationHistory.add(userContent)
            
            val request = GeminiRequest(
                contents = conversationHistory.toList(),
                systemInstruction = askModeInstruction,
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 2048
                )
            )
            
            val response = geminiService.generateContent(apiKey, request)
            
            val responseText = response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Empty response from AI"))
            
            val aiContent = Content(
                role = "model",
                parts = listOf(Part(text = responseText))
            )
            conversationHistory.add(aiContent)
            
            Result.success(responseText)
        } catch (e: Exception) {
            if (conversationHistory.isNotEmpty()) {
                conversationHistory.removeAt(conversationHistory.size - 1)
            }
            Result.failure(e)
        }
    }
    
    // ============================================
    // AGENT MODE METHODS
    // ============================================
    
    /**
     * Sealed class for Agent mode responses
     * Can be either a structured proposal or just a text response
     */
    sealed class AgentResponse {
        data class Proposal(val proposal: AgentProposal) : AgentResponse()
        data class TextOnly(val text: String) : AgentResponse()
    }
    
    /**
     * Request a proposal from AI (dry-run, no DB writes)
     * Returns either a structured proposal or plain text response
     */
    suspend fun requestProposal(message: String): Result<AgentResponse> = withContext(Dispatchers.IO) {
        try {
            val contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = message))
                )
            )
            
            val request = GeminiRequest(
                contents = contents,
                systemInstruction = agentModeInstruction,
                generationConfig = GenerationConfig(
                    temperature = 0.5f,  // Lower for structured output
                    maxOutputTokens = 1024
                )
            )
            
            val response = geminiService.generateContent(apiKey, request)
            
            val responseText = response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Empty response from AI"))
            
            // Try to parse JSON response into AgentProposal
            val proposal = parseProposal(responseText)
            
            if (proposal != null && proposal.actions.isNotEmpty()) {
                // Successfully parsed a proposal with actions
                Result.success(AgentResponse.Proposal(proposal))
            } else {
                // AI responded with text (not an action request)
                Result.success(AgentResponse.TextOnly(responseText))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Parse AI response into AgentProposal
     */
    private fun parseProposal(responseText: String): AgentProposal? {
        return try {
            // Find JSON object in response
            val jsonStart = responseText.indexOf("{")
            val jsonEnd = responseText.lastIndexOf("}") + 1
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) return null
            
            val jsonString = responseText.substring(jsonStart, jsonEnd)
            val json = JSONObject(jsonString)
            
            val intent = json.optString("intent", "Thực hiện yêu cầu của bạn")
            val impact = json.optString("impact", "Thay đổi lịch trình")
            
            val actionsArray = json.optJSONArray("actions") ?: JSONArray()
            val actions = mutableListOf<ProposedAction>()
            
            for (i in 0 until actionsArray.length()) {
                val actionJson = actionsArray.getJSONObject(i)
                val typeStr = actionJson.optString("type", "CREATE_SCHEDULE")
                val type = try {
                    ActionType.valueOf(typeStr)
                } catch (e: Exception) {
                    ActionType.CREATE_SCHEDULE
                }
                
                val data = mutableMapOf<String, Any?>()
                actionJson.keys().forEach { key ->
                    if (key != "type") {
                        data[key] = actionJson.opt(key)
                    }
                }
                
                val name = actionJson.optString("name", "Công việc")
                actions.add(ProposedAction(
                    type = type,
                    description = name,
                    data = data
                ))
            }
            
            AgentProposal(
                intent = intent,
                actions = actions,
                impact = impact,
                rawResponse = responseText
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Execute a proposal (Step 2 of Agent Mode flow)
     * This is where database writes happen
     * 
     * TODO: Integrate with ScheduleRepository for actual DB writes
     */
    suspend fun executeProposal(proposal: AgentProposal): Result<ExecutionResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val appliedChanges = mutableListOf<String>()
        
        try {
            for (action in proposal.actions) {
                when (action.type) {
                    ActionType.CREATE_SCHEDULE -> {
                        val scheduleData = action.getScheduleData()
                        if (scheduleData != null) {
                            // TODO: Call scheduleRepository.insertSchedule(...)
                            appliedChanges.add("Tạo: ${scheduleData.name} (${scheduleData.startTime})")
                        }
                    }
                    ActionType.UPDATE_SCHEDULE -> {
                        appliedChanges.add("Cập nhật: ${action.description}")
                    }
                    ActionType.DELETE_SCHEDULE -> {
                        appliedChanges.add("Xóa: ${action.description}")
                    }
                    else -> {
                        appliedChanges.add(action.description)
                    }
                }
            }
            
            Result.success(ExecutionResult(
                success = true,
                changesApplied = appliedChanges,
                executionTimeMs = System.currentTimeMillis() - startTime
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============================================
    // LEGACY METHODS (for backward compatibility)
    // ============================================
    
    /**
     * Send a single message (legacy - uses Ask Mode)
     */
    suspend fun sendMessage(message: String): Result<String> = sendAskModeMessage(message)
    
    /**
     * Request schedule suggestions (legacy)
     */
    suspend fun requestScheduleSuggestions(userRequest: String): Result<String> = withContext(Dispatchers.IO) {
        requestProposal(userRequest).map { response ->
            when (response) {
                is AgentResponse.Proposal -> response.proposal.rawResponse
                is AgentResponse.TextOnly -> response.text
            }
        }
    }
    
    /**
     * Clear conversation history
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
}

/**
 * Simple chat message model for UI layer
 */
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
