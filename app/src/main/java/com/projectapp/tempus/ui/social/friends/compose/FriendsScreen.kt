package com.projectapp.tempus.ui.social.friends.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

import androidx.compose.ui.res.stringResource
import com.projectapp.tempus.R
import com.projectapp.tempus.ui.social.friends.FriendsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel = viewModel(),
    onNavigateToChat: (String, String, String?) -> Unit = { _, _, _ -> },
    onNavigateToMessages: () -> Unit = {},
    onUserClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSearchDialog by remember { mutableStateOf(false) }
    
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
        
        
        viewModel.loadBlockedUsers()
        
        
        if (uiState.selectedTab == FriendsTab.DISCOVER) {
            viewModel.loadAllUsers()
        }
    }
    
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    
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
                Icon(Icons.Filled.PersonAdd, contentDescription = stringResource(R.string.social_find_friends))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            
            FriendsHeader(
                pendingCount = uiState.pendingRequests.size,
                onMessagesClick = onNavigateToMessages
            )
            
            
            FriendsTabBar(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::selectTab,
                pendingCount = uiState.pendingRequests.size
            )
            
            
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
                            friends = uiState.friends,
                            sentRequests = uiState.sentRequests,
                            pendingRequests = uiState.pendingRequests,
                            blockedUsers = uiState.blockedUsers,
                            onSendRequest = viewModel::sendFriendRequest,
                            onAcceptRequest = viewModel::acceptRequest,
                            onRefresh = viewModel::loadAllUsers,
                            onUserClick = onUserClick
                        )
                        FriendsTab.FRIENDS -> FriendsList(
                            friends = uiState.friends,
                            onUnfriend = viewModel::unfriend,
                            onChat = onNavigateToChat,
                            onUserClick = onUserClick
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
                        text = stringResource(R.string.social_connect),
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
                            text = if (pendingCount > 0) stringResource(R.string.social_new_invites_fmt, pendingCount) else stringResource(R.string.social_online),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                
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
                            contentDescription = stringResource(R.string.social_messages),
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
        
        Tab(
            selected = selectedTab == FriendsTab.DISCOVER,
            onClick = { onTabSelected(FriendsTab.DISCOVER) },
            text = { 
                Text(
                    stringResource(R.string.social_tab_discover),
                    fontWeight = if (selectedTab == FriendsTab.DISCOVER) FontWeight.Bold else FontWeight.Medium
                )
            }
        )
        
        Tab(
            selected = selectedTab == FriendsTab.FRIENDS,
            onClick = { onTabSelected(FriendsTab.FRIENDS) },
            text = { 
                Text(
                    stringResource(R.string.social_tab_friends),
                    fontWeight = if (selectedTab == FriendsTab.FRIENDS) FontWeight.Bold else FontWeight.Medium
                )
            }
        )
        
        Tab(
            selected = selectedTab == FriendsTab.REQUESTS,
            onClick = { onTabSelected(FriendsTab.REQUESTS) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.social_tab_requests),
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
        
        Tab(
            selected = selectedTab == FriendsTab.BLOCKED,
            onClick = { onTabSelected(FriendsTab.BLOCKED) },
            text = { 
                Text(
                    stringResource(R.string.social_tab_blocked),
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
    onChat: (String, String, String?) -> Unit,
    onUserClick: (String) -> Unit
) {
    if (friends.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.People,
            title = stringResource(R.string.social_no_friends),
            subtitle = stringResource(R.string.social_find_friends)
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
                    onChat = { onChat(friend.friendId, friend.friendUsername, friend.friendAvatar) },
                    onClick = { onUserClick(friend.friendId) }
                )
            }
        }
    }
}

@Composable
private fun FriendCard(
    friend: Friendship,
    onUnfriend: () -> Unit,
    onChat: () -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
            
            UserAvatar(
                username = friend.friendUsername,
                avatarUrl = friend.friendAvatar,
                size = 52
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            
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
            
            
            IconButton(onClick = onChat) {
                Icon(
                    Icons.AutoMirrored.Filled.Chat,
                    contentDescription = stringResource(R.string.social_messages),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.social_more),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.social_unfriend), color = TempusDesignSystem.Error) },
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
            title = stringResource(R.string.social_no_requests),
            subtitle = stringResource(R.string.social_requests_empty_desc)
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pendingRequests.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.social_requests_received),
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
                        text = stringResource(R.string.social_requests_sent),
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
            
            val displayName = if (isReceived) request.senderUsername else request.receiverUsername
            val avatarUrl = if (isReceived) request.senderAvatar else request.receiverAvatar
            
            UserAvatar(
                username = displayName,
                avatarUrl = avatarUrl,
                size = 48
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isReceived) stringResource(R.string.social_want_to_connect) else stringResource(R.string.social_waiting),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            
            if (isReceived) {
                IconButton(onClick = onReject) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.social_reject),
                        tint = TempusDesignSystem.Error
                    )
                }
                IconButton(onClick = onAccept) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = stringResource(R.string.social_accept),
                        tint = SocialColors.Secondary
                    )
                }
            } else {
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.social_cancel), color = TempusDesignSystem.Error)
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
            title = stringResource(R.string.social_no_blocked),
            subtitle = stringResource(R.string.social_blocked_empty_desc)
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
                            Text(stringResource(R.string.social_unblock), color = MaterialTheme.colorScheme.primary)
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
        if (!avatarUrl.isNullOrBlank()) {
            coil.compose.AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar $username",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Text(
                text = username.firstOrNull()?.uppercase() ?: "?",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = (size / 2).sp
            )
        }
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


