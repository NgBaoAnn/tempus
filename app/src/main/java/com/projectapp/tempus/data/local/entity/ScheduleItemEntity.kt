package com.projectapp.tempus.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


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
    val taskId: String,                   
    val date: String,                     
    val status: String = "planned",       
    val editedVersion: String? = null,    
    
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    
    val syncStatus: String = SyncStatus.SYNCED.name,
    val localUpdatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        
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
                    
                    java.time.LocalDate.parse(iso).atStartOfDay()
                        .toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                } catch (e2: Exception) {
                    System.currentTimeMillis()
                }
            }
        }
    }
}
