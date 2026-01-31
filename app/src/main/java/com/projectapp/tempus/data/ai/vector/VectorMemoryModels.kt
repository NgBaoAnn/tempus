package com.projectapp.tempus.data.ai.vector

import com.google.gson.annotations.SerializedName

/**
 * Request to send a chat message with vector context retrieval
 */
data class ChatRequest(
    @SerializedName("user_id") val userId: String,
    val query: String,
    val mode: String = "ask",
    @SerializedName("session_id") val sessionId: String? = null
)

/**
 * Response from chat endpoint with AI response and context used
 */
data class ChatResponse(
    val response: String,
    @SerializedName("context_used") val contextUsed: List<String>,
    @SerializedName("session_id") val sessionId: String
)

/**
 * Request to sync tasks to vector memory
 */
data class TaskSyncRequest(
    @SerializedName("user_id") val userId: String,
    val tasks: List<TaskDto>
)

/**
 * Task data for vector memory storage
 */
data class TaskDto(
    val id: String,
    val title: String,
    val description: String?,
    val status: String,
    val deadline: String?,
    val priority: String,
    val labels: List<String>
)

/**
 * Response from sync endpoint
 */
data class SyncResponse(
    val synced: Int
)

/**
 * Memory statistics for a user
 */
data class MemoryStats(
    val tasks: Int,
    val memories: Int,
    val interactions: Int,
    @SerializedName("user_tasks") val userTasks: Int,
    @SerializedName("user_memories") val userMemories: Int,
    @SerializedName("user_interactions") val userInteractions: Int
)

/**
 * Generic status response
 */
data class StatusResponse(
    val status: String
)
