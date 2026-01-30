package com.projectapp.tempus.domain.usecase

import com.projectapp.tempus.domain.model.ScheduleSuggestion
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern


class ParseScheduleSuggestionUseCase {
    
    companion object {
        
        private val TIME_RANGE_PATTERN = Pattern.compile(
            """(\d{1,2})[h:.]?(\d{2})?\s*[-–~]\s*(\d{1,2})[h:.]?(\d{2})?\s*[:\-]?\s*(.+)""",
            Pattern.CASE_INSENSITIVE
        )
        
        
        private val BULLET_TIME_PATTERN = Pattern.compile(
            """[•\-*]\s*(\d{1,2})[h:.]?(\d{2})?\s*[:\-]?\s*(.+)""",
            Pattern.CASE_INSENSITIVE
        )
        
        
        private const val DEFAULT_DURATION_MINUTES = 60
    }
    
    
    fun parse(
        aiResponse: String, 
        targetDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    ): List<ScheduleSuggestion> {
        val suggestions = mutableListOf<ScheduleSuggestion>()
        
        
        val jsonSuggestions = tryParseJson(aiResponse, targetDate)
        if (jsonSuggestions.isNotEmpty()) {
            return jsonSuggestions
        }
        
        
        val lines = aiResponse.split("\n")
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue
            
            
            val rangeResult = parseTimeRange(trimmedLine, targetDate)
            if (rangeResult != null) {
                suggestions.add(rangeResult)
                continue
            }
            
            
            val bulletResult = parseBulletTime(trimmedLine, targetDate)
            if (bulletResult != null) {
                suggestions.add(bulletResult)
            }
        }
        
        return suggestions
    }
    
    
    private fun tryParseJson(response: String, targetDate: String): List<ScheduleSuggestion> {
        
        val jsonStart = response.indexOf("[")
        val jsonEnd = response.lastIndexOf("]")
        
        if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) {
            return emptyList()
        }
        
        try {
            val jsonString = response.substring(jsonStart, jsonEnd + 1)
            
            return parseSimpleJsonArray(jsonString, targetDate)
        } catch (e: Exception) {
            return emptyList()
        }
    }
    
    
    private fun parseSimpleJsonArray(json: String, targetDate: String): List<ScheduleSuggestion> {
        val suggestions = mutableListOf<ScheduleSuggestion>()
        
        
        val objectPattern = Pattern.compile("""\{[^}]+\}""")
        val matcher = objectPattern.matcher(json)
        
        while (matcher.find()) {
            val obj = matcher.group()
            val suggestion = parseJsonObject(obj, targetDate)
            if (suggestion != null) {
                suggestions.add(suggestion)
            }
        }
        
        return suggestions
    }
    
    
    private fun parseJsonObject(json: String, targetDate: String): ScheduleSuggestion? {
        val nameMatch = Regex(""""(?:name|task|title)"\s*:\s*"([^"]+)"""").find(json)
        val startMatch = Regex(""""(?:start|startTime|start_time|from)"\s*:\s*"([^"]+)"""").find(json)
        val endMatch = Regex(""""(?:end|endTime|end_time|to)"\s*:\s*"([^"]+)"""").find(json)
        val durationMatch = Regex(""""(?:duration|minutes)"\s*:\s*(\d+)""").find(json)
        
        val name = nameMatch?.groupValues?.get(1) ?: return null
        val startTime = startMatch?.groupValues?.get(1)?.let { normalizeTime(it) } ?: return null
        val endTime = endMatch?.groupValues?.get(1)?.let { normalizeTime(it) }
        val duration = durationMatch?.groupValues?.get(1)?.toIntOrNull() ?: calculateDuration(startTime, endTime)
        
        return ScheduleSuggestion(
            name = name.trim(),
            startTime = startTime,
            endTime = endTime,
            durationMinutes = duration,
            date = targetDate
        )
    }
    
    /**
     * Parse time range format: "8:00 - 9:00: Học Toán"
     */
    private fun parseTimeRange(line: String, targetDate: String): ScheduleSuggestion? {
        val matcher = TIME_RANGE_PATTERN.matcher(line)
        if (!matcher.find()) return null
        
        val startHour = matcher.group(1).toIntOrNull() ?: return null
        val startMinute = matcher.group(2)?.toIntOrNull() ?: 0
        val endHour = matcher.group(3).toIntOrNull() ?: return null
        val endMinute = matcher.group(4)?.toIntOrNull() ?: 0
        val taskName = matcher.group(5)?.trim() ?: return null
        
        if (taskName.isEmpty()) return null
        
        val startTime = String.format("%02d:%02d", startHour, startMinute)
        val endTime = String.format("%02d:%02d", endHour, endMinute)
        val duration = calculateDuration(startTime, endTime)
        
        return ScheduleSuggestion(
            name = cleanTaskName(taskName),
            startTime = startTime,
            endTime = endTime,
            durationMinutes = duration,
            date = targetDate
        )
    }
    
    /**
     * Parse bullet with time format: "• 8:00 Học Toán"
     */
    private fun parseBulletTime(line: String, targetDate: String): ScheduleSuggestion? {
        val matcher = BULLET_TIME_PATTERN.matcher(line)
        if (!matcher.find()) return null
        
        val hour = matcher.group(1).toIntOrNull() ?: return null
        val minute = matcher.group(2)?.toIntOrNull() ?: 0
        val taskName = matcher.group(3)?.trim() ?: return null
        
        if (taskName.isEmpty()) return null
        
        val startTime = String.format("%02d:%02d", hour, minute)
        
        return ScheduleSuggestion(
            name = cleanTaskName(taskName),
            startTime = startTime,
            durationMinutes = DEFAULT_DURATION_MINUTES,
            date = targetDate
        )
    }
    
    /**
     * Normalize time string to HH:MM format
     */
    private fun normalizeTime(time: String): String {
        val cleanTime = time.replace("h", ":").replace(".", ":")
        val parts = cleanTime.split(":")
        if (parts.isEmpty()) return time
        
        val hour = parts[0].toIntOrNull() ?: return time
        val minute = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
        
        return String.format("%02d:%02d", hour, minute)
    }
    
    /**
     * Calculate duration between two times in minutes
     */
    private fun calculateDuration(startTime: String, endTime: String?): Int {
        if (endTime == null) return DEFAULT_DURATION_MINUTES
        
        try {
            val startParts = startTime.split(":")
            val endParts = endTime.split(":")
            
            val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
            val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()
            
            val duration = endMinutes - startMinutes
            return if (duration > 0) duration else DEFAULT_DURATION_MINUTES
        } catch (e: Exception) {
            return DEFAULT_DURATION_MINUTES
        }
    }
    
    /**
     * Clean task name by removing common prefixes/suffixes
     */
    private fun cleanTaskName(name: String): String {
        return name
            .replace(Regex("""^[\-:\s]+"""), "")
            .replace(Regex("""[\-:\s]+$"""), "")
            .trim()
    }
}
