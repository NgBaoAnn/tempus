package com.projectapp.tempus.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity cho Schedule Item (trạng thái theo ngày)
 * Tương ứng với bảng `schedule_items` trên Supabase
 * 
 * Mỗi schedule có thể có nhiều items, mỗi item đại diện cho 1 ngày cụ thể
 */
@Entity(
    tableName = "schedule_items",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["taskId"]),
        Index(value = ["date"]),
        Index(value = ["taskId", "date"], unique = true)
    ]
)
data class ScheduleItemEntity(
    @PrimaryKey 
    val id: String,
    val taskId: String,                   // FK to schedules.id
    val date: String,                     // "YYYY-MM-DD"
    val status: String = "planned",       // planned, done, delete
    val editedVersion: String? = null,    // FK to edited_version (nếu có override)
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // ===== SYNC TRACKING =====
    val syncStatus: String = SyncStatus.SYNCED.name,
    val localUpdatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Convert từ ScheduleItemRow (Supabase DTO) sang Entity
         */
        fun fromRow(row: com.projectapp.tempus.data.schedule.dto.ScheduleItemRow): ScheduleItemEntity {
            return ScheduleItemEntity(
                id = row.id,
                taskId = row.taskId,
                date = row.date,
                status = row.status?.name ?: "planned",
                editedVersion = row.editedVersion,
                createdAt = row.createdAt?.let { parseIsoToMillis(it) } ?: System.currentTimeMillis(),
                updatedAt = row.updatedAt?.let { parseIsoToMillis(it) } ?: System.currentTimeMillis(),
                syncStatus = SyncStatus.SYNCED.name,
                localUpdatedAt = System.currentTimeMillis()
            )
        }
        
        private fun parseIsoToMillis(iso: String): Long {
            return try {
                java.time.OffsetDateTime.parse(iso).toInstant().toEpochMilli()
            } catch (e: Exception) {
                try {
                    // Try parsing as date without time
                    java.time.LocalDate.parse(iso).atStartOfDay()
                        .toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                } catch (e2: Exception) {
                    System.currentTimeMillis()
                }
            }
        }
    }
}
