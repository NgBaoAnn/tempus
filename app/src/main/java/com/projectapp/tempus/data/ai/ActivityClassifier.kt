package com.projectapp.tempus.data.ai

import com.projectapp.tempus.data.schedule.dto.ScheduleLabel


object ActivityClassifier {
    
    
    private val availableColors = listOf(
        "#FF3B30",  
        "#FF9500",  
        "#FFCC00",  
        "#34C759",  
        "#00C7BE",  
        "#007AFF",  
        "#5856D6",  
        "#AF52DE",  
        "#FF2D55",  
        "#A2845E"   
    )
    
    
    private val labelToColor = mapOf(
        ScheduleLabel.wakeup to "#FF9500",   
        ScheduleLabel.eat to "#FFCC00",      
        ScheduleLabel.exercise to "#FF3B30", 
        ScheduleLabel.rest to "#AF52DE",     
        ScheduleLabel.water to "#00C7BE",    
        ScheduleLabel.book to "#007AFF",     
        ScheduleLabel.sleep to "#5856D6",    
        ScheduleLabel.clean to "#34C759",    
        ScheduleLabel.cook to "#FF2D55",     
        ScheduleLabel.garden to "#A2845E",   
        ScheduleLabel.UNKNOWN to "#007AFF"   
    )
    
    
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
    
    
    fun classifyActivity(activityName: String): ScheduleLabel {
        val lowerName = activityName.lowercase()
        
        
        var bestLabel = ScheduleLabel.book  
        var bestScore = 0
        
        for ((label, keywords) in labelKeywords) {
            var score = 0
            for (keyword in keywords) {
                if (lowerName.contains(keyword)) {
                    score += keyword.length  
                }
            }
            if (score > bestScore) {
                bestScore = score
                bestLabel = label
            }
        }
        
        return bestLabel
    }
    
    
    fun getColorForLabel(label: ScheduleLabel): String {
        return labelToColor[label] ?: "#007AFF"  
    }
    
    
    fun classifyWithColor(activityName: String): Pair<ScheduleLabel, String> {
        val label = classifyActivity(activityName)
        val color = getColorForLabel(label)
        return Pair(label, color)
    }
    
    
    fun getRandomColor(): String {
        return availableColors.random()
    }
}