@Composable
private fun DiscoverList(
    users: List<UserBasicDto>,
    friends: List<Friendship>,
    sentRequests: List<FriendRequest>,
    pendingRequests: List<FriendRequest>,
    blockedUsers: List<UserBasicDto>,
    onSendRequest: (String) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onRefresh: () -> Unit,
    onUserClick: (String) -> Unit
) {
    
    val friendIds = remember(friends) { friends.map { it.friendId }.toSet() }
    val sentRequestUserIds = remember(sentRequests) { sentRequests.map { it.receiverId }.toSet() }
    val pendingRequestMap = remember(pendingRequests) { pendingRequests.associateBy { it.senderId } }
    val blockedUserIds = remember(blockedUsers) { blockedUsers.map { it.id }.toSet() }
    
    if (users.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.Explore,
            title = stringResource(R.string.social_no_users),
            subtitle = stringResource(R.string.social_no_users_desc)
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
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
                            text = stringResource(R.string.social_discover_limit),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.social_users_count, users.size),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.social_refresh),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            items(users, key = { it.id }) { user ->
                
                val isFriend = user.id in friendIds
                val isRequestSent = user.id in sentRequestUserIds
                val pendingRequest = pendingRequestMap[user.id]
                val isBlocked = user.id in blockedUserIds
                
                DiscoverUserCard(
                    user = user,
                    isFriend = isFriend,
                    isRequestSent = isRequestSent,
                    pendingRequestId = pendingRequest?.id,
                    isBlocked = isBlocked,
                    onSendRequest = { onSendRequest(user.id) },
                    onAcceptRequest = { pendingRequest?.id?.let { onAcceptRequest(it) } },
                    onClick = { onUserClick(user.id) }
                )
            }
        }
    }
}

@Composable
private fun DiscoverUserCard(
    user: UserBasicDto,
    isFriend: Boolean,
    isRequestSent: Boolean,
    pendingRequestId: String?,
    isBlocked: Boolean,
    onSendRequest: () -> Unit,
    onAcceptRequest: () -> Unit,
    onClick: () -> Unit
) {
    var localRequestSent by remember { mutableStateOf(false) }
    val hasSentRequest = isRequestSent || localRequestSent
    val hasReceivedRequest = pendingRequestId != null
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    if (!user.avatar.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model = user.avatar,
                            contentDescription = "Avatar ${user.username}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = user.username.firstOrNull()?.uppercase() ?: "?",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            
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
            
            
            when {
                isFriend -> {
                    
                    OutlinedButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        border = BorderStroke(1.dp, SocialColors.Secondary.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.Filled.People,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = SocialColors.Secondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.social_tab_friends),
                            color = SocialColors.Secondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
                hasReceivedRequest -> {
                    
                    FilledTonalButton(
                        onClick = onAcceptRequest,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = SocialColors.Secondary.copy(alpha = 0.15f),
                            contentColor = SocialColors.Secondary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = stringResource(R.string.social_accept),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.social_accept),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
                isBlocked -> {
                    
                    OutlinedButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        border = BorderStroke(1.dp, TempusDesignSystem.Error.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.Filled.Block,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = TempusDesignSystem.Error
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.social_tab_blocked),
                            color = TempusDesignSystem.Error,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
                hasSentRequest -> {
                    
                    OutlinedButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            stringResource(R.string.social_requests_sent),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
                else -> {
                    
                    FilledTonalButton(
                        onClick = {
                            onSendRequest()
                            localRequestSent = true
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
                            contentDescription = stringResource(R.string.social_add_friend),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.social_more),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
