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
import io.github.jan.supabase.postgrest.query.Order
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
            
            // Use simple DTO without FK joins to avoid parsing issues
            val result = supabase.from("friend_requests")
                .insert(dto) {
                    select(Columns.raw("id, sender_id, receiver_id, status, created_at, updated_at"))
                }
                .decodeSingle<FriendRequestSimpleDto>()
            
            result.toDomain()
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { e ->
                e.printStackTrace()
                val friendlyException = when {
                    e.javaClass.simpleName.contains("RestException") -> 
                        Exception("Lỗi máy chủ: Kiểm tra lại quyền truy cập (RLS) hoặc dữ liệu.")
                    e.javaClass.simpleName.contains("HttpRequestTimeoutException") -> 
                        Exception("Kết nối quá hạn. Vui lòng kiểm tra mạng.")
                    else -> e
                }
                Result.failure(friendlyException)
            }
        )
    }

    // Helper to fetch user details manually
    private suspend fun fetchUsersDetails(userIds: List<String>): Map<String, UserBasicDto> {
        if (userIds.isEmpty()) return emptyMap()
        
        return runCatching {
            supabase.from("users")
                .select(Columns.raw("id, username, avatar, email")) {
                    filter {
                        isIn("id", userIds)
                    }
                }
                .decodeList<UserBasicDto>()
                .associateBy { it.id }
        }.getOrDefault(emptyMap())
    }

    override suspend fun getPendingReceivedRequests(): Result<List<FriendRequest>> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            // 1. Get simple requests
            val requests = supabase.from("friend_requests")
                .select(Columns.raw("id, sender_id, receiver_id, status, created_at, updated_at")) {
                    filter {
                        eq("receiver_id", currentUserId)
                        eq("status", "pending")
                    }
                }
                .decodeList<FriendRequestSimpleDto>()
            
            if (requests.isEmpty()) return@runCatching emptyList()

            // 2. Fetch sender details
            val senderIds = requests.map { it.senderId }.distinct()
            val sendersMap = fetchUsersDetails(senderIds)

            // 3. Map to domain
            requests.map { dto ->
                val sender = sendersMap[dto.senderId]
                FriendRequest(
                    id = dto.id,
                    senderId = dto.senderId,
                    senderUsername = sender?.username ?: "User",
                    senderAvatar = sender?.avatar,
                    receiverId = dto.receiverId,
                    receiverUsername = "Me",
                    receiverAvatar = null,
                    status = FriendRequestStatus.fromString(dto.status ?: "pending"),
                    createdAt = try {
                        Instant.parse(dto.createdAt)
                    } catch (e: Exception) { Instant.now() },
                    updatedAt = try {
                        dto.updatedAt?.let { Instant.parse(it) } ?: Instant.now()
                    } catch (e: Exception) { Instant.now() }
                )
            }
        }
    }

    override suspend fun getSentRequests(): Result<List<FriendRequest>> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            // 1. Get simple requests
            val requests = supabase.from("friend_requests")
                .select(Columns.raw("id, sender_id, receiver_id, status, created_at, updated_at")) {
                    filter {
                        eq("sender_id", currentUserId)
                        eq("status", "pending")
                    }
                }
                .decodeList<FriendRequestSimpleDto>()
            
            if (requests.isEmpty()) return@runCatching emptyList()

            // 2. Fetch receiver details
            val receiverIds = requests.map { it.receiverId }.distinct()
            val receiversMap = fetchUsersDetails(receiverIds)

            // 3. Map to domain
            requests.map { dto ->
                val receiver = receiversMap[dto.receiverId]
                FriendRequest(
                    id = dto.id,
                    senderId = dto.senderId,
                    senderUsername = "Me",
                    senderAvatar = null,
                    receiverId = dto.receiverId,
                    receiverUsername = receiver?.username ?: "User",
                    receiverAvatar = receiver?.avatar,
                    status = FriendRequestStatus.fromString(dto.status ?: "pending"),
                    createdAt = try {
                        Instant.parse(dto.createdAt)
                    } catch (e: Exception) { Instant.now() },
                    updatedAt = try {
                        dto.updatedAt?.let { Instant.parse(it) } ?: Instant.now()
                    } catch (e: Exception) { Instant.now() }
                )
            }
        }
    }

    override suspend fun acceptFriendRequest(requestId: String): Result<Unit> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            // Get the request details first
            val request = supabase.from("friend_requests")
                .select(Columns.raw("id, sender_id, receiver_id, status")) {
                    filter {
                        eq("id", requestId)
                        eq("receiver_id", currentUserId)
                        eq("status", "pending")
                    }
                }
                .decodeSingleOrNull<FriendRequestSimpleDto>()
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
            
            // 1. Get simple friendships
            val results = supabase.from("friendships")
                .select(Columns.raw("id, user1_id, user2_id, created_at")) {
                    filter {
                        or {
                            eq("user1_id", currentUserId)
                            eq("user2_id", currentUserId)
                        }
                    }
                }
                .decodeList<FriendshipSimpleDto>()
            
            if (results.isEmpty()) return@runCatching emptyList()

            // 2. Identify friend IDs
            val friendIds = results.map { 
                if (it.user1Id == currentUserId) it.user2Id else it.user1Id 
            }.distinct()

            // 3. Keep Fetching friend details
            val friendsMap = fetchUsersDetails(friendIds)
            
            // 4. Map to domain
            results.map { dto ->
                val friendId = if (dto.user1Id == currentUserId) dto.user2Id else dto.user1Id
                val friend = friendsMap[friendId]
                
                Friendship(
                    id = dto.id,
                    friendId = friendId,
                    friendUsername = friend?.username ?: "User",
                    friendAvatar = friend?.avatar,
                    friendEmail = friend?.email ?: "",
                    createdAt = try {
                        dto.createdAt?.let { Instant.parse(it) } ?: Instant.now()
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
            
            // 1. Get blocked list
            val results = supabase.from("blocked_users")
                .select(Columns.raw("id, blocker_id, blocked_id, created_at")) {
                    filter {
                        eq("blocker_id", currentUserId)
                    }
                }
                .decodeList<BlockedUserSimpleDto>()
            
            if (results.isEmpty()) return@runCatching emptyList()
            
            // 2. Fetch blocked users details
            val blockedIds = results.map { it.blockedId }.distinct()
            val blockedUsersMap = fetchUsersDetails(blockedIds)

            // 3. Map to DTOs
            results.mapNotNull { dto ->
                val user = blockedUsersMap[dto.blockedId]
                user // We return the UserBasicDto directly as per return type
            }
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
                .select(Columns.raw("id, blocker_id, blocked_id")) {
                    filter {
                        eq("blocker_id", currentUserId)
                        eq("blocked_id", userId)
                    }
                }
                .decodeList<BlockedUserSimpleDto>()
            
            if (blocked.isNotEmpty()) {
                return@runCatching RelationshipStatus.Blocked
            }
            
            // Check if blocked by
            val blockedBy = supabase.from("blocked_users")
                .select(Columns.raw("id, blocker_id, blocked_id")) {
                    filter {
                        eq("blocker_id", userId)
                        eq("blocked_id", currentUserId)
                    }
                }
                .decodeList<BlockedUserSimpleDto>()
            
            if (blockedBy.isNotEmpty()) {
                return@runCatching RelationshipStatus.BlockedBy
            }
            
            // Check friendships
            val orderedIds = listOf(currentUserId, userId).sorted()
            val friendship = supabase.from("friendships")
                .select(Columns.raw("id, user1_id, user2_id")) {
                    filter {
                        eq("user1_id", orderedIds[0])
                        eq("user2_id", orderedIds[1])
                    }
                }
                .decodeList<FriendshipSimpleDto>()
            
            if (friendship.isNotEmpty()) {
                return@runCatching RelationshipStatus.Friends
            }
            
            // Check pending requests (sent)
            val sentRequest = supabase.from("friend_requests")
                .select(Columns.raw("id, sender_id, receiver_id, status")) {
                    filter {
                        eq("sender_id", currentUserId)
                        eq("receiver_id", userId)
                        eq("status", "pending")
                    }
                }
                .decodeList<FriendRequestSimpleDto>()
            
            if (sentRequest.isNotEmpty()) {
                return@runCatching RelationshipStatus.RequestSent
            }
            
            // Check pending requests (received)
            val receivedRequest = supabase.from("friend_requests")
                .select(Columns.raw("id, sender_id, receiver_id, status")) {
                    filter {
                        eq("sender_id", userId)
                        eq("receiver_id", currentUserId)
                        eq("status", "pending")
                    }
                }
                .decodeList<FriendRequestSimpleDto>()
            
            if (receivedRequest.isNotEmpty()) {
                return@runCatching RelationshipStatus.RequestReceived
            }
            
            RelationshipStatus.None
        }
    }

    // =============== USER PROFILE ===============

    override suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return runCatching {
            // 1. Fetch User Basic Info
            val userDto = supabase.from("users")
                .select(Columns.raw("id, username, avatar, email, created_at")) {
                    filter { eq("id", userId) }
                }
                .decodeSingle<UserProfileDto>()
            
            // 2. Fetch Stats
            // Friends count
            val friendsCount = supabase.from("friendships")
                .select(Columns.raw("count")) {
                    filter {
                        or {
                            eq("user1_id", userId)
                            eq("user2_id", userId)
                        }
                    }
                    count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                }.countOrNull() ?: 0

            // Trees count - Count alive trees only
            val treesCount = supabase.from("trees")
                .select(Columns.raw("count")) {
                    filter {
                        eq("user_id", userId)
                        eq("is_alive", true)
                    }
                    count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                }.countOrNull() ?: 0
                
            // 3. Get relationship status (reuse existing)
            val relationship = getRelationshipStatus(userId).getOrDefault(RelationshipStatus.None)
            
            // 4. Map to Domain
            UserProfile(
                id = userDto.id,
                username = userDto.username,
                avatar = userDto.avatar,
                email = userDto.email,
                joinedDate = try {
                    Instant.parse(userDto.createdAt).toString()
                } catch (e: Exception) { "" },
                friendsCount = friendsCount.toInt(),
                treesCount = treesCount.toInt(), // Real value
                relationshipStatus = relationship
            )
        }
    }

    // =============== DISCOVER / ALL USERS ===============

    override suspend fun getAllUsers(excludedIds: List<String>): Result<List<UserBasicDto>> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            // Lấy tất cả users trừ current user và excludedIds
            // Sort theo username để dễ duyệt
            supabase.from("users")
                .select(Columns.raw("id, username, avatar, email")) {
                    filter {
                        neq("id", currentUserId)
                        
                            // Filter out excluded IDs using loop of neq (safe fallback)
                            excludedIds.forEach { neq("id", it) }
                    }
                    order("username", Order.ASCENDING)
                    limit(50) // Limit để tránh load quá nhiều
                }
                .decodeList<UserBasicDto>()
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
