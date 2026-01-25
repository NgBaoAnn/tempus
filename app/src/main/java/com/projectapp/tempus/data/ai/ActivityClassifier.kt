package com.projectapp.tempus.data.ai

import com.projectapp.tempus.data.schedule.dto.ScheduleLabel

/**
 * Utility để tự động phân loại hoạt động và gán icon/màu sắc phù hợp
 * Sử dụng keyword matching để xác định loại hoạt động
 */
object ActivityClassifier {
    
    /**
     * Danh sách màu sắc có sẵn (từ EditScheduleScreen)
     */
    private val availableColors = listOf(
        "#FF3B30",  // Red - Alert/Important
        "#FF9500",  // Orange - Exercise/Active
        "#FFCC00",  // Yellow - Social/Fun
        "#34C759",  // Green - Health/Nature
        "#00C7BE",  // Teal - Water/Fresh
        "#007AFF",  // Blue - Work/Study
        "#5856D6",  // Purple - Creative
        "#AF52DE",  // Magenta - Rest/Relax
        "#FF2D55",  // Pink - Personal
        "#A2845E"   // Brown - Home/Garden
    )
    
    /**
     * Map từ ScheduleLabel sang màu sắc phù hợp
     */
    private val labelToColor = mapOf(
        ScheduleLabel.wakeup to "#FF9500",   // Orange - energetic morning
        ScheduleLabel.eat to "#FFCC00",      // Yellow - meal time
        ScheduleLabel.exercise to "#FF3B30", // Red - active/exercise
        ScheduleLabel.rest to "#AF52DE",     // Magenta - relaxation
        ScheduleLabel.water to "#00C7BE",    // Teal - hydration
        ScheduleLabel.book to "#007AFF",     // Blue - study/learning
        ScheduleLabel.sleep to "#5856D6",    // Purple - night/sleep
        ScheduleLabel.clean to "#34C759",    // Green - clean environment
        ScheduleLabel.cook to "#FF2D55",     // Pink - cooking
        ScheduleLabel.garden to "#A2845E",   // Brown - gardening
        ScheduleLabel.UNKNOWN to "#007AFF"   // Default blue
    )
    
    /**
     * Keywords để classify activity vào từng category
     * Lowercase để dễ matching
     */
    private val labelKeywords = mapOf(
        ScheduleLabel.wakeup to listOf(
            "thức dậy", "wake up", "dạy sớm", "buổi sáng", "morning", 
            "báo thức", "alarm", "đánh răng", "rửa mặt"
        ),
        ScheduleLabel.eat to listOf(
            "ăn", "eat", "breakfast", "lunch", "dinner", "bữa sáng", 
            "bữa trưa", "bữa tối", "meal", "food", "ăn sáng", "ăn trưa", 
            "ăn tối", "snack", "ăn vặt"
        ),
        ScheduleLabel.exercise to listOf(
            "tập", "exercise", "gym", "workout", "thể dục", "thể thao",
            "chạy bộ", "running", "yoga", "cardio", "tennis", "bóng đá",
            "football", "basketball", "bơi", "swimming", "đạp xe", "cycling",
            "fitness", "training", "aerobic", "plank", "squat", "push up"
        ),
        ScheduleLabel.rest to listOf(
            "nghỉ", "rest", "break", "relax", "thư giãn", "giải lao",
            "meditation", "thiền", "nap", "ngủ trưa", "nghỉ ngơi", "chill"
        ),
        ScheduleLabel.water to listOf(
            "uống nước", "water", "hydrate", "drink water", "nước", 
            "trà", "tea", "cà phê", "coffee"
        ),
        ScheduleLabel.book to listOf(
            "học", "study", "read", "đọc", "book", "sách", "bài", "lesson",
            "course", "khóa học", "lớp học", "class", "research", "nghiên cứu",
            "ôn thi", "exam", "homework", "bài tập", "assignment", "ielts",
            "toeic", "english", "coding", "lập trình", "programming", "work",
            "làm việc", "meeting", "họp", "project", "dự án"
        ),
        ScheduleLabel.sleep to listOf(
            "ngủ", "sleep", "bed", "đi ngủ", "bedtime", "night", "đêm",
            "gác chân", "nghỉ tối"
        ),
        ScheduleLabel.clean to listOf(
            "dọn", "clean", "cleaning", "lau", "quét", "giặt", "laundry",
            "tidy", "sắp xếp", "organize", "dọn dẹp", "vệ sinh", "rửa bát"
        ),
        ScheduleLabel.cook to listOf(
            "nấu", "cook", "cooking", "bếp", "kitchen", "recipe", "đồ ăn",
            "chuẩn bị bữa", "meal prep"
        ),
        ScheduleLabel.garden to listOf(
            "vườn", "garden", "gardening", "cây", "plant", "plants", "tưới",
            "watering", "hoa", "flower", "trồng"
        )
    )
    
    /**
     * Classify activity name thành ScheduleLabel phù hợp
     * @param activityName Tên hoạt động
     * @return ScheduleLabel phù hợp nhất hoặc book (default cho học tập/làm việc)
     */
    fun classifyActivity(activityName: String): ScheduleLabel {
        val lowerName = activityName.lowercase()
        
        // Tìm label có keyword match nhiều nhất
        var bestLabel = ScheduleLabel.book  // Default: học tập/làm việc
        var bestScore = 0
        
        for ((label, keywords) in labelKeywords) {
            var score = 0
            for (keyword in keywords) {
                if (lowerName.contains(keyword)) {
                    score += keyword.length  // Longer match = higher score
                }
            }
            if (score > bestScore) {
                bestScore = score
                bestLabel = label
            }
        }
        
        return bestLabel
    }
    
    /**
     * Lấy màu sắc phù hợp cho ScheduleLabel
     * @param label ScheduleLabel
     * @return Hex color string
     */
    fun getColorForLabel(label: ScheduleLabel): String {
        return labelToColor[label] ?: "#007AFF"  // Default blue
    }
    
    /**
     * Classify activity và trả về cả label lẫn color
     * @param activityName Tên hoạt động
     * @return Pair(ScheduleLabel, ColorHex)
     */
    fun classifyWithColor(activityName: String): Pair<ScheduleLabel, String> {
        val label = classifyActivity(activityName)
        val color = getColorForLabel(label)
        return Pair(label, color)
    }
    
    /**
     * Lấy random color cho trường hợp không xác định được label
     */
    fun getRandomColor(): String {
        return availableColors.random()
    }
}
