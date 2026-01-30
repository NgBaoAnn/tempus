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


class SupabaseFriendRepository(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : FriendRepository {

    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")
    }

    
    override suspend fun sendFriendRequest(receiverId: String): Result<FriendRequest> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            
            if (currentUserId == receiverId) {
                throw IllegalArgumentException("Cannot send friend request to yourself")
            }
            
            val dto = CreateFriendRequestDto(
                senderId = currentUserId,
                receiverId = receiverId
            )
            
            
            val result = supabase.from("friend_requests")
                .insert(dto) {
                    select(Columns.raw("id, sender_id, receiver_id, status, created_at, updated_at"))
                }
                .decodeSingle<FriendRequestSimpleDto>()
            
            
            val receiverProfile = fetchUsersDetails(listOf(receiverId))[receiverId]
            
            FriendRequest(
                id = result.id,
                senderId = result.senderId,
                senderUsername = "Me",
                senderAvatar = null,
                receiverId = result.receiverId,
                receiverUsername = receiverProfile?.username ?: "User",
                receiverAvatar = receiverProfile?.avatar,
                status = FriendRequestStatus.fromString(result.status ?: "pending"),
                createdAt = try {
                    result.createdAt?.let { Instant.parse(it) } ?: Instant.now()
                } catch (e: Exception) { Instant.now() },
                updatedAt = try {
                    result.updatedAt?.let { Instant.parse(it) } ?: Instant.now()
                } catch (e: Exception) { Instant.now() }
            )
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
            
            
            val requests = supabase.from("friend_requests")
                .select(Columns.raw("id, sender_id, receiver_id, status, created_at, updated_at")) {
                    filter {
                        eq("receiver_id", currentUserId)
                        eq("status", "pending")
                    }
                }
                .decodeList<FriendRequestSimpleDto>()
            
            if (requests.isEmpty()) return@runCatching emptyList()

            
            val senderIds = requests.map { it.senderId }.distinct()
            val sendersMap = fetchUsersDetails(senderIds)

            
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
            
            
            val requests = supabase.from("friend_requests")
                .select(Columns.raw("id, sender_id, receiver_id, status, created_at, updated_at")) {
                    filter {
                        eq("sender_id", currentUserId)
                        eq("status", "pending")
                    }
                }
                .decodeList<FriendRequestSimpleDto>()
            
            if (requests.isEmpty()) return@runCatching emptyList()

            
            val receiverIds = requests.map { it.receiverId }.distinct()
            val receiversMap = fetchUsersDetails(receiverIds)

            
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

    
    override suspend fun getFriends(): Result<List<Friendship>> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            
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

            
            val friendIds = results.map { 
                if (it.user1Id == currentUserId) it.user2Id else it.user1Id 
            }.distinct()

            
            val friendsMap = fetchUsersDetails(friendIds)
            
            
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

    
    override suspend fun blockUser(userId: String): Result<Unit> {
    return runCatching {
        val currentUserId = getCurrentUserId()
        
        
        try {
            supabase.from("friendships")
                .delete {
                    filter {
                        or {
                            and {
                                eq("user1_id", currentUserId)
                                eq("user2_id", userId)
                            }
                            and {
                                eq("user1_id", userId)
                                eq("user2_id", currentUserId)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            
        }
        
        
        try {
            supabase.from("friend_requests")
                .delete {
                    filter {
                        or {
                            and {
                                eq("sender_id", currentUserId)
                                eq("receiver_id", userId)
                            }
                            and {
                                eq("sender_id", userId)
                                eq("receiver_id", currentUserId)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            
        }
        
        
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
            
            
            val results = supabase.from("blocked_users")
                .select(Columns.raw("id, blocker_id, blocked_id, created_at")) {
                    filter {
                        eq("blocker_id", currentUserId)
                    }
                }
                .decodeList<BlockedUserSimpleDto>()
            
            if (results.isEmpty()) return@runCatching emptyList()
            
            
            val blockedIds = results.map { it.blockedId }.distinct()
            val blockedUsersMap = fetchUsersDetails(blockedIds)

            
            results.mapNotNull { dto ->
                val user = blockedUsersMap[dto.blockedId]
                user 
            }
        }
    }

    
    override suspend fun getAllBlockedUserIds(): Result<List<String>> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            val blockedIds = mutableSetOf<String>()
            
            
            try {
                val iBlocked = supabase.from("blocked_users")
                    .select(Columns.raw("blocked_id")) {
                        filter {
                            eq("blocker_id", currentUserId)
                        }
                    }
                    .decodeList<BlockedIdDto>()
                blockedIds.addAll(iBlocked.map { it.blockedId })
            } catch (e: Exception) {
                
                e.printStackTrace()
            }
            
            
            try {
                val blockedMe = supabase.from("blocked_users")
                    .select(Columns.raw("blocker_id")) {
                        filter {
                            eq("blocked_id", currentUserId)
                        }
                    }
                    .decodeList<BlockerIdDto>()
                blockedIds.addAll(blockedMe.map { it.blockerId })
            } catch (e: Exception) {
                
                e.printStackTrace()
            }
            
            blockedIds.toList()
        }
    }

    
    override suspend fun isUserBlocked(userId: String): Result<Boolean> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            
            val iBlocked = supabase.from("blocked_users")
                .select(Columns.raw("id")) {
                    filter {
                        eq("blocker_id", currentUserId)
                        eq("blocked_id", userId)
                    }
                    limit(1)
                }
                .decodeList<BlockedUserSimpleDto>()
            
            if (iBlocked.isNotEmpty()) return@runCatching true
            
            
            val theyBlocked = supabase.from("blocked_users")
                .select(Columns.raw("id")) {
                    filter {
                        eq("blocker_id", userId)
                        eq("blocked_id", currentUserId)
                    }
                    limit(1)
                }
                .decodeList<BlockedUserSimpleDto>()
            
            theyBlocked.isNotEmpty()
        }
    }

    
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

    
    override suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return runCatching {
            
            val userDto = supabase.from("users")
                .select(Columns.raw("id, username, avatar, email, created_at")) {
                    filter { eq("id", userId) }
                }
                .decodeSingle<UserProfileDto>()
            
            
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

            
            val treesCount = supabase.from("trees")
                .select(Columns.raw("count")) {
                    filter {
                        eq("user_id", userId)
                        eq("is_alive", true)
                    }
                    count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                }.countOrNull() ?: 0
                
            
            val relationship = getRelationshipStatus(userId).getOrDefault(RelationshipStatus.None)
            
            
            UserProfile(
                id = userDto.id,
                username = userDto.username,
                avatar = userDto.avatar,
                email = userDto.email,
                joinedDate = try {
                    Instant.parse(userDto.createdAt).toString()
                } catch (e: Exception) { "" },
                friendsCount = friendsCount.toInt(),
                treesCount = treesCount.toInt(), 
                relationshipStatus = relationship
            )
        }
    }

    
    override suspend fun getAllUsers(excludedIds: List<String>): Result<List<UserBasicDto>> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            
            supabase.from("users")
                .select(Columns.raw("id, username, avatar, email")) {
                    filter {
                        neq("id", currentUserId)
                        
                            
                            excludedIds.forEach { neq("id", it) }
                    }
                    order("username", Order.ASCENDING)
                    limit(50) 
                }
                .decodeList<UserBasicDto>()
        }
    }
}


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
