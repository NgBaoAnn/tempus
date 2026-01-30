package com.projectapp.tempus.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "sub_tasks",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["scheduleId"])
    ]
)
data class SubTaskEntity(
    @PrimaryKey 
    val id: String,
    val scheduleId: String,               
    val title: String,
    val isDone: Boolean = false,
    val orderNo: Int = 0,
    
    
    val createdAt: Long = System.currentTimeMillis(),
    
    
    val syncStatus: String = SyncStatus.SYNCED.name,
    val localUpdatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        
        fun fromRow(row: com.projectapp.tempus.data.schedule.dto.SubTaskRow): SubTaskEntity {
            return SubTaskEntity(
                id = row.id ?: java.util.UUID.randomUUID().toString(),
                scheduleId = row.scheduleId,
                title = row.title,
                isDone = row.isDone,
                orderNo = row.orderNo,
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
