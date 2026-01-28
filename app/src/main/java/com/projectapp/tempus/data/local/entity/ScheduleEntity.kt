package com.projectapp.tempus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Sync status for offline-first architecture
 */
enum class SyncStatus {
    SYNCED,           // Đã đồng bộ với server
    PENDING_CREATE,   // Tạo mới local, chưa push lên server
    PENDING_UPDATE,   // Sửa local, chưa push lên server  
    PENDING_DELETE    // Đánh dấu xóa, chưa xóa trên server
}

/**
 * Room Entity cho Schedule (task chính)
 * Tương ứng với bảng `schedule` trên Supabase
 */
@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey 
    val id: String,                       // UUID từ Supabase hoặc local-generated
    val userId: String,
    val name: String,
    
    // Optional fields
    val label: String? = null,            // wakeup, eat, exercise, rest, etc.
    val priority: String = "medium",      // low, medium, high
    val categoryId: String? = null,
    val description: String? = null,
    
    // Time fields
    val startTimeDate: String,            // ISO datetime (2025-12-21T07:00:00+07:00)
    val implementationTime: String,       // Duration: "HH:MM:SS"
    val repeat: String = "once",          // once, daily, weekly, monthly, custom
    val repeatDays: String? = null,       // Các thứ lặp: "1,3,5" = Mon, Wed, Fri
    val endDate: String? = null,          // ISO date: schedule không hiển thị sau ngày này
    
    // Display
    val color: String? = "#2196F3",
    val source: String = "manual",        // manual, ai
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    
    // ===== SYNC TRACKING =====
    val syncStatus: String = SyncStatus.SYNCED.name,
    val localUpdatedAt: Long = System.currentTimeMillis(),
    val serverUpdatedAt: Long? = null
) {
    companion object {
        /**
         * Convert từ ScheduleRow (Supabase DTO) sang Entity
         */
        fun fromRow(row: com.projectapp.tempus.data.schedule.dto.ScheduleRow): ScheduleEntity {
            return ScheduleEntity(
                id = row.id,
                userId = row.userId,
                name = row.name,
                label = row.label?.name,
                priority = row.priority?.name ?: "medium",
                categoryId = row.categoryId,
                description = row.description,
                startTimeDate = row.startTimeDate,
                implementationTime = row.implementationTime,
                repeat = row.repeat.name,
                repeatDays = row.repeatDays,
                endDate = row.endDate,
                color = row.color,
                source = row.source?.name ?: "manual",
                createdAt = row.createdAt?.let { parseIsoToMillis(it) } ?: System.currentTimeMillis(),
                syncStatus = SyncStatus.SYNCED.name,
                localUpdatedAt = System.currentTimeMillis(),
                serverUpdatedAt = row.createdAt?.let { parseIsoToMillis(it) }
            )
        }
        
        private fun parseIsoToMillis(iso: String): Long {
            return try {
                java.time.OffsetDateTime.parse(iso).toInstant().toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }
    }
}
