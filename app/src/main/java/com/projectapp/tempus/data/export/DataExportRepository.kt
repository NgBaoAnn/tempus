package com.projectapp.tempus.data.export

import android.content.Context
import android.os.Environment
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.schedule.dto.ScheduleRow
import com.projectapp.tempus.data.schedule.dto.ScheduleItemRow
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class ExportData(
    val exportedAt: String,
    val userId: String,
    val schedules: List<ScheduleRow>,
    val scheduleItems: List<ScheduleItemRow>
)

class DataExportRepository(private val context: Context) {
    
    private val supabase = SupabaseClientProvider.client
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    
    private fun getCurrentUserId(): String? {
        return supabase.auth.currentSessionOrNull()?.user?.id
    }
    
    suspend fun getAllUserData(): ExportData? {
        val userId = getCurrentUserId() ?: return null
        
        val schedules = supabase.from("schedule")
            .select { filter { eq("user_id", userId) } }
            .decodeList<ScheduleRow>()
        
        val taskIds = schedules.map { it.id }
        val scheduleItems = if (taskIds.isNotEmpty()) {
            supabase.from("schedule_items")
                .select { filter { isIn("task_id", taskIds) } }
                .decodeList<ScheduleItemRow>()
        } else emptyList()
        
        return ExportData(
            exportedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            userId = userId,
            schedules = schedules,
            scheduleItems = scheduleItems
        )
    }
    
    suspend fun exportToJson(): File? {
        val data = getAllUserData() ?: return null
        val jsonString = json.encodeToString(data)
        
        val fileName = "tempus_export_${System.currentTimeMillis()}.json"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        file.writeText(jsonString)
        
        return file
    }
    
    suspend fun exportToCsv(): File? {
        val data = getAllUserData() ?: return null
        
        val csvBuilder = StringBuilder()
        
        // Header
        csvBuilder.appendLine("ID,Name,Label,StartTime,Duration,Repeat,Color,Source,CreatedAt")
        
        // Data rows
        data.schedules.forEach { s ->
            csvBuilder.appendLine(
                "${s.id},\"${s.name}\",${s.label?.name ?: ""},${s.startTimeDate},${s.implementationTime},${s.repeat.name},${s.color ?: ""},${s.source?.name ?: ""},${s.createdAt ?: ""}"
            )
        }
        
        val fileName = "tempus_export_${System.currentTimeMillis()}.csv"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        file.writeText(csvBuilder.toString())
        
        return file
    }
    
    suspend fun deleteAllData(): Boolean {
        val userId = getCurrentUserId()
        if (userId == null) {
            android.util.Log.e("DataExport", "deleteAllData failed: No user logged in")
            return false
        }
        
        try {
            android.util.Log.d("DataExport", "Starting data deletion for user: $userId")
            
            // ========== 1. DELETE FROM SUPABASE (CLOUD) ==========
            
            // 1.1 Get all schedule IDs
            val schedules = supabase.from("schedule")
                .select { filter { eq("user_id", userId) } }
                .decodeList<ScheduleRow>()
            
            android.util.Log.d("DataExport", "Found ${schedules.size} schedules to delete from Supabase")
            
            val taskIds = schedules.map { it.id }
            
            // 1.2 Delete schedule_items first (foreign key)
            if (taskIds.isNotEmpty()) {
                android.util.Log.d("DataExport", "Deleting schedule_items for ${taskIds.size} tasks")
                supabase.from("schedule_items")
                    .delete { filter { isIn("task_id", taskIds) } }
            }
            
            // 1.3 Delete edited_version (if exists, may have foreign key to schedule)
            try {
                android.util.Log.d("DataExport", "Deleting edited_version for ${taskIds.size} tasks")
                if (taskIds.isNotEmpty()) {
                    supabase.from("edited_version")
                        .delete { filter { isIn("schedule_id", taskIds) } }
                }
            } catch (e: Exception) {
                android.util.Log.w("DataExport", "edited_version delete failed (table may not exist): ${e.message}")
            }
            
            // 1.4 Delete schedules from Supabase
            android.util.Log.d("DataExport", "Deleting schedules for user: $userId")
            supabase.from("schedule")
                .delete { filter { eq("user_id", userId) } }
            
            // ========== 2. DELETE FROM LOCAL ROOM DATABASE ==========
            android.util.Log.d("DataExport", "Clearing local Room database...")
            
            try {
                val db = com.projectapp.tempus.data.local.TempusDatabase.getDatabase(context)
                db.scheduleDao().clearAllLocalData()
                android.util.Log.d("DataExport", "Local Room database cleared successfully")
            } catch (e: Exception) {
                android.util.Log.e("DataExport", "Error clearing local Room DB: ${e.message}")
            }
            
            // ========== 3. SAVE DELETION LOG ==========
            saveDeletionLog("User requested data deletion")
            
            android.util.Log.d("DataExport", "Data deletion completed successfully (Cloud + Local)")
            return true
        } catch (e: Exception) {
            android.util.Log.e("DataExport", "Error deleting data", e)
            e.printStackTrace()
            return false
        }
    }
    
    fun saveDeletionLog(reason: String) {
        val logFile = File(context.filesDir, "deletion_log.txt")
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val userId = getCurrentUserId() ?: "unknown"
        
        val logEntry = "[$timestamp] User: $userId | Reason: $reason\n"
        logFile.appendText(logEntry)
    }
}
