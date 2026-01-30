package com.projectapp.tempus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


enum class SyncStatus {
    SYNCED,           
    PENDING_CREATE,   
    PENDING_UPDATE,   
    PENDING_DELETE    
}


@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey 
    val id: String,                       
    val userId: String,
    val name: String,
    
    
    val label: String? = null,            
    val priority: String = "medium",      
    val categoryId: String? = null,
    val description: String? = null,
    
    
    val startTimeDate: String,            
    val implementationTime: String,       
    val repeat: String = "once",          
    val repeatDays: String? = null,       
    val endDate: String? = null,          
    
    
    val color: String? = "#2196F3",
    val source: String = "manual",        
    
    
    val createdAt: Long = System.currentTimeMillis(),
    
    
    val syncStatus: String = SyncStatus.SYNCED.name,
    val localUpdatedAt: Long = System.currentTimeMillis(),
    val serverUpdatedAt: Long? = null
) {
    companion object {
        
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
