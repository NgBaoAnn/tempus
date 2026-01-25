package com.projectapp.tempus.data.voice

import com.projectapp.tempus.data.ai.AIRepository
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.data.voice.dto.ParsedTask
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration

/**
 * Service to parse Vietnamese natural language into task data using AI
 * 
 * Flow: Voice Text → AI API (Gemini) → Structured JSON → ParsedTask
 */
class TaskParserService(
    private val aiRepository: AIRepository
) {
    
    // System instruction for parsing voice commands
    private val parsingPrompt = """
Bạn là parser chuyển câu nói tiếng Việt thành task.
Trả về JSON với format CHÍNH XÁC sau (không thêm text):

{
  "taskName": "Tên công việc",
  "date": "YYYY-MM-DD",
  "time": "HH:MM",
  "duration": số_phút,
  "confidence": 0.0-1.0
}

Quy tắc:
- "mai" = ngày mai, "hôm nay" = ngày hôm nay
- "3 giờ chiều" = 15:00, "7 giờ tối" = 19:00
- "sáng" mặc định = 09:00, "chiều" = 14:00, "tối" = 19:00
- Nếu không nói rõ duration, mặc định = 60 phút
- confidence: 1.0 nếu đủ thông tin, thấp hơn nếu thiếu

VÍ DỤ:
Input: "Họp team lúc 3 giờ chiều mai"
Output: {"taskName": "Họp team", "date": "2026-01-26", "time": "15:00", "duration": 60, "confidence": 1.0}

Input: "Học tiếng Anh tối nay 2 tiếng"
Output: {"taskName": "Học tiếng Anh", "date": "2026-01-25", "time": "19:00", "duration": 120, "confidence": 1.0}
""".trimIndent()
    
    /**
     * Parse Vietnamese voice text into ParsedTask using AI
     */
    suspend fun parse(text: String, today: LocalDate = LocalDate.now()): ParsedTask {
        val prompt = """
$parsingPrompt

Ngày hôm nay: ${today}

Input: "$text"
Output:
""".trimIndent()
        
        val result = aiRepository.sendAskModeMessage(prompt)
        
        return result.fold(
            onSuccess = { responseText ->
                parseJsonResponse(text, responseText, today)
            },
            onFailure = {
                // Fallback: return raw text with no parsing
                ParsedTask(
                    rawText = text,
                    taskName = text,
                    date = null,
                    time = null,
                    duration = null,
                    confidence = 0f
                )
            }
        )
    }
    
    /**
     * Parse JSON response from AI into ParsedTask
     */
    private fun parseJsonResponse(rawText: String, jsonResponse: String, today: LocalDate): ParsedTask {
        return try {
            // Find JSON in response
            val jsonStart = jsonResponse.indexOf("{")
            val jsonEnd = jsonResponse.lastIndexOf("}") + 1
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                return ParsedTask(rawText, rawText, null, null, null, 0f)
            }
            
            val jsonString = jsonResponse.substring(jsonStart, jsonEnd)
            val json = org.json.JSONObject(jsonString)
            
            val taskName = json.optString("taskName", null)
            val dateStr = json.optString("date", null)
            val timeStr = json.optString("time", null)
            val durationMinutes = json.optInt("duration", 60)
            val confidence = json.optDouble("confidence", 0.8).toFloat()
            
            val date = try {
                if (dateStr != null) LocalDate.parse(dateStr) else null
            } catch (e: Exception) {
                null
            }
            
            val time = try {
                if (timeStr != null) LocalTime.parse(timeStr) else null
            } catch (e: Exception) {
                null
            }
            
            val duration = if (durationMinutes > 0) Duration.ofMinutes(durationMinutes.toLong()) else null
            
            ParsedTask(
                rawText = rawText,
                taskName = taskName,
                date = date,
                time = time,
                duration = duration,
                confidence = confidence
            )
        } catch (e: Exception) {
            ParsedTask(rawText, rawText, null, null, null, 0f)
        }
    }
}
