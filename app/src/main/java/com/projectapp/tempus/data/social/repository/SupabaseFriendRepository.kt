package com.projectapp.tempus.data.social.repository

import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.social.dto.*
import com.projectapp.tempus.domain.social.model.FriendRequest
import com.projectapp.tempus.domain.social.model.FriendRequestStatus
import com.projectapp.tempus.domain.social.model.Friendship
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.time.Instant

/**
 * Implementation của FriendRepository sử dụng Supabase
 */
class SupabaseFriendRepository(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : FriendRepository {

    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")
    }

    // =============== FRIEND REQUESTS ===============

    override suspend fun sendFriendRequest(receiverId: String): Result<FriendRequest> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            // Kiểm tra không gửi cho chính mình
            if (currentUserId == receiverId) {
                throw IllegalArgumentException("Cannot send friend request to yourself")
            }
            
            val dto = CreateFriendRequestDto(
                senderId = currentUserId,
                receiverId = receiverId
            )
            
            val result = supabase.from("friend_requests")
                .insert(dto) {
                    select(Columns.raw("*, sender:users!sender_id(*), receiver:users!receiver_id(*)"))
                }
                .decodeSingle<FriendRequestDto>()
            
            result.toDomain()
        }
    }

    override suspend fun getPendingReceivedRequests(): Result<List<FriendRequest>> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            val results = supabase.from("friend_requests")
                .select(Columns.raw("*, sender:users!sender_id(id, username, avatar, email)")) {
                    filter {
                        eq("receiver_id", currentUserId)
                        eq("status", "pending")
                    }
                }
                .decodeList<FriendRequestDto>()
            
            results.map { it.toDomain() }
        }
    }

    override suspend fun getSentRequests(): Result<List<FriendRequest>> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            val results = supabase.from("friend_requests")
                .select(Columns.raw("*, receiver:users!receiver_id(id, username, avatar, email)")) {
                    filter {
                        eq("sender_id", currentUserId)
                        eq("status", "pending")
                    }
                }
                .decodeList<FriendRequestDto>()
            
            results.map { it.toDomain() }
        }
    }

    override suspend fun acceptFriendRequest(requestId: String): Result<Unit> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            // Get the request details first
            val request = supabase.from("friend_requests")
                .select {
                    filter {
                        eq("id", requestId)
                        eq("receiver_id", currentUserId)
                        eq("status", "pending")
                    }
                }
                .decodeSingleOrNull<FriendRequestDto>()
                ?: throw IllegalStateException("Friend request not found")
            
            // Update request status
            supabase.from("friend_requests")
                .update(
                    UpdateFriendRequestDto(
                        status = FriendRequestStatus.ACCEPTED.toDbValue(),
                        updatedAt = Instant.now().toString()
                    )
                ) {
                    filter {
                        eq("id", requestId)
                    }
                }
            
            // Create friendship (order IDs for constraint)
            val orderedIds = listOf(request.senderId, request.receiverId).sorted()
            supabase.from("friendships")
                .insert(
                    CreateFriendshipDto(
                        user1Id = orderedIds[0],
                        user2Id = orderedIds[1]
                    )
                )
        }
    }

    override suspend fun rejectFriendRequest(requestId: String): Result<Unit> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            supabase.from("friend_requests")
                .update(
                    UpdateFriendRequestDto(
                        status = FriendRequestStatus.REJECTED.toDbValue(),
                        updatedAt = Instant.now().toString()
                    )
                ) {
                    filter {
                        eq("id", requestId)
                        eq("receiver_id", currentUserId)
                        eq("status", "pending")
                    }
                }
        }
    }

    override suspend fun cancelFriendRequest(requestId: String): Result<Unit> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            supabase.from("friend_requests")
                .delete {
                    filter {
                        eq("id", requestId)
                        eq("sender_id", currentUserId)
                        eq("status", "pending")
                    }
                }
        }
    }

    // =============== FRIENDSHIPS ===============

    override suspend fun getFriends(): Result<List<Friendship>> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            // Query friendships where user is either user1 or user2
            // Join with users table to get friend info
            val results = supabase.from("friendships")
                .select(Columns.raw("""
                    id,
                    user1_id,
                    user2_id,
                    created_at,
                    friend1:users!user1_id(id, username, avatar, email),
                    friend2:users!user2_id(id, username, avatar, email)
                """.trimIndent())) {
                    filter {
                        or {
                            eq("user1_id", currentUserId)
                            eq("user2_id", currentUserId)
                        }
                    }
                }
                .decodeList<FriendshipWithBothUsersDto>()
            
            results.map { dto ->
                // Xác định friend là user1 hay user2
                val friend = if (dto.user1Id == currentUserId) dto.friend2 else dto.friend1
                Friendship(
                    id = dto.id,
                    friendId = friend?.id ?: "",
                    friendUsername = friend?.username ?: "Unknown",
                    friendAvatar = friend?.avatar,
                    friendEmail = friend?.email ?: "",
                    createdAt = try {
                        Instant.parse(dto.createdAt)
                    } catch (e: Exception) {
                        Instant.now()
                    }
                )
            }
        }
    }

    override suspend fun unfriend(friendshipId: String): Result<Unit> {
        return runCatching {
            supabase.from("friendships")
                .delete {
                    filter {
                        eq("id", friendshipId)
                    }
                }
        }
    }

    // =============== BLOCKED USERS ===============

    override suspend fun blockUser(userId: String): Result<Unit> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            val dto = CreateBlockedUserDto(
                blockerId = currentUserId,
                blockedId = userId
            )
            
            supabase.from("blocked_users").insert(dto)
            
            // Optionally: remove friendship if exists
            // This could also be done via database trigger
        }
    }

    override suspend fun unblockUser(blockedUserId: String): Result<Unit> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            supabase.from("blocked_users")
                .delete {
                    filter {
                        eq("blocker_id", currentUserId)
                        eq("blocked_id", blockedUserId)
                    }
                }
        }
    }

    override suspend fun getBlockedUsers(): Result<List<UserBasicDto>> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            val results = supabase.from("blocked_users")
                .select(Columns.raw("blocked:users!blocked_id(id, username, avatar, email)")) {
                    filter {
                        eq("blocker_id", currentUserId)
                    }
                }
                .decodeList<BlockedUserDto>()
            
            results.mapNotNull { it.blocked }
        }
    }

    // =============== USER SEARCH ===============

    override suspend fun searchUsers(query: String): Result<List<UserBasicDto>> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            supabase.from("users")
                .select(Columns.raw("id, username, avatar, email")) {
                    filter {
                        ilike("username", "%$query%")
                        neq("id", currentUserId)
                    }
                    limit(20)
                }
                .decodeList<UserBasicDto>()
        }
    }

    override suspend fun getRelationshipStatus(userId: String): Result<RelationshipStatus> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            // Check if blocked
            val blocked = supabase.from("blocked_users")
                .select {
                    filter {
                        eq("blocker_id", currentUserId)
                        eq("blocked_id", userId)
                    }
                }
                .decodeList<BlockedUserDto>()
            
            if (blocked.isNotEmpty()) {
                return@runCatching RelationshipStatus.Blocked
            }
            
            // Check if blocked by
            val blockedBy = supabase.from("blocked_users")
                .select {
                    filter {
                        eq("blocker_id", userId)
                        eq("blocked_id", currentUserId)
                    }
                }
                .decodeList<BlockedUserDto>()
            
            if (blockedBy.isNotEmpty()) {
                return@runCatching RelationshipStatus.BlockedBy
            }
            
            // Check friendships
            val orderedIds = listOf(currentUserId, userId).sorted()
            val friendship = supabase.from("friendships")
                .select {
                    filter {
                        eq("user1_id", orderedIds[0])
                        eq("user2_id", orderedIds[1])
                    }
                }
                .decodeList<FriendshipDto>()
            
            if (friendship.isNotEmpty()) {
                return@runCatching RelationshipStatus.Friends
            }
            
            // Check pending requests (sent)
            val sentRequest = supabase.from("friend_requests")
                .select {
                    filter {
                        eq("sender_id", currentUserId)
                        eq("receiver_id", userId)
                        eq("status", "pending")
                    }
                }
                .decodeList<FriendRequestDto>()
            
            if (sentRequest.isNotEmpty()) {
                return@runCatching RelationshipStatus.RequestSent
            }
            
            // Check pending requests (received)
            val receivedRequest = supabase.from("friend_requests")
                .select {
                    filter {
                        eq("sender_id", userId)
                        eq("receiver_id", currentUserId)
                        eq("status", "pending")
                    }
                }
                .decodeList<FriendRequestDto>()
            
            if (receivedRequest.isNotEmpty()) {
                return@runCatching RelationshipStatus.RequestReceived
            }
            
            RelationshipStatus.None
        }
    }
}

/**
 * DTO helper for friendship query with both user joins
 */
@kotlinx.serialization.Serializable
private data class FriendshipWithBothUsersDto(
    val id: String,
    @kotlinx.serialization.SerialName("user1_id")
    val user1Id: String,
    @kotlinx.serialization.SerialName("user2_id")
    val user2Id: String,
    @kotlinx.serialization.SerialName("created_at")
    val createdAt: String,
    val friend1: UserBasicDto? = null,
    val friend2: UserBasicDto? = null
)
