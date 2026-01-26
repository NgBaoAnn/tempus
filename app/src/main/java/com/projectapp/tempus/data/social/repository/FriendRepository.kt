package com.projectapp.tempus.data.social.repository

import com.projectapp.tempus.data.social.dto.UserBasicDto
import com.projectapp.tempus.data.social.dto.UserProfile
import com.projectapp.tempus.domain.social.model.FriendRequest
import com.projectapp.tempus.domain.social.model.Friendship

/**
 * Repository interface cho Friend System
 */
interface FriendRepository {
    
    // =============== FRIEND REQUESTS ===============
    
    /**
     * Gửi lời mời kết bạn
     */
    suspend fun sendFriendRequest(receiverId: String): Result<FriendRequest>
    
    /**
     * Lấy danh sách lời mời đã nhận (pending)
     */
    suspend fun getPendingReceivedRequests(): Result<List<FriendRequest>>
    
    /**
     * Lấy danh sách lời mời đã gửi
     */
    suspend fun getSentRequests(): Result<List<FriendRequest>>
    
    /**
     * Chấp nhận lời mời kết bạn
     */
    suspend fun acceptFriendRequest(requestId: String): Result<Unit>
    
    /**
     * Từ chối lời mời kết bạn
     */
    suspend fun rejectFriendRequest(requestId: String): Result<Unit>
    
    /**
     * Huỷ lời mời đã gửi
     */
    suspend fun cancelFriendRequest(requestId: String): Result<Unit>
    
    // =============== FRIENDSHIPS ===============
    
    /**
     * Lấy danh sách bạn bè
     */
    suspend fun getFriends(): Result<List<Friendship>>
    
    /**
     * Huỷ kết bạn
     */
    suspend fun unfriend(friendshipId: String): Result<Unit>
    
    // =============== BLOCKED USERS ===============
    
    /**
     * Chặn user
     */
    suspend fun blockUser(userId: String): Result<Unit>
    
    /**
     * Bỏ chặn user
     */
    suspend fun unblockUser(blockedUserId: String): Result<Unit>
    
    /**
     * Lấy danh sách user đã chặn
     */
    suspend fun getBlockedUsers(): Result<List<UserBasicDto>>
    
    // =============== USER SEARCH ===============
    
    /**
     * Tìm kiếm user theo username
     */
    suspend fun searchUsers(query: String): Result<List<UserBasicDto>>
    
    /**
     * Kiểm tra trạng thái quan hệ với một user
     */
    suspend fun getRelationshipStatus(userId: String): Result<RelationshipStatus>
    
    /**
     * Lấy danh sách tất cả users (để hiển thị trong Discover tab)
     * Exclude current user và blocked users
     */
    /**
     * Lấy thông tin chi tiết user profile
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile>

    suspend fun getAllUsers(excludedIds: List<String> = emptyList()): Result<List<UserBasicDto>>
}

/**
 * Trạng thái quan hệ với một user
 */
sealed class RelationshipStatus {
    data object None : RelationshipStatus()
    data object Friends : RelationshipStatus()
    data object RequestSent : RelationshipStatus()
    data object RequestReceived : RelationshipStatus()
    data object Blocked : RelationshipStatus()
    data object BlockedBy : RelationshipStatus()
}
