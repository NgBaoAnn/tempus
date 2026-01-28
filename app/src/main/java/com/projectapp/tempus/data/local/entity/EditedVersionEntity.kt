package com.projectapp.tempus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity cho Edited Version (phiên bản chỉnh sửa theo ngày)
 * Tương ứng với bảng `edited_version` trên Supabase
 * 
 * Khi user sửa 1 task cho 1 ngày cụ thể (không ảnh hưởng các ngày khác),
 * một EditedVersion được tạo ra để lưu thông tin override.
 */
@Entity(tableName = "edited_versions")
data class EditedVersionEntity(
    @PrimaryKey 
    val id: String,
    
    // Override fields (chỉ những field được sửa mới có giá trị)
    val name: String? = null,
    val iconId: String? = null,
    val label: String? = null,
    val startTimeDate: String? = null,    // ISO datetime
    val implementationTime: String? = null, // Duration
    val color: String? = null,
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    
    // ===== SYNC TRACKING =====
    val syncStatus: String = SyncStatus.SYNCED.name,
    val localUpdatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromRow(row: com.projectapp.tempus.data.schedule.dto.EditedVersionRow): EditedVersionEntity {
            return EditedVersionEntity(
                id = row.id,
                name = row.name,
                iconId = row.iconId,
                label = row.label?.name,
                startTimeDate = row.startTimeDate,
                implementationTime = row.implementationTime,
                color = row.color,
                createdAt = row.createdAt?.let { parseIsoToMillis(it) } ?: System.currentTimeMillis(),
                syncStatus = SyncStatus.SYNCED.name,
                localUpdatedAt = System.currentTimeMillis()
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
