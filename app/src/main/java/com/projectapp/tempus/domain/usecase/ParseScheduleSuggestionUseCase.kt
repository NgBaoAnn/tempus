package com.projectapp.tempus.domain.usecase

import com.projectapp.tempus.domain.model.ScheduleSuggestion
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

/**
 * Use case to parse AI text response into structured schedule suggestions
 * 
 * Supports multiple formats:
 * - "8:00 - 9:00: Học Toán"
 * - "08h00 - 09h00 Học Lý"
 * - JSON format from structured prompt
 */
class ParseScheduleSuggestionUseCase {
    
    companion object {
        // Pattern: "HH:MM - HH:MM: Task name" or "HH:MM-HH:MM Task name"
        private val TIME_RANGE_PATTERN = Pattern.compile(
            """(\d{1,2})[h:.]?(\d{2})?\s*[-–~]\s*(\d{1,2})[h:.]?(\d{2})?\s*[:\-]?\s*(.+)""",
            Pattern.CASE_INSENSITIVE
        )
        
        // Pattern: "• HH:MM: Task name" or "- HH:MM Task name"
        private val BULLET_TIME_PATTERN = Pattern.compile(
            """[•\-*]\s*(\d{1,2})[h:.]?(\d{2})?\s*[:\-]?\s*(.+)""",
            Pattern.CASE_INSENSITIVE
        )
        
        // Default duration if only start time is provided
        private const val DEFAULT_DURATION_MINUTES = 60
    }
    
    /**
     * Parse AI response text into list of schedule suggestions
     * 
     * @param aiResponse The raw text response from AI
     * @param targetDate The date for which suggestions are being created (default: today)
     * @return List of parsed ScheduleSuggestion objects
     */
    fun parse(
        aiResponse: String, 
        targetDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    ): List<ScheduleSuggestion> {
        val suggestions = mutableListOf<ScheduleSuggestion>()
        
        // Try parsing JSON first
        val jsonSuggestions = tryParseJson(aiResponse, targetDate)
        if (jsonSuggestions.isNotEmpty()) {
            return jsonSuggestions
        }
        
        // Parse line by line
        val lines = aiResponse.split("\n")
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue
            
            // Try time range pattern first (e.g., "8:00 - 9:00: Học Toán")
            val rangeResult = parseTimeRange(trimmedLine, targetDate)
            if (rangeResult != null) {
                suggestions.add(rangeResult)
                continue
            }
            
            // Try bullet with time pattern (e.g., "• 8:00 Học Toán")
            val bulletResult = parseBulletTime(trimmedLine, targetDate)
            if (bulletResult != null) {
                suggestions.add(bulletResult)
            }
        }
        
        return suggestions
    }
    
    /**
     * Try to parse JSON formatted response
     */
    private fun tryParseJson(response: String, targetDate: String): List<ScheduleSuggestion> {
        // Look for JSON array pattern
        val jsonStart = response.indexOf("[")
        val jsonEnd = response.lastIndexOf("]")
        
        if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) {
            return emptyList()
        }
        
        try {
            val jsonString = response.substring(jsonStart, jsonEnd + 1)
            // Simple JSON parsing without external library
            return parseSimpleJsonArray(jsonString, targetDate)
        } catch (e: Exception) {
            return emptyList()
        }
    }
    
    /**
     * Simple JSON array parser for schedule objects
     * Expected format: [{"name": "...", "start": "HH:MM", "end": "HH:MM"}, ...]
     */
    private fun parseSimpleJsonArray(json: String, targetDate: String): List<ScheduleSuggestion> {
        val suggestions = mutableListOf<ScheduleSuggestion>()
        
        // Find all objects in array
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
    
    /**
     * Parse a single JSON object into ScheduleSuggestion
     */
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
