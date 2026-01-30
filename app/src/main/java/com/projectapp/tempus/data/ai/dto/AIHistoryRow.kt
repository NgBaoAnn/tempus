package com.projectapp.tempus.data.ai.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Data class mapping to ai_history table in Supabase
 * Stores chat conversation history between user and AI
 */
@Serializable
data class AIHistoryRow(
    val id: String? = null,
    
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("session_id")
    val sessionId: String? = null,
    
    val title: String? = null,
    
    val prompt: String? = null,
    
    val response: String? = null,
    
    /**
     * Metadata for additional context:
     * - mode: "ask" | "agent" | "life_planner"
     * - model: "gemini-pro" etc
     */
    val meta: JsonObject? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null
)

/**
 * Insert body for creating new AI history record
 * Excludes id and created_at as they are auto-generated
 */
@Serializable
data class AIHistoryInsert(
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("session_id")
    val sessionId: String,
    
    val title: String? = null,
    
    val prompt: String,
    
    val response: String,
    
    val meta: JsonObject? = null
)
