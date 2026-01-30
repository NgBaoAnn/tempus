package com.projectapp.tempus.data.sync

import com.projectapp.tempus.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


data class SyncState(
    val pendingChanges: Int = 0,
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,
    val error: String? = null,
    val progress: SyncProgress? = null
)


data class SyncProgress(
    val phase: String,           
    val current: Int,
    val total: Int
)


data class SyncResult(
    var schedulesCreated: Int = 0,
    var schedulesUpdated: Int = 0,
    var schedulesDeleted: Int = 0,
    var itemsCreated: Int = 0,
    var itemsUpdated: Int = 0,
    var subTasksCreated: Int = 0,
    var subTasksUpdated: Int = 0,
    var errors: MutableList<String> = mutableListOf()
) {
    val totalChanges: Int
        get() = schedulesCreated + schedulesUpdated + schedulesDeleted + 
                itemsCreated + itemsUpdated + subTasksCreated + subTasksUpdated
    
    val hasErrors: Boolean
        get() = errors.isNotEmpty()
    
    fun summary(): String {
        return buildString {
            if (schedulesCreated > 0) append("Đã tạo $schedulesCreated tasks. ")
            if (schedulesUpdated > 0) append("Đã cập nhật $schedulesUpdated tasks. ")
            if (schedulesDeleted > 0) append("Đã xóa $schedulesDeleted tasks. ")
            if (isEmpty()) append("Không có thay đổi.")
        }
    }
}
