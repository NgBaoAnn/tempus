package com.projectapp.tempus.data.ai.context

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.projectapp.tempus.data.ai.dto.Content
import com.projectapp.tempus.data.ai.dto.Part
import com.projectapp.tempus.data.user.UserProfileCache
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.conversationDataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_conversations")

/**
 * Manages AI conversation context with:
 * - Sliding window for recent messages
 * - Memory summarization for older messages
 * - Token-aware pruning
 * - User profile context injection
 * - Session persistence
 */
class AIContextManager(
    private val appContext: Context,
    private val maxRecentMessages: Int = 10,
    private val maxTokens: Int = 8000,
    private val summarizeThreshold: Int = 8
) {
    companion object {
        private const val TAG = "AIContextManager"
        private val KEY_MEMORY_SUMMARY = stringPreferencesKey("memory_summary")
        private val KEY_RECENT_MESSAGES = stringPreferencesKey("recent_messages")
        private val KEY_SESSION_ID = stringPreferencesKey("current_session_id")
        
        // Token estimation: ~4 characters per token for English/Vietnamese
        private const val CHARS_PER_TOKEN = 4
    }
    
    // In-memory storage
    private val recentMessages = mutableListOf<Content>()
    private var memorySummary: String = ""
    private var currentSessionId: String = generateSessionId()
    
    /**
     * Add a message to conversation history
     */
    fun addMessage(content: Content) {
        recentMessages.add(content)
        Log.d(TAG, "Added message (${content.role}): ${content.parts.firstOrNull()?.text?.take(50)}...")
        
        // Check if we need to summarize old messages
        if (recentMessages.size > summarizeThreshold) {
            triggerSummarization()
        }
        
        // Prune if exceeding token limit
        pruneToTokenLimit()
    }
    
    /**
     * Get the full context for API request
     * Returns: [summary context] + [recent messages]
     */
    fun getContextForRequest(): List<Content> {
        val context = mutableListOf<Content>()
        
        // Add memory summary if exists
        if (memorySummary.isNotBlank()) {
            context.add(Content(
                role = "user",
                parts = listOf(Part(text = "[PREVIOUS CONVERSATION SUMMARY]\n$memorySummary\n[END SUMMARY]"))
            ))
        }
        
        // Add user profile context
        val profileContext = buildUserProfileContext()
        if (profileContext.isNotBlank()) {
            context.add(Content(
                role = "user", 
                parts = listOf(Part(text = profileContext))
            ))
        }
        
        // Add recent messages
        context.addAll(recentMessages)
        
        return context
    }
    
    /**
     * Get just recent messages (without summary/profile)
     * Used when conversationHistory is passed directly
     */
    fun getRecentMessages(): List<Content> = recentMessages.toList()
    
    /**
     * Build user profile context string
     */
    private fun buildUserProfileContext(): String {
        val profile = UserProfileCache.getProfile() ?: return ""
        val lang = UserProfileCache.getLanguage() ?: "vi"
        
        return if (lang == "en") {
            """[USER PROFILE]
            |Name: ${profile.username}
            |Language: $lang
            |[END PROFILE]""".trimMargin()
        } else {
            """[THÔNG TIN NGƯỜI DÙNG]
            |Tên: ${profile.username}
            |Ngôn ngữ: $lang
            |[END PROFILE]""".trimMargin()
        }
    }
    
    /**
     * Summarize oldest messages and move to memory
     * Called when message count exceeds threshold
     */
    private fun triggerSummarization() {
        if (recentMessages.size <= 4) return
        
        // Take oldest half of messages to summarize
        val toSummarize = recentMessages.take(recentMessages.size / 2)
        
        // Create simple summary (in production, could use AI to summarize)
        val summaryText = buildSimpleSummary(toSummarize)
        
        // Append to existing memory
        memorySummary = if (memorySummary.isBlank()) {
            summaryText
        } else {
            "$memorySummary\n---\n$summaryText"
        }
        
        // Remove summarized messages
        repeat(toSummarize.size) {
            recentMessages.removeAt(0)
        }
        
        Log.d(TAG, "Summarized ${toSummarize.size} messages. Remaining: ${recentMessages.size}")
    }
    
    /**
     * Build a simple text summary of messages
     */
    private fun buildSimpleSummary(messages: List<Content>): String {
        val userMessages = messages.filter { it.role == "user" }
            .mapNotNull { it.parts.firstOrNull()?.text }
            .take(3)
        val aiResponses = messages.filter { it.role == "model" }
            .mapNotNull { it.parts.firstOrNull()?.text?.take(100) }
            .take(2)
        
        return buildString {
            if (userMessages.isNotEmpty()) {
                append("User asked about: ${userMessages.joinToString("; ") { it.take(50) }}")
            }
            if (aiResponses.isNotEmpty()) {
                append("\nAI discussed: ${aiResponses.joinToString("; ")}")
            }
        }
    }
    
    /**
     * Prune messages to stay within token limit
     */
    private fun pruneToTokenLimit() {
        var totalTokens = estimateTokens(memorySummary)
        
        // Calculate tokens from recent messages
        val messageTokens = recentMessages.map { content ->
            estimateTokens(content.parts.firstOrNull()?.text ?: "")
        }
        
        totalTokens += messageTokens.sum()
        
        // Remove oldest messages if over limit
        while (totalTokens > maxTokens && recentMessages.size > 2) {
            val removed = recentMessages.removeAt(0)
            val removedTokens = estimateTokens(removed.parts.firstOrNull()?.text ?: "")
            totalTokens -= removedTokens
            Log.d(TAG, "Pruned message to stay within token limit. New total: $totalTokens")
        }
    }
    
    /**
     * Estimate token count from text
     */
    private fun estimateTokens(text: String): Int {
        return text.length / CHARS_PER_TOKEN
    }
    
    /**
     * Clear all conversation history
     */
    fun clearHistory() {
        recentMessages.clear()
        memorySummary = ""
        currentSessionId = generateSessionId()
        Log.d(TAG, "Cleared conversation history")
    }
    
    /**
     * Start a new session
     */
    fun startNewSession() {
        clearHistory()
        currentSessionId = generateSessionId()
    }
    
    /**
     * Get current session ID
     */
    fun getSessionId(): String = currentSessionId
    
    /**
     * Save current session to DataStore
     */
    suspend fun saveSession() {
        try {
            appContext.conversationDataStore.edit { prefs ->
                prefs[KEY_SESSION_ID] = currentSessionId
                prefs[KEY_MEMORY_SUMMARY] = memorySummary
                prefs[KEY_RECENT_MESSAGES] = messagesToJson(recentMessages)
            }
            Log.d(TAG, "Session saved: $currentSessionId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save session", e)
        }
    }
    
    /**
     * Restore session from DataStore
     */
    suspend fun restoreSession(): Boolean {
        return try {
            val prefs = appContext.conversationDataStore.data.first()
            
            val savedSessionId = prefs[KEY_SESSION_ID]
            val savedSummary = prefs[KEY_MEMORY_SUMMARY]
            val savedMessages = prefs[KEY_RECENT_MESSAGES]
            
            if (savedSessionId != null) {
                currentSessionId = savedSessionId
                memorySummary = savedSummary ?: ""
                
                if (savedMessages != null) {
                    recentMessages.clear()
                    recentMessages.addAll(jsonToMessages(savedMessages))
                }
                
                Log.d(TAG, "Session restored: $currentSessionId with ${recentMessages.size} messages")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore session", e)
            false
        }
    }
    
    /**
     * Convert messages to JSON for storage
     */
    private fun messagesToJson(messages: List<Content>): String {
        val array = JSONArray()
        messages.forEach { content ->
            val obj = JSONObject().apply {
                put("role", content.role)
                put("text", content.parts.firstOrNull()?.text ?: "")
            }
            array.put(obj)
        }
        return array.toString()
    }
    
    /**
     * Convert JSON back to messages
     */
    private fun jsonToMessages(json: String): List<Content> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                Content(
                    role = obj.getString("role"),
                    parts = listOf(Part(text = obj.getString("text")))
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse messages JSON", e)
            emptyList()
        }
    }
    
    /**
     * Generate unique session ID
     */
    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}"
    }
    
    /**
     * Get conversation statistics
     */
    fun getStats(): ContextStats {
        val totalTokens = estimateTokens(memorySummary) + 
            recentMessages.sumOf { estimateTokens(it.parts.firstOrNull()?.text ?: "") }
        
        return ContextStats(
            recentMessageCount = recentMessages.size,
            hasSummary = memorySummary.isNotBlank(),
            estimatedTokens = totalTokens,
            maxTokens = maxTokens,
            sessionId = currentSessionId
        )
    }
}

/**
 * Statistics about current context state
 */
data class ContextStats(
    val recentMessageCount: Int,
    val hasSummary: Boolean,
    val estimatedTokens: Int,
    val maxTokens: Int,
    val sessionId: String
)
