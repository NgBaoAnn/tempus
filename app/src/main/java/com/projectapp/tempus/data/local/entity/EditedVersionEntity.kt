package com.projectapp.tempus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "edited_versions")
data class EditedVersionEntity(
    @PrimaryKey 
    val id: String,
    
    
    val name: String? = null,
    val iconId: String? = null,
    val label: String? = null,
    val startTimeDate: String? = null,    
    val implementationTime: String? = null, 
    val color: String? = null,
    
    
    val createdAt: Long = System.currentTimeMillis(),
    
    
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
