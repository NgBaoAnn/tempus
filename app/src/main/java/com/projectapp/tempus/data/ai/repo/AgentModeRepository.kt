package com.projectapp.tempus.data.ai.repo

import com.projectapp.tempus.core.gemini.GeminiApiKeyManager
import com.projectapp.tempus.core.gemini.GeminiClientProvider
import com.projectapp.tempus.data.ai.ActivityClassifier
import com.projectapp.tempus.data.ai.dto.Content
import com.projectapp.tempus.data.ai.dto.GeminiRequest
import com.projectapp.tempus.data.ai.dto.GenerationConfig
import com.projectapp.tempus.data.ai.dto.Part
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.domain.model.ActionType
import com.projectapp.tempus.domain.model.AgentProposal
import com.projectapp.tempus.domain.model.ExecutionResult
import com.projectapp.tempus.domain.model.ProposedAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repository handling Agent Mode functionality.
 * Manages schedule proposals, context building, and action execution.
 */
class AgentModeRepository(
    private val scheduleRepository: ScheduleRepository,
    private val userId: String
) {
    
    private val geminiService = GeminiClientProvider.service
    private val apiKeyManager = GeminiApiKeyManager
    
    private val maxRetries = 8
    
    /**
     * Response from Agent Mode - either a proposal or text-only response
     */
    sealed class AgentResponse {
        data class Proposal(val proposal: AgentProposal) : AgentResponse()
        data class TextOnly(val text: String) : AgentResponse()
    }
    
    /**
     * Request AI to analyze message and generate schedule proposals
     */
    suspend fun requestProposal(message: String): Result<AgentResponse> = withContext(Dispatchers.IO) {
        try {
            val scheduleContext = buildScheduleContext()
            
            android.util.Log.d("AgentModeRepository", "Schedule context: $scheduleContext")
            
            val contextMessage = if (scheduleContext.isNotBlank()) {
                """$message

[CONTEXT - Lịch trình hiện tại của người dùng]
$scheduleContext
[END CONTEXT]"""
            } else {
                """$message

[CONTEXT - Lịch trình hiện tại của người dùng]
Không thể tải lịch trình. Vui lòng thử lại.
[END CONTEXT]"""
            }
            
            val responseText = executeWithRetry { apiKey ->
                val contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = contextMessage))
                    )
                )
                
                val request = GeminiRequest(
                    contents = contents,
                    systemInstruction = AIPromptProvider.getAgentModeInstruction(),
                    generationConfig = GenerationConfig(
                        temperature = 0.5f,
                        maxOutputTokens = 4096
                    )
                )
                
                val response = geminiService.generateContent(apiKey, request)
                
                response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Empty response from AI")
            }.getOrElse { e ->
                android.util.Log.e("AgentModeRepository", "Error in requestProposal", e)
                return@withContext Result.failure(e)
            }
            
            val proposal = parseProposal(responseText)
            
            if (proposal != null && proposal.actions.isNotEmpty()) {
                Result.success(AgentResponse.Proposal(proposal))
            } else {
                Result.success(AgentResponse.TextOnly(responseText))
            }
        } catch (e: Exception) {
            android.util.Log.e("AgentModeRepository", "Error in requestProposal", e)
            Result.failure(e)
        }
    }
    
    /**
     * Build context string from user's current schedules
     */
    private suspend fun buildScheduleContext(): String {
        return try {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val allSchedules = scheduleRepository.getAllSchedules(userId)
            
            if (allSchedules.isEmpty()) return "Không có lịch trình nào."
            
            val scheduleLines = allSchedules.map { schedule ->
                val dateTimeStr = schedule.startTimeDate
                val originalDate = try {
                    if (dateTimeStr.contains("T")) {
                        dateTimeStr.substringBefore("T")
                    } else {
                        dateTimeStr.split(" ").firstOrNull() ?: "không xác định"
                    }
                } catch (e: Exception) {
                    "không xác định"
                }
                
                val timeInfo = try {
                    if (dateTimeStr.contains("T")) {
                        dateTimeStr.substringAfter("T").take(5)
                    } else {
                        dateTimeStr.split(" ").getOrNull(1)?.take(5) ?: "?"
                    }
                } catch (e: Exception) {
                    "?"
                }
                
                val repeatType = schedule.repeat.name
                val appliesToday = when (repeatType) {
                    "daily" -> true
                    "weekly" -> true
                    "once" -> originalDate == today
                    else -> true
                }
                
                val todayMarker = if (appliesToday) "[HÔM NAY] " else ""
                val repeatInfo = if (repeatType != "once") "(lặp: $repeatType)" else ""
                
                "$todayMarker- ID: ${schedule.id}, Tên: ${schedule.name}, Giờ: $timeInfo, $repeatInfo"
            }
            
            """Hôm nay: $today
LƯU Ý: Schedule có lặp (daily/weekly) SẼ xuất hiện hôm nay dù ngày tạo gốc khác.
Danh sách lịch trình:
${scheduleLines.joinToString("\n")}"""
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Parse JSON proposal from AI response
     */
    private fun parseProposal(responseText: String): AgentProposal? {
        return try {
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
            android.util.Log.e("AgentModeRepository", "Error parsing proposal", e)
            null
        }
    }
    
    /**
     * Execute approved proposal actions
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
                            val dateStr = scheduleData.date ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val startTimeDate = "${dateStr}T${scheduleData.startTime}:00+07:00"
                            
                            val hours = scheduleData.durationMinutes / 60
                            val minutes = scheduleData.durationMinutes % 60
                            val implementationTime = String.format("%02d:%02d:00", hours, minutes)
                            
                            val aiLabel = action.data["label"] as? String
                            val aiColor = action.data["color"] as? String
                            
                            val (fallbackLabel, fallbackColor) = ActivityClassifier.classifyWithColor(scheduleData.name)
                            
                            val finalLabel = if (!aiLabel.isNullOrBlank()) aiLabel else fallbackLabel.name
                            val finalColor = if (!aiColor.isNullOrBlank() && aiColor.startsWith("#")) aiColor else fallbackColor
                            
                            val row = mapOf(
                                "user_id" to userId,
                                "name_schedule" to scheduleData.name,
                                "start_time_date" to startTimeDate,
                                "implementation_time" to implementationTime,
                                "repeat" to "once",
                                "label" to finalLabel,
                                "color" to finalColor,
                                "source" to "ai"
                            )
                            
                            val inserted = scheduleRepository.insertSchedule(row)
                            appliedChanges.add("✅ Tạo: ${scheduleData.name} (${scheduleData.startTime}) - ID: ${inserted.id}")
                        }
                    }
                    ActionType.UPDATE_SCHEDULE -> {
                        val taskId = action.data["id"] as? String ?: action.data["taskId"] as? String
                        if (taskId != null) {
                            val dbFields = mutableMapOf<String, Any?>()
                            
                            action.data.forEach { (key, value) ->
                                when (key) {
                                    "name" -> dbFields["name_schedule"] = value
                                    "startTime" -> {
                                        val time = value as? String ?: return@forEach
                                        val date = action.data["date"] as? String 
                                            ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                                        dbFields["start_time_date"] = "${date}T${time}:00+07:00"
                                    }
                                    "duration" -> {
                                        val mins = (value as? Number)?.toInt() ?: return@forEach
                                        val hrs = mins / 60
                                        val m = mins % 60
                                        dbFields["implementation_time"] = String.format("%02d:%02d:00", hrs, m)
                                    }
                                    "label" -> dbFields["label"] = value
                                    "color" -> dbFields["color"] = value
                                    "date" -> {
                                        if (!dbFields.containsKey("start_time_date")) {
                                            val newDate = value as? String ?: return@forEach
                                            dbFields["start_time_date"] = "${newDate}T00:00:00+07:00"
                                        }
                                    }
                                    "id", "taskId" -> { }
                                    else -> { }
                                }
                            }
                            
                            if (dbFields.isNotEmpty()) {
                                scheduleRepository.updateSchedule(taskId, dbFields)
                                appliedChanges.add("✅ Cập nhật: ${action.description}")
                            } else {
                                appliedChanges.add("⚠️ Không có field hợp lệ để cập nhật: ${action.description}")
                            }
                        } else {
                            appliedChanges.add("⚠️ Bỏ qua cập nhật (thiếu ID): ${action.description}")
                        }
                    }
                    ActionType.DELETE_SCHEDULE -> {
                        val taskId = action.data["id"] as? String ?: action.data["taskId"] as? String
                        if (taskId != null) {
                            scheduleRepository.deleteSchedule(taskId)
                            appliedChanges.add("✅ Xóa hoàn toàn: ${action.description}")
                        } else {
                            appliedChanges.add("⚠️ Bỏ qua xóa (thiếu ID): ${action.description}")
                        }
                    }
                    ActionType.SKIP_INSTANCE -> {
                        val taskId = action.data["id"] as? String ?: action.data["taskId"] as? String
                        val date = action.data["date"] as? String 
                            ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        
                        if (taskId != null) {
                            scheduleRepository.upsertScheduleItem(
                                taskId = taskId,
                                date = date,
                                status = com.projectapp.tempus.data.schedule.dto.StatusType.delete
                            )
                            appliedChanges.add("✅ Bỏ qua ngày $date: ${action.description}")
                        } else {
                            appliedChanges.add("⚠️ Bỏ qua (thiếu ID): ${action.description}")
                        }
                    }
                    else -> {
                        appliedChanges.add("ℹ️ ${action.description}")
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
                    android.util.Log.w("AgentModeRepository", "Rate limit hit on attempt ${attempt + 1}, rotating key...")
                } else {
                    return Result.failure(e)
                }
            }
        }
        
        return Result.failure(
            lastException ?: Exception("All API keys exhausted due to rate limits")
        )
    }
}
