package com.projectapp.tempus.domain.model

/**
 * Các hành động ảnh hưởng đến điểm số
 */
enum class PointAction(
    val points: Int,
    val description: String
) {
    // Kiếm điểm
    TASK_COMPLETE(10, "Hoàn thành task"),
    POMODORO_COMPLETE(5, "Hoàn thành Pomodoro"),
    STREAK_BONUS_3(15, "Streak 3 ngày liên tiếp"),
    STREAK_BONUS_7(30, "Streak 1 tuần liên tiếp"),
    STREAK_BONUS_30(100, "Streak 1 tháng liên tiếp"),
    
    // Mất điểm
    TASK_UNCOMPLETE(-10, "Huỷ hoàn thành task"),
    MISS_TASK(-5, "Bỏ lỡ task"),
    PLANT_TREE(-50, "Trồng cây mới (cơ bản)"),
    WATER_TREE(-10, "Tưới cây");
    
    fun isEarning(): Boolean = points > 0
    fun isSpending(): Boolean = points < 0
}
