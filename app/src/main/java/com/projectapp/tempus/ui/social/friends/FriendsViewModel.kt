package com.projectapp.tempus.ui.social.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.social.dto.UserBasicDto
import com.projectapp.tempus.data.social.repository.FriendRepository
import com.projectapp.tempus.data.social.repository.RelationshipStatus
import com.projectapp.tempus.data.social.repository.SupabaseFriendRepository
import com.projectapp.tempus.domain.social.model.FriendRequest
import com.projectapp.tempus.domain.social.model.Friendship
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State cho Friends Screen
 */
data class FriendsUiState(
    val friends: List<Friendship> = emptyList(),
    val pendingRequests: List<FriendRequest> = emptyList(),
    val sentRequests: List<FriendRequest> = emptyList(),
    val searchResults: List<UserBasicDto> = emptyList(),
    val blockedUsers: List<UserBasicDto> = emptyList(),
    val discoverUsers: List<UserBasicDto> = emptyList(), // All users for Discover tab
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val isLoadingDiscover: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedTab: FriendsTab = FriendsTab.DISCOVER // Default to Discover tab
)

enum class FriendsTab {
    DISCOVER,  // Thêm Discover tab - hiển thị tất cả users
    FRIENDS,
    REQUESTS,
    BLOCKED
}

/**
 * ViewModel cho Friends Screen
 */
class FriendsViewModel(
    private val friendRepository: FriendRepository = SupabaseFriendRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * Load tất cả dữ liệu ban đầu
     */
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Load friends
            friendRepository.getFriends()
                .onSuccess { friends ->
                    _uiState.update { it.copy(friends = friends) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Không thể tải danh sách bạn bè") }
                }
            
            // Load pending requests
            friendRepository.getPendingReceivedRequests()
                .onSuccess { requests ->
                    _uiState.update { it.copy(pendingRequests = requests) }
                }
                .onFailure { e ->
                    // Just log error for requests, don't show user error yet if other parts succeed
                }
            
            // Load sent requests
            friendRepository.getSentRequests()
                .onSuccess { requests ->
                    _uiState.update { it.copy(sentRequests = requests) }
                }
            
            _uiState.update { it.copy(isLoading = false) }
            
            // Load discover users (with filtering) after loading other data
            loadAllUsers()
        }
    }

    /**
     * Chuyển tab
     */
    fun selectTab(tab: FriendsTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        
        // Load data based on tab
        when (tab) {
            FriendsTab.DISCOVER -> {
                if (_uiState.value.discoverUsers.isEmpty()) {
                    loadAllUsers()
                }
            }
            FriendsTab.BLOCKED -> {
                if (_uiState.value.blockedUsers.isEmpty()) {
                    loadBlockedUsers()
                }
            }
            else -> { /* Data already loaded in loadData() */ }
        }
    }

    /**
     * Load tất cả users cho Discover tab
     */
    fun loadAllUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDiscover = true) }
            
            // 1. First, fetch all blocked user IDs (both directions)
            val allBlockedIds = friendRepository.getAllBlockedUserIds()
                .getOrDefault(emptyList())
                .toMutableList()
            
            // Gather other IDs to exclude
            val state = _uiState.value
            val excludedIds = mutableListOf<String>()
            
            // Exclude Friends
            excludedIds.addAll(state.friends.map { it.friendId })
            
            // Exclude Pending Requests (Senders) - Don't show people who sent us requests
            excludedIds.addAll(state.pendingRequests.map { it.senderId })
            
            // Exclude ALL Blocked Users (both directions)
            excludedIds.addAll(allBlockedIds)
            
            friendRepository.getAllUsers(excludedIds)
                .onSuccess { users ->
                    _uiState.update { it.copy(discoverUsers = users, isLoadingDiscover = false) }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            error = "Không thể tải danh sách users",
                            isLoadingDiscover = false
                        ) 
                    }
                }
        }
    }

    /**
     * Tìm kiếm user theo username
     */
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            
            friendRepository.searchUsers(query)
                .onSuccess { users ->
                    _uiState.update { it.copy(searchResults = users, isSearching = false) }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            searchResults = emptyList(), 
                            isSearching = false,
                            error = "Không thể tìm kiếm"
                        ) 
                    }
                }
        }
    }

    /**
     * Gửi lời mời kết bạn
     */
    fun sendFriendRequest(userId: String) {
        viewModelScope.launch {
            friendRepository.sendFriendRequest(userId)
                .onSuccess { request ->
                    _uiState.update { state ->
                        state.copy(
                            sentRequests = state.sentRequests + request,
                            searchResults = emptyList(),
                            successMessage = "Đã gửi lời mời kết bạn!"
                        )
                    }
                }
                .onFailure { e ->
                    // User-friendly error message
                    _uiState.update { it.copy(error = "Kết bạn không thành công") }
                }
        }
    }

    /**
     * Chấp nhận lời mời
     */
    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            friendRepository.acceptFriendRequest(requestId)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Đã chấp nhận lời mời!") }
                    loadData() // Reload to update friends list
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Không thể chấp nhận") }
                }
        }
    }

    /**
     * Từ chối lời mời
     */
    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            friendRepository.rejectFriendRequest(requestId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            pendingRequests = state.pendingRequests.filter { it.id != requestId },
                            successMessage = "Đã từ chối lời mời"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Không thể từ chối") }
                }
        }
    }

    /**
     * Huỷ lời mời đã gửi
     */
    fun cancelRequest(requestId: String) {
        viewModelScope.launch {
            friendRepository.cancelFriendRequest(requestId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            sentRequests = state.sentRequests.filter { it.id != requestId },
                            successMessage = "Đã huỷ lời mời"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Không thể huỷ") }
                }
        }
    }

    /**
     * Huỷ kết bạn
     */
    fun unfriend(friendshipId: String) {
        viewModelScope.launch {
            friendRepository.unfriend(friendshipId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            friends = state.friends.filter { it.id != friendshipId },
                            successMessage = "Đã huỷ kết bạn"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Không thể huỷ kết bạn") }
                }
        }
    }

    /**
     * Chặn user
     */
    fun blockUser(userId: String) {
        viewModelScope.launch {
            friendRepository.blockUser(userId)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Đã chặn người dùng") }
                    loadData()
                    loadBlockedUsers()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Không thể chặn") }
                }
        }
    }

    /**
     * Bỏ chặn user
     */
    fun unblockUser(userId: String) {
        viewModelScope.launch {
            friendRepository.unblockUser(userId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            blockedUsers = state.blockedUsers.filter { it.id != userId },
                            successMessage = "Đã bỏ chặn"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Không thể bỏ chặn") }
                }
        }
    }

    /**
     * Load danh sách user đã chặn
     */
    fun loadBlockedUsers() {
        viewModelScope.launch {
            friendRepository.getBlockedUsers()
                .onSuccess { users ->
                    _uiState.update { it.copy(blockedUsers = users) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Không thể tải blocked list") }
                }
        }
    }

    /**
     * Clear messages
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearSearchResults() {
        _uiState.update { it.copy(searchResults = emptyList()) }
    }
}
