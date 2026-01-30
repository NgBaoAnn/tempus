package com.projectapp.tempus.data.social.repository

import com.projectapp.tempus.data.social.dto.UserBasicDto
import com.projectapp.tempus.data.social.dto.UserProfile
import com.projectapp.tempus.domain.social.model.FriendRequest
import com.projectapp.tempus.domain.social.model.Friendship


interface FriendRepository {
    
    
    suspend fun sendFriendRequest(receiverId: String): Result<FriendRequest>
    
    
    suspend fun getPendingReceivedRequests(): Result<List<FriendRequest>>
    
    
    suspend fun getSentRequests(): Result<List<FriendRequest>>
    
    
    suspend fun acceptFriendRequest(requestId: String): Result<Unit>
    
    
    suspend fun rejectFriendRequest(requestId: String): Result<Unit>
    
    
    suspend fun cancelFriendRequest(requestId: String): Result<Unit>
    
    
    suspend fun getFriends(): Result<List<Friendship>>
    
    
    suspend fun unfriend(friendshipId: String): Result<Unit>
    
    
    suspend fun blockUser(userId: String): Result<Unit>
    
    
    suspend fun unblockUser(blockedUserId: String): Result<Unit>
    
    
    suspend fun getBlockedUsers(): Result<List<UserBasicDto>>
    
    
    suspend fun getAllBlockedUserIds(): Result<List<String>>
    
    
    suspend fun isUserBlocked(userId: String): Result<Boolean>
    
    
    suspend fun searchUsers(query: String): Result<List<UserBasicDto>>
    
    
    suspend fun getRelationshipStatus(userId: String): Result<RelationshipStatus>
    
    
    suspend fun getUserProfile(userId: String): Result<UserProfile>

    suspend fun getAllUsers(excludedIds: List<String> = emptyList()): Result<List<UserBasicDto>>
}


sealed class RelationshipStatus {
    data object None : RelationshipStatus()
    data object Friends : RelationshipStatus()
    data object RequestSent : RelationshipStatus()
    data object RequestReceived : RelationshipStatus()
    data object Blocked : RelationshipStatus()
    data object BlockedBy : RelationshipStatus()
}
