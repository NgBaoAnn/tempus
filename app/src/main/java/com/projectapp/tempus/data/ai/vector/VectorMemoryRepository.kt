package com.projectapp.tempus.data.ai.vector

import android.util.Log
import com.projectapp.tempus.core.vector.VectorMemoryProvider
import com.projectapp.tempus.data.schedule.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing vector memory operations
 * Handles communication with the Chroma backend for semantic context retrieval
 */
class VectorMemoryRepository(
    private val userId: String,
    private val scheduleRepository: ScheduleRepository? = null
) {
    private val api = VectorMemoryProvider.api
    private var currentSessionId: String? = null
    
    companion object {
        private const val TAG = "VectorMemoryRepo"
    }
    
    /**
     * Send message with vector context retrieval
     * The backend handles:
     * 1. Generating embeddings for the query
     * 2. Querying Chroma for relevant context
     * 3. Building the prompt with context
     * 4. Calling the LLM
     * 5. Storing the interaction
     */
    suspend fun sendMessage(
        query: String,
        mode: String = "ask"
    ): Result<ChatResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.chat(
                ChatRequest(
                    userId = userId,
                    query = query,
                    mode = mode,
                    sessionId = currentSessionId
                )
            )
            currentSessionId = response.sessionId
            Log.d(TAG, "Response received, context used: ${response.contextUsed.size} chunks")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sync all tasks to vector memory for semantic search
     * Call this periodically (e.g., hourly) to keep vector DB in sync
     */
    suspend fun syncTasks(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (scheduleRepository == null) {
                Log.w(TAG, "ScheduleRepository not provided, cannot sync tasks")
                return@withContext Result.failure(Exception("ScheduleRepository not available"))
            }
            
            val schedules = scheduleRepository.getAllSchedules(userId)
            
            val taskDtos = schedules.map { schedule ->
                TaskDto(
                    id = schedule.id,
                    title = schedule.name,
                    description = schedule.description,
                    status = "active", // ScheduleRow doesn't have completion status
                    deadline = schedule.endDate,
                    priority = schedule.priority?.name?.lowercase() ?: "medium",
                    labels = listOfNotNull(schedule.label?.name)
                )
            }
            
            if (taskDtos.isEmpty()) {
                Log.d(TAG, "No tasks to sync")
                return@withContext Result.success(0)
            }
            
            val response = api.syncTasks(
                TaskSyncRequest(userId, taskDtos)
            )
            
            Log.d(TAG, "Synced ${response.synced} tasks to vector memory")
            Result.success(response.synced)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing tasks", e)
            Result.failure(e)
        }
    }
    
    /**
     * Add a user preference or memory to the vector store
     * Examples:
     * - "User prefers morning meetings"
     * - "User works best between 9am-12pm"
     * - "User has high priority on work tasks"
     */
    suspend fun addMemory(
        text: String, 
        category: String = "general"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.addMemory(userId, text, category)
            if (response.isSuccessful) {
                Log.d(TAG, "Memory added: $text")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to add memory: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding memory", e)
            Result.failure(e)
        }
    }
    
    /**
     * Clear all vector memory for the user
     * Use with caution - this deletes all stored context
     */
    suspend fun clearMemory(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.clearMemory(userId)
            if (response.isSuccessful) {
                currentSessionId = null
                Log.d(TAG, "Memory cleared for user: $userId")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to clear memory: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing memory", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get memory statistics for the user
     * Useful for debugging and monitoring
     */
    suspend fun getStats(): Result<MemoryStats> = withContext(Dispatchers.IO) {
        try {
            val stats = api.getStats(userId)
            Log.d(TAG, "Stats: tasks=${stats.userTasks}, memories=${stats.userMemories}, interactions=${stats.userInteractions}")
            Result.success(stats)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting stats", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if the vector memory backend is available
     */
    suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.healthCheck()
            response.isSuccessful
        } catch (e: Exception) {
            Log.w(TAG, "Vector memory backend not available", e)
            false
        }
    }
    
    /**
     * Start a new conversation session
     * This clears the session ID so the next request starts fresh
     */
    fun startNewSession() {
        currentSessionId = null
        Log.d(TAG, "Started new session")
    }
    
    /**
     * Get current session ID
     */
    fun getSessionId(): String? = currentSessionId
}
