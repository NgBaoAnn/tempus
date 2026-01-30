package com.projectapp.tempus.data.ai

import android.util.Log
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.ai.dto.AIHistoryInsert
import com.projectapp.tempus.data.ai.dto.AIHistoryRow
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Repository để quản lý AI Chat History với Supabase
 * Lưu trữ và truy xuất conversation history giữa user và AI
 */
class AIHistoryRepository {
    
    private val supabase = SupabaseClientProvider.client
    private val TABLE_NAME = "ai_history"
    
    private fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }
    
    /**
     * Lưu một cặp prompt/response vào database
     * @param prompt User's message
     * @param response AI's response
     * @param sessionId Session ID to group messages
     * @param title Optional session title (AI-generated)
     * @param mode Chat mode (ask/agent/life_planner)
     * @return true if saved successfully
     */
    suspend fun saveConversation(
        prompt: String,
        response: String,
        sessionId: String,
        title: String? = null,
        mode: String = "ask"
    ): Boolean {
        val userId = getCurrentUserId()
        
        if (userId == null) {
            Log.w("AIHistoryRepo", "Cannot save: User not authenticated")
            return false
        }
        
        return try {
            val meta = buildJsonObject {
                put("mode", mode)
            }
            
            val insertDto = AIHistoryInsert(
                userId = userId,
                sessionId = sessionId,
                title = title,
                prompt = prompt,
                response = response,
                meta = meta
            )
            
            Log.d("AIHistoryRepo", "Attempting to save conversation for user: $userId, session: $sessionId")
            supabase.from(TABLE_NAME).insert(insertDto)
            
            Log.d("AIHistoryRepo", "Saved conversation successfully: prompt=${prompt.take(50)}...")
            true
        } catch (e: Exception) {
            Log.e("AIHistoryRepo", "Failed to save conversation: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Lấy lịch sử chat của user, sắp xếp theo thời gian mới nhất trước
     * @param limit Số lượng records tối đa (default: 50)
     * @return List of AIHistoryRow, newest first
     */
    suspend fun getHistory(limit: Int = 50): List<AIHistoryRow> {
        val userId = getCurrentUserId()
        
        if (userId == null) {
            Log.w("AIHistoryRepo", "Cannot get history: User not authenticated")
            return emptyList()
        }
        
        return try {
            val result = supabase.from(TABLE_NAME)
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("created_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<AIHistoryRow>()
            
            Log.d("AIHistoryRepo", "Loaded ${result.size} history records")
            result
        } catch (e: Exception) {
            Log.e("AIHistoryRepo", "Failed to load history: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Lấy lịch sử theo thứ tự thời gian (cũ trước) để hiển thị đúng thứ tự chat
     * @param limit Số lượng records tối đa
     * @return List of AIHistoryRow, oldest first for display
     */
    suspend fun getHistoryForDisplay(limit: Int = 50): List<AIHistoryRow> {
        return getHistory(limit).reversed()
    }
    
    /**
     * Xóa toàn bộ lịch sử chat của user
     * @return true if deleted successfully
     */
    suspend fun clearHistory(): Boolean {
        val userId = getCurrentUserId()
        
        if (userId == null) {
            Log.w("AIHistoryRepo", "Cannot clear: User not authenticated")
            return false
        }
        
        return try {
            supabase.from(TABLE_NAME)
                .delete {
                    filter {
                        eq("user_id", userId)
                    }
                }
            
            Log.d("AIHistoryRepo", "Cleared all history for user")
            true
        } catch (e: Exception) {
            Log.e("AIHistoryRepo", "Failed to clear history: ${e.message}", e)
            false
        }
    }
    
    /**
     * Xóa một record history cụ thể
     * @param historyId ID của record cần xóa
     */
    suspend fun deleteHistory(historyId: String): Boolean {
        return try {
            supabase.from(TABLE_NAME)
                .delete {
                    filter {
                        eq("id", historyId)
                    }
                }
            
            Log.d("AIHistoryRepo", "Deleted history: $historyId")
            true
        } catch (e: Exception) {
            Log.e("AIHistoryRepo", "Failed to delete history: ${e.message}", e)
            false
        }
    }
    
    /**
     * Lấy danh sách các sessions (distinct by session_id)
     * Mỗi session có title và thời gian tạo
     * @return List of sessions with title and created_at
     */
    suspend fun getSessionList(): List<AIHistoryRow> {
        val userId = getCurrentUserId()
        
        if (userId == null) {
            Log.w("AIHistoryRepo", "Cannot get sessions: User not authenticated")
            return emptyList()
        }
        
        return try {
            // Lấy tất cả records và group theo session_id ở client side
            val result = supabase.from(TABLE_NAME)
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<AIHistoryRow>()
            
            // Group by session_id và lấy record đầu tiên (mới nhất) của mỗi session
            val sessions = result
                .filter { it.sessionId != null }
                .groupBy { it.sessionId }
                .map { (_, records) -> records.first() }
                .sortedByDescending { it.createdAt }
            
            Log.d("AIHistoryRepo", "Loaded ${sessions.size} sessions")
            sessions
        } catch (e: Exception) {
            Log.e("AIHistoryRepo", "Failed to load sessions: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Lấy tất cả messages của một session
     * @param sessionId ID của session
     * @return List of messages trong session, oldest first
     */
    suspend fun getSessionMessages(sessionId: String): List<AIHistoryRow> {
        val userId = getCurrentUserId()
        
        if (userId == null) {
            Log.w("AIHistoryRepo", "Cannot get session messages: User not authenticated")
            return emptyList()
        }
        
        return try {
            val result = supabase.from(TABLE_NAME)
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("session_id", sessionId)
                    }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<AIHistoryRow>()
            
            Log.d("AIHistoryRepo", "Loaded ${result.size} messages for session: $sessionId")
            result
        } catch (e: Exception) {
            Log.e("AIHistoryRepo", "Failed to load session messages: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Xóa toàn bộ session
     * @param sessionId ID của session cần xóa
     */
    suspend fun deleteSession(sessionId: String): Boolean {
        val userId = getCurrentUserId()
        
        if (userId == null) {
            Log.w("AIHistoryRepo", "Cannot delete session: User not authenticated")
            return false
        }
        
        return try {
            supabase.from(TABLE_NAME)
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("session_id", sessionId)
                    }
                }
            
            Log.d("AIHistoryRepo", "Deleted session: $sessionId")
            true
        } catch (e: Exception) {
            Log.e("AIHistoryRepo", "Failed to delete session: ${e.message}", e)
            false
        }
    }
    
    /**
     * Update title cho session
     * @param sessionId ID của session
     * @param title Title mới
     */
    suspend fun updateSessionTitle(sessionId: String, title: String): Boolean {
        val userId = getCurrentUserId()
        
        if (userId == null) {
            Log.w("AIHistoryRepo", "Cannot update title: User not authenticated")
            return false
        }
        
        return try {
            supabase.from(TABLE_NAME)
                .update({
                    set("title", title)
                }) {
                    filter {
                        eq("user_id", userId)
                        eq("session_id", sessionId)
                    }
                }
            
            Log.d("AIHistoryRepo", "Updated session title: $sessionId -> $title")
            true
        } catch (e: Exception) {
            Log.e("AIHistoryRepo", "Failed to update session title: ${e.message}", e)
            false
        }
    }
}
