package com.projectapp.tempus.data.ai.repo

import com.projectapp.tempus.core.gemini.GeminiApiKeyManager
import com.projectapp.tempus.core.gemini.GeminiClientProvider
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.ai.dto.Content
import com.projectapp.tempus.data.ai.dto.GeminiRequest
import com.projectapp.tempus.data.ai.dto.GenerationConfig
import com.projectapp.tempus.data.ai.dto.Part
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.data.schedule.dto.RepeatType
import com.projectapp.tempus.data.schedule.dto.SourceType
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

/**
 * Repository handling Personalization Schedule Generation.
 * Generates optimized schedules based on user preferences and tasks.
 */
class PersonalizationRepository(
    private val scheduleRepository: ScheduleRepository
) {
    
    private val geminiService = GeminiClientProvider.service
    private val apiKeyManager = GeminiApiKeyManager
    
    private val maxRetries = 8
    
    /**
     * Generate schedule preview from personalization settings
     * AI will analyze tasks and create optimal time slots based on:
     * - Wake up / sleep time
     * - Task priorities
     * - Estimated durations
     * 
     * @param wakeUpTime User's wake up time (HH:mm)
     * @param sleepTime User's sleep time (HH:mm)
     * @param tasks List of tasks to schedule
     * @param activeDays Days to apply (1=Mon, 7=Sun)
     * @return Result with list of schedule slots for preview (NOT saved yet)
     */
    suspend fun generateSchedulePreview(
        wakeUpTime: String,
        sleepTime: String,
        tasks: List<PersonalizationTaskInput>,
        activeDays: List<Int>
    ): Result<List<ScheduleSlot>> = withContext(Dispatchers.IO) {
        try {
            if (tasks.isEmpty()) {
                return@withContext Result.failure(Exception("Không có công việc nào để lên lịch"))
            }
            
            // Build task list for AI
            val taskListStr = tasks.mapIndexed { index, task ->
                """${index + 1}. "${task.name}" - ${task.estimatedMinutes} phút (${task.priority}) ${if (task.description.isNotEmpty()) "- ${task.description}" else ""} """
            }.joinToString("\n")
            
            val prompt = """
                |THÔNG TIN NGƯỜI DÙNG:
                |- Thức dậy: $wakeUpTime
                |- Đi ngủ: $sleepTime
                |- Ngày áp dụng: ${activeDays.joinToString(", ")}
                |
                |CÁC CÔNG VIỆC CẦN SẮP XẾP:
                |$taskListStr
                |
                |YÊU CẦU:
                |Hãy tạo lịch trình tối ưu cho các công việc trên. Sắp xếp sao cho:
                |1. Công việc ưu tiên cao nên làm vào buổi sáng khi năng lượng cao
                |2. Có thời gian nghỉ ngơi giữa các công việc (15-30 phút)
                |3. Không xếp lịch ngoài giờ thức - ngủ
                |4. Sử dụng đúng thời gian ước tính cho từng công việc
                |5. Thêm thời gian cho bữa ăn (sáng, trưa, tối) nếu cần
                |
                |Trả lời theo JSON format sau (KHÔNG có markdown):
                |{
                |  "schedule": [
                |    {
                |      "name": "Tên công việc",
                |      "start_time": "HH:mm",
                |      "end_time": "HH:mm",
                |      "priority": "high/medium/low",
                |      "label": "book/exercise/rest/cook/clean/garden/eat/water/wakeup/sleep"
                |    }
                |  ]
                |}
            """.trimMargin()
            
            android.util.Log.d("PersonalizationRepository", "Generating schedule preview...")
            
            val responseText = executeWithRetry { apiKey ->
                val contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = prompt))
                    )
                )
                
                val request = GeminiRequest(
                    contents = contents,
                    generationConfig = GenerationConfig(
                        temperature = 0.5f,
                        maxOutputTokens = 2048
                    )
                )
                
                val response = geminiService.generateContent(apiKey, request)
                
                response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Empty response from AI")
            }.getOrElse { e ->
                android.util.Log.e("PersonalizationRepository", "Error in generateSchedulePreview", e)
                return@withContext Result.failure(e)
            }
            
            // Parse JSON response
            val slots = parseScheduleSlots(responseText)
            if (slots.isEmpty()) {
                return@withContext Result.failure(Exception("AI không thể tạo lịch. Vui lòng thử lại."))
            }
            
            Result.success(slots)
        } catch (e: Exception) {
            android.util.Log.e("PersonalizationRepository", "Error generating schedule preview", e)
            Result.failure(e)
        }
    }
    
    /**
     * Save generated schedule slots to database after user confirmation
     * @param slots List of schedule slots from preview
     * @param activeDays Days to apply (1=Mon, 7=Sun)
     * @return Result with number of schedules created
     */
    suspend fun saveGeneratedSchedulesToDatabase(
        slots: List<ScheduleSlot>,
        activeDays: List<Int>
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now()
            val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id 
                ?: return@withContext Result.failure(Exception("User not logged in"))
            
            var schedulesCreated = 0
            
            for (slot in slots) {
                val label = slot.label.ifEmpty { AIHelpers.inferLabelFromTitle(slot.name) }
                val color = AIHelpers.inferColorFromLabel(label)
                
                android.util.Log.d("PersonalizationRepository", "Processing slot: ${slot.name}, startTime='${slot.startTime}', endTime='${slot.endTime}'")
                
                // Calculate duration
                val startParts = slot.startTime.split(":")
                val endParts = slot.endTime.split(":")
                val startMinutes = startParts.getOrNull(0)?.toIntOrNull() ?: 0
                val startMins = startParts.getOrNull(1)?.toIntOrNull() ?: 0
                val endMinutes = endParts.getOrNull(0)?.toIntOrNull() ?: 0
                val endMins = endParts.getOrNull(1)?.toIntOrNull() ?: 0
                val totalStartMinutes = startMinutes * 60 + startMins
                val totalEndMinutes = endMinutes * 60 + endMins
                val durationMinutes = if (totalEndMinutes > totalStartMinutes) totalEndMinutes - totalStartMinutes else 60
                val durationStr = String.format("%02d:%02d:00", durationMinutes / 60, durationMinutes % 60)
                
                // Format start_time properly as HH:mm:ss
                val startTimeFormatted = if (slot.startTime.contains(":")) {
                    if (slot.startTime.count { it == ':' } == 1) {
                        slot.startTime + ":00"
                    } else {
                        slot.startTime
                    }
                } else {
                    "08:00:00"
                }
                
                val priority = when (slot.priority) {
                    "high" -> "high"
                    "low" -> "low"
                    else -> "medium"
                }
                
                // Determine repeat type based on active days
                val repeatType = if (activeDays.size == 7) {
                    RepeatType.daily.name
                } else {
                    RepeatType.custom.name
                }
                
                val repeatDays = if (activeDays.size < 7) {
                    activeDays.joinToString(",")
                } else null
                
                // Format start_time_date as full timestamp
                val systemOffset = java.time.ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())
                val startTimeDateFull = "${today}T${startTimeFormatted}${systemOffset}"
                
                val scheduleRow = mapOf(
                    "id" to UUID.randomUUID().toString(),
                    "user_id" to userId,
                    "name_schedule" to slot.name,
                    "description" to "",
                    "priority" to priority,
                    "label" to label,
                    "start_time_date" to startTimeDateFull,
                    "implementation_time" to durationStr,
                    "repeat" to repeatType,
                    "repeat_days" to repeatDays,
                    "color" to color,
                    "source" to SourceType.PERSONALIZATION.name,
                    "created_at" to java.time.OffsetDateTime.now().toString()
                )
                
                scheduleRepository.insertSchedule(scheduleRow)
                schedulesCreated++
                
                android.util.Log.d("PersonalizationRepository", "Created schedule: ${slot.name} at $startTimeFormatted")
            }
            
            Result.success(schedulesCreated)
        } catch (e: Exception) {
            android.util.Log.e("PersonalizationRepository", "Error saving generated schedules", e)
            Result.failure(e)
        }
    }
    
    /**
     * Parse JSON response to schedule slots
     */
    private fun parseScheduleSlots(responseText: String): List<ScheduleSlot> {
        try {
            val jsonStart = responseText.indexOf("{")
            val jsonEnd = responseText.lastIndexOf("}") + 1
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                return emptyList()
            }
            
            val jsonStr = responseText.substring(jsonStart, jsonEnd)
            val json = JSONObject(jsonStr)
            val scheduleArray = json.optJSONArray("schedule") ?: return emptyList()
            
            val slots = mutableListOf<ScheduleSlot>()
            for (i in 0 until scheduleArray.length()) {
                val item = scheduleArray.getJSONObject(i)
                slots.add(ScheduleSlot(
                    name = item.optString("name", ""),
                    startTime = item.optString("start_time", "08:00"),
                    endTime = item.optString("end_time", "09:00"),
                    priority = item.optString("priority", "medium"),
                    label = item.optString("label", "book")
                ))
            }
            
            return slots
        } catch (e: Exception) {
            android.util.Log.e("PersonalizationRepository", "Error parsing schedule slots: ${e.message}")
            return emptyList()
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
                    android.util.Log.w("PersonalizationRepository", "Rate limit hit on attempt ${attempt + 1}, rotating key...")
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
