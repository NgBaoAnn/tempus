package com.projectapp.tempus.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.projectapp.tempus.data.ai.vector.VectorMemoryRepository
import java.util.concurrent.TimeUnit

/**
 * Background worker to sync tasks to vector memory periodically
 * Runs every hour when connected to network
 */
class VectorSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "VectorSyncWorker"
        private const val WORK_NAME = "vector_memory_sync"
        private const val KEY_USER_ID = "user_id"
        
        /**
         * Schedule periodic sync (every hour)
         */
        fun schedulePeriodicSync(context: Context, userId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val inputData = workDataOf(KEY_USER_ID to userId)
            
            val request = PeriodicWorkRequestBuilder<VectorSyncWorker>(
                1, TimeUnit.HOURS
            )
                .setInputData(inputData)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10, TimeUnit.MINUTES
                )
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            
            Log.d(TAG, "Scheduled periodic vector sync for user: $userId")
        }
        
        /**
         * Trigger immediate sync
         */
        fun syncNow(context: Context, userId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val inputData = workDataOf(KEY_USER_ID to userId)
            
            val request = OneTimeWorkRequestBuilder<VectorSyncWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context)
                .enqueue(request)
            
            Log.d(TAG, "Triggered immediate vector sync for user: $userId")
        }
        
        /**
         * Cancel all sync work
         */
        fun cancelSync(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled vector sync work")
        }
    }
    
    override suspend fun doWork(): Result {
        val userId = inputData.getString(KEY_USER_ID)
        
        if (userId.isNullOrBlank()) {
            Log.e(TAG, "No user ID provided")
            return Result.failure()
        }
        
        Log.d(TAG, "Starting vector sync for user: $userId")
        
        return try {
            val repo = VectorMemoryRepository(userId)
            
            // Check if backend is available
            if (!repo.isAvailable()) {
                Log.w(TAG, "Vector memory backend not available, will retry")
                return Result.retry()
            }
            
            // Sync tasks
            val result = repo.syncTasks()
            
            if (result.isSuccess) {
                Log.d(TAG, "Vector sync completed: ${result.getOrDefault(0)} tasks synced")
                Result.success()
            } else {
                Log.e(TAG, "Vector sync failed", result.exceptionOrNull())
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vector sync error", e)
            Result.retry()
        }
    }
}
