package com.projectapp.tempus.data.ai.repo

import com.projectapp.tempus.core.gemini.GeminiApiKeyManager
import com.projectapp.tempus.core.gemini.GeminiClientProvider
import com.projectapp.tempus.data.ai.dto.Content
import com.projectapp.tempus.data.ai.dto.GeminiRequest
import com.projectapp.tempus.data.ai.dto.GenerationConfig
import com.projectapp.tempus.data.ai.dto.Part
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.domain.model.EnergyContext
import com.projectapp.tempus.domain.model.LifePlan
import com.projectapp.tempus.domain.model.LifePlanProposal
import com.projectapp.tempus.domain.model.Milestone
import com.projectapp.tempus.domain.model.MilestoneStatus
import com.projectapp.tempus.domain.model.PlanStatus
import com.projectapp.tempus.domain.model.ScheduledTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Repository handling Life Planner Mode functionality.
 * Manages long-term goal planning with milestones and scheduled tasks.
 */
class LifePlannerRepository(
    private val scheduleRepository: ScheduleRepository,
    private val userId: String
) {
    
    private val geminiService = GeminiClientProvider.service
    private val apiKeyManager = GeminiApiKeyManager
    
    private val maxRetries = 8
    
    /**
     * Request AI to generate a life plan for the given goal
     */
    suspend fun requestLifePlan(
        goal: String,
        energyContext: EnergyContext? = null
    ): Result<LifePlanProposal> = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now()
            
            val contextPart = energyContext?.let {
                """
                |
                |[THÔNG TIN NGƯỜI DÙNG]
                |${it.toContextString()}
                """.trimMargin()
            } ?: ""
            
            val fullMessage = """
                |Ngày hôm nay: $today
                |
                |Mục tiêu: $goal
                |$contextPart
                |
                |Hãy tạo kế hoạch chi tiết cho mục tiêu này.
            """.trimMargin()
            
            android.util.Log.d("LifePlannerRepository", "Requesting life plan for: $goal")
            
            val responseText = executeWithRetry { apiKey ->
                val contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = fullMessage))
                    )
                )
                
                val request = GeminiRequest(
                    contents = contents,
                    systemInstruction = AIPromptProvider.getLifePlannerInstruction(),
                    generationConfig = GenerationConfig(
                        temperature = 0.6f,
                        maxOutputTokens = 4096
                    )
                )
                
                val response = geminiService.generateContent(apiKey, request)
                
                response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Empty response from AI")
            }.getOrElse { e ->
                android.util.Log.e("LifePlannerRepository", "Error in requestLifePlan", e)
                return@withContext Result.failure(e)
            }
            
            val proposal = parseLifePlanResponse(responseText, today)
                ?: return@withContext Result.failure(Exception("Could not parse life plan response"))
            
            Result.success(proposal)
        } catch (e: Exception) {
            android.util.Log.e("LifePlannerRepository", "Error in requestLifePlan", e)
            Result.failure(e)
        }
    }
    
    /**
     * Parse JSON response into LifePlanProposal
     */
    private fun parseLifePlanResponse(responseText: String, startDate: LocalDate): LifePlanProposal? {
        return try {
            val jsonStart = responseText.indexOf("{")
            val jsonEnd = responseText.lastIndexOf("}") + 1
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) return null
            
            val jsonString = responseText.substring(jsonStart, jsonEnd)
            val json = JSONObject(jsonString)
            
            val planTitle = json.optString("planTitle", "Kế hoạch mới")
            val description = json.optString("description", "")
            val durationWeeks = json.optInt("durationWeeks", 4)
            val hoursPerWeek = json.optInt("hoursPerWeek", 10)
            
            val milestonesArray = json.optJSONArray("milestones") ?: JSONArray()
            val milestones = mutableListOf<Milestone>()
            var totalTasks = 0
            
            for (i in 0 until milestonesArray.length()) {
                val milestoneJson = milestonesArray.getJSONObject(i)
                val milestoneTitle = milestoneJson.optString("title", "Milestone ${i + 1}")
                val weekNumber = milestoneJson.optInt("week", i + 1)
                val targetDate = startDate.plusWeeks(weekNumber.toLong())
                
                val tasksArray = milestoneJson.optJSONArray("tasks") ?: JSONArray()
                val scheduledTasks = mutableListOf<ScheduledTask>()
                
                for (j in 0 until tasksArray.length()) {
                    val taskJson = tasksArray.getJSONObject(j)
                    val taskTitle = taskJson.optString("title", "Task")
                    val dayOfWeekStr = taskJson.optString("dayOfWeek", "monday").uppercase()
                    val time = taskJson.optString("time", "09:00")
                    val duration = taskJson.optInt("duration", 60)
                    val taskLabel = taskJson.optString("label", "star")
                    
                    val dayOfWeek = try {
                        DayOfWeek.valueOf(dayOfWeekStr)
                    } catch (e: Exception) {
                        DayOfWeek.MONDAY
                    }
                    
                    scheduledTasks.add(ScheduledTask(
                        title = taskTitle,
                        dayOfWeek = dayOfWeek,
                        preferredTime = time,
                        durationMinutes = duration,
                        label = taskLabel
                    ))
                    totalTasks++
                }
                
                milestones.add(Milestone(
                    title = milestoneTitle,
                    weekNumber = weekNumber,
                    targetDate = targetDate,
                    scheduledTasks = scheduledTasks,
                    status = MilestoneStatus.PENDING
                ))
            }
            
            val tipsArray = json.optJSONArray("tips") ?: JSONArray()
            val tips = (0 until tipsArray.length()).map { tipsArray.optString(it, "") }
            
            val warningsArray = json.optJSONArray("warnings") ?: JSONArray()
            val warnings = (0 until warningsArray.length()).map { warningsArray.optString(it, "") }
            
            val endDate = startDate.plusWeeks(durationWeeks.toLong())
            
            val lifePlan = LifePlan(
                title = planTitle,
                description = description,
                startDate = startDate,
                endDate = endDate,
                milestones = milestones,
                estimatedHoursPerWeek = hoursPerWeek,
                status = PlanStatus.DRAFT,
                tips = tips.filter { it.isNotBlank() },
                warnings = warnings.filter { it.isNotBlank() }
            )
            
            LifePlanProposal(
                plan = lifePlan,
                totalTasksToCreate = totalTasks * durationWeeks,
                rawResponse = responseText
            )
        } catch (e: Exception) {
            android.util.Log.e("LifePlannerRepository", "Error parsing life plan", e)
            null
        }
    }
    
    /**
     * Execute approved life plan - creates schedules in database
     */
    suspend fun executeLifePlan(plan: LifePlan): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var schedulesCreated = 0
            val today = LocalDate.now()
            
            for (milestone in plan.milestones) {
                var firstWeekDayOffset = 0
                
                for (task in milestone.scheduledTasks) {
                    val weeksUntilMilestone = milestone.weekNumber - 1
                    
                    for (weekOffset in 0..weeksUntilMilestone) {
                        var taskDate: LocalDate
                        
                        if (weekOffset == 0) {
                            taskDate = today.plusDays(firstWeekDayOffset.toLong())
                            firstWeekDayOffset = (firstWeekDayOffset + 1) % 7
                        } else {
                            val weekStartDate = today.plusWeeks(weekOffset.toLong())
                            taskDate = weekStartDate
                            
                            while (taskDate.dayOfWeek != java.time.DayOfWeek.MONDAY) {
                                taskDate = taskDate.minusDays(1)
                            }
                            
                            while (taskDate.dayOfWeek != task.dayOfWeek) {
                                taskDate = taskDate.plusDays(1)
                            }
                        }
                        
                        if (taskDate.isBefore(today)) continue
                        
                        val startTimeDate = "${taskDate}T${task.preferredTime}:00+07:00"
                        val hours = task.durationMinutes / 60
                        val minutes = task.durationMinutes % 60
                        val implementationTime = String.format("%02d:%02d:00", hours, minutes)
                        
                        val label = if (task.label.isNotBlank() && task.label != "star") 
                            task.label 
                        else 
                            AIHelpers.inferLabelFromTitle(task.title)
                        
                        val row = mapOf(
                            "user_id" to userId,
                            "name_schedule" to "${task.title} [${plan.title}]",
                            "start_time_date" to startTimeDate,
                            "implementation_time" to implementationTime,
                            "repeat" to "once",
                            "source" to "ai",
                            "label" to label,
                            "color" to AIHelpers.inferColorFromLabel(label)
                        )
                        
                        scheduleRepository.insertSchedule(row)
                        schedulesCreated++
                    }
                }
            }
            
            Result.success(schedulesCreated)
        } catch (e: Exception) {
            android.util.Log.e("LifePlannerRepository", "Error executing life plan", e)
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
                    android.util.Log.w("LifePlannerRepository", "Rate limit hit on attempt ${attempt + 1}, rotating key...")
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
