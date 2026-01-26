package com.projectapp.tempus.ui.social.friends.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectapp.tempus.data.social.dto.UserBasicDto
import com.projectapp.tempus.domain.social.model.FriendRequest
import com.projectapp.tempus.domain.social.model.Friendship
import com.projectapp.tempus.ui.social.friends.FriendsTab
import com.projectapp.tempus.ui.social.friends.FriendsUiState

import com.projectapp.tempus.ui.theme.TempusDesignSystem
import com.projectapp.tempus.ui.components.TempusCard
import com.projectapp.tempus.ui.social.friends.FriendsViewModel

/**
 * Premium Blue Liquid Glass Design System
 * Style: Flowing glass, smooth transitions, translucent, modern premium
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel = viewModel(),
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToMessages: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSearchDialog by remember { mutableStateOf(false) }
    
    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error/success messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSearchDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = "Tìm bạn bè")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            FriendsHeader(
                pendingCount = uiState.pendingRequests.size,
                onMessagesClick = onNavigateToMessages
            )
            
            // Tab Bar
            FriendsTabBar(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::selectTab,
                pendingCount = uiState.pendingRequests.size
            )
            
            // Content
            when {
                uiState.selectedTab == FriendsTab.DISCOVER && uiState.isLoadingDiscover -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                else -> {
                    when (uiState.selectedTab) {
                        FriendsTab.DISCOVER -> DiscoverList(
                            users = uiState.discoverUsers,
                            onSendRequest = viewModel::sendFriendRequest,
                            onRefresh = viewModel::loadAllUsers
                        )
                        FriendsTab.FRIENDS -> FriendsList(
                            friends = uiState.friends,
                            onUnfriend = viewModel::unfriend,
                            onChat = onNavigateToChat
                        )
                        FriendsTab.REQUESTS -> RequestsList(
                            pendingRequests = uiState.pendingRequests,
                            sentRequests = uiState.sentRequests,
                            onAccept = viewModel::acceptRequest,
                            onReject = viewModel::rejectRequest,
                            onCancel = viewModel::cancelRequest
                        )
                        FriendsTab.BLOCKED -> BlockedList(
                            blockedUsers = uiState.blockedUsers,
                            onUnblock = viewModel::unblockUser
                        )
                    }
                }
            }
        }
    }
    
    // Search Dialog
    if (showSearchDialog) {
        SearchUserDialog(
            searchResults = uiState.searchResults,
            isSearching = uiState.isSearching,
            onSearch = viewModel::searchUsers,
            onSendRequest = viewModel::sendFriendRequest,
            onDismiss = { 
                showSearchDialog = false
                viewModel.clearSearchResults()
            }
        )
    }
}

@Composable
private fun FriendsHeader(
    pendingCount: Int,
    onMessagesClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Kết nối",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(TempusDesignSystem.Success, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (pendingCount > 0) "$pendingCount lời mời mới" else "Đang trực tuyến",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Messages button with glass effect
                Surface(
                    onClick = onMessagesClick,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.2f))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Filled.MailOutline,
                            contentDescription = "Tin nhắn",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsTabBar(
    selectedTab: FriendsTab,
    onTabSelected: (FriendsTab) -> Unit,
    pendingCount: Int
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
            if (selectedTab.ordinal < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.BottomStart)
                        .offset(x = tabPositions[selectedTab.ordinal].left)
                        .width(tabPositions[selectedTab.ordinal].width),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        divider = {}
    ) {
        // Discover tab
        Tab(
            selected = selectedTab == FriendsTab.DISCOVER,
            onClick = { onTabSelected(FriendsTab.DISCOVER) },
            text = { 
                Text(
                    "Khám phá",
                    fontWeight = if (selectedTab == FriendsTab.DISCOVER) FontWeight.Bold else FontWeight.Medium
                )
            }
        )
        // Friends tab  
        Tab(
            selected = selectedTab == FriendsTab.FRIENDS,
            onClick = { onTabSelected(FriendsTab.FRIENDS) },
            text = { 
                Text(
                    "Bạn bè",
                    fontWeight = if (selectedTab == FriendsTab.FRIENDS) FontWeight.Bold else FontWeight.Medium
                )
            }
        )
        // Requests tab
        Tab(
            selected = selectedTab == FriendsTab.REQUESTS,
            onClick = { onTabSelected(FriendsTab.REQUESTS) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Lời mời",
                        fontWeight = if (selectedTab == FriendsTab.REQUESTS) FontWeight.Bold else FontWeight.Medium
                    )
                    if (pendingCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        ) {
                            Text(pendingCount.toString())
                        }
                    }
                }
            }
        )
        // Blocked tab
        Tab(
            selected = selectedTab == FriendsTab.BLOCKED,
            onClick = { onTabSelected(FriendsTab.BLOCKED) },
            text = { 
                Text(
                    "Đã chặn",
                    fontWeight = if (selectedTab == FriendsTab.BLOCKED) FontWeight.Bold else FontWeight.Medium
                )
            }
        )
    }
}

@Composable
private fun FriendsList(
    friends: List<Friendship>,
    onUnfriend: (String) -> Unit,
    onChat: (String) -> Unit
) {
    if (friends.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.People,
            title = "Chưa có bạn bè",
            subtitle = "Tìm và kết nối với bạn bè mới!"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(friends, key = { it.id }) { friend ->
                FriendCard(
                    friend = friend,
                    onUnfriend = { onUnfriend(friend.id) },
                    onChat = { onChat(friend.friendId) }
                )
            }
        }
    }
}

@Composable
private fun FriendCard(
    friend: Friendship,
    onUnfriend: () -> Unit,
    onChat: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            UserAvatar(
                username = friend.friendUsername,
                avatarUrl = friend.friendAvatar,
                size = 52
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.friendUsername,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = friend.friendEmail,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Chat button
            IconButton(onClick = onChat) {
                Icon(
                    Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Nhắn tin",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // More options
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Thêm",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Huỷ kết bạn", color = TempusDesignSystem.Error) },
                        onClick = {
                            showMenu = false
                            onUnfriend()
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.PersonRemove, null, tint = TempusDesignSystem.Error)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestsList(
    pendingRequests: List<FriendRequest>,
    sentRequests: List<FriendRequest>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onCancel: (String) -> Unit
) {
    if (pendingRequests.isEmpty() && sentRequests.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.MailOutline,
            title = "Không có lời mời",
            subtitle = "Các lời mời kết bạn sẽ hiện ở đây"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pendingRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "Lời mời đã nhận",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(pendingRequests, key = { it.id }) { request ->
                    FriendRequestCard(
                        request = request,
                        isReceived = true,
                        onAccept = { onAccept(request.id) },
                        onReject = { onReject(request.id) },
                        onCancel = {}
                    )
                }
            }
            
            if (sentRequests.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lời mời đã gửi",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(sentRequests, key = { it.id }) { request ->
                    FriendRequestCard(
                        request = request,
                        isReceived = false,
                        onAccept = {},
                        onReject = {},
                        onCancel = { onCancel(request.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendRequestCard(
    request: FriendRequest,
    isReceived: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            val displayName = if (isReceived) request.senderUsername else request.receiverUsername
            val avatarUrl = if (isReceived) request.senderAvatar else request.receiverAvatar
            
            UserAvatar(
                username = displayName,
                avatarUrl = avatarUrl,
                size = 48
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isReceived) "Muốn kết bạn với bạn" else "Đang chờ phản hồi",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Actions
            if (isReceived) {
                IconButton(onClick = onReject) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Từ chối",
                        tint = TempusDesignSystem.Error
                    )
                }
                IconButton(onClick = onAccept) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Chấp nhận",
                        tint = SocialColors.Secondary
                    )
                }
            } else {
                TextButton(onClick = onCancel) {
                    Text("Huỷ", color = TempusDesignSystem.Error)
                }
            }
        }
    }
}

@Composable
private fun BlockedList(
    blockedUsers: List<UserBasicDto>,
    onUnblock: (String) -> Unit
) {
    if (blockedUsers.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.Block,
            title = "Không có ai bị chặn",
            subtitle = "Người dùng bị chặn sẽ hiện ở đây"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(blockedUsers, key = { it.id }) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            username = user.username,
                            avatarUrl = user.avatar,
                            size = 44
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = user.username,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        TextButton(onClick = { onUnblock(user.id) }) {
                            Text("Bỏ chặn", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserAvatar(
    username: String,
    avatarUrl: String?,
    size: Int
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(TempusDesignSystem.PrimaryLight),
        contentAlignment = Alignment.Center
    ) {
        // TODO: Load actual avatar with Coil/Glide
        Text(
            text = username.firstOrNull()?.uppercase() ?: "?",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = (size / 2).sp
        )
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// =============== DISCOVER LIST ===============

@Composable
private fun DiscoverList(
    users: List<UserBasicDto>,
    onSendRequest: (String) -> Unit,
    onRefresh: () -> Unit
) {
    if (users.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.Explore,
            title = "Không có user nào",
            subtitle = "Hiện không có user nào để hiển thị"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with count and refresh
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Khám phá bạn bè",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${users.size} người dùng",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Làm mới",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            items(users, key = { it.id }) { user ->
                DiscoverUserCard(
                    user = user,
                    onSendRequest = { onSendRequest(user.id) }
                )
            }
        }
    }
}

@Composable
private fun DiscoverUserCard(
    user: UserBasicDto,
    onSendRequest: () -> Unit
) {
    var requestSent by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with gradient border
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(TempusDesignSystem.PrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.username.firstOrNull()?.uppercase() ?: "?",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            // User info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                user.email?.let { email ->
                    Text(
                        text = email,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Action button
            if (requestSent) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Đã gửi",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                FilledTonalButton(
                    onClick = {
                        onSendRequest()
                        requestSent = true
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Filled.PersonAdd,
                        contentDescription = "Thêm bạn",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Thêm",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
