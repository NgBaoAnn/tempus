package com.projectapp.tempus.data.ai.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject


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
    
    
    val meta: JsonObject? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null
)


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
