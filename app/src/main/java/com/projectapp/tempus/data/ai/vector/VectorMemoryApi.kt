package com.projectapp.tempus.data.ai.vector

import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for Vector Memory backend
 * Communicates with Python FastAPI server hosting Chroma
 */
interface VectorMemoryApi {
    
    /**
     * Main chat endpoint - sends query and receives AI response with vector context
     */
    @POST("ai/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
    
    /**
     * Sync tasks to vector memory for semantic search
     */
    @POST("memory/sync/tasks")
    suspend fun syncTasks(@Body request: TaskSyncRequest): SyncResponse
    
    /**
     * Add a user memory/preference
     */
    @POST("memory/add")
    suspend fun addMemory(
        @Query("user_id") userId: String,
        @Query("text") text: String,
        @Query("category") category: String = "general"
    ): Response<StatusResponse>
    
    /**
     * Clear all memory for a user
     */
    @DELETE("memory/clear/{user_id}")
    suspend fun clearMemory(@Path("user_id") userId: String): Response<StatusResponse>
    
    /**
     * Get memory statistics for a user
     */
    @GET("memory/stats/{user_id}")
    suspend fun getStats(@Path("user_id") userId: String): MemoryStats
    
    /**
     * Health check endpoint
     */
    @GET("health")
    suspend fun healthCheck(): Response<StatusResponse>
}
