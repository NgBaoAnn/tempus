package com.projectapp.tempus.ui.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectapp.tempus.ui.social.friends.FriendsViewModel
import com.projectapp.tempus.ui.social.friends.compose.FriendsScreen
import com.projectapp.tempus.ui.social.messages.MessagesViewModel
import com.projectapp.tempus.ui.social.messages.compose.ChatScreen
import com.projectapp.tempus.ui.social.messages.compose.ConversationsScreen
import com.projectapp.tempus.ui.theme.TempusTheme


sealed class SocialScreen {
    object Friends : SocialScreen()
    object Conversations : SocialScreen()
    data class Chat(val friendId: String, val friendUsername: String, val friendAvatar: String?) : SocialScreen()
    data class Profile(val userId: String) : SocialScreen()
}


class SocialFragment : Fragment() {

    private val friendsViewModel: FriendsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                TempusTheme {
                    SocialNavHost(
                        friendsViewModel = friendsViewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        
        friendsViewModel.loadData()
    }
}


@Composable
fun SocialNavHost(
    friendsViewModel: FriendsViewModel
) {
    var currentScreen by remember { mutableStateOf<SocialScreen>(SocialScreen.Friends) }
    val messagesViewModel: MessagesViewModel = viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        messagesViewModel.notificationEvents.collect { event ->
            com.projectapp.tempus.util.MessageNotificationHelper.showNotification(
                context = context,
                conversationId = event.conversationId,
                senderName = event.senderName,
                messageContent = event.messageContent
            )
        }
    }

    when (val screen = currentScreen) {
        is SocialScreen.Friends -> {
            FriendsScreen(
                viewModel = friendsViewModel,
                onNavigateToChat = { friendId, friendUsername, friendAvatar ->
                    currentScreen = SocialScreen.Chat(friendId, friendUsername, friendAvatar)
                },
                onNavigateToMessages = {
                    currentScreen = SocialScreen.Conversations
                },
                onUserClick = { userId ->
                    currentScreen = SocialScreen.Profile(userId)
                }
            )
        }
        
        is SocialScreen.Conversations -> {
            ConversationsScreen(
                viewModel = messagesViewModel,
                onNavigateBack = {
                    currentScreen = SocialScreen.Friends
                },
                onOpenChat = { friendId, friendUsername, friendAvatar ->
                    currentScreen = SocialScreen.Chat(friendId, friendUsername, friendAvatar)
                }
            )
        }
        
        is SocialScreen.Chat -> {
            ChatScreen(
                friendId = screen.friendId,
                friendUsername = screen.friendUsername,
                friendAvatar = screen.friendAvatar,
                viewModel = messagesViewModel,
                onNavigateBack = {
                    currentScreen = SocialScreen.Conversations
                }
            )
        }
        
        is SocialScreen.Profile -> {
            com.projectapp.tempus.ui.social.profile.FriendProfileScreen(
                userId = screen.userId,
                onBack = {
                    currentScreen = SocialScreen.Friends
                }
            )
        }
    }
}
