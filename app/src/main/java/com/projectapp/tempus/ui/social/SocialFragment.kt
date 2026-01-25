package com.projectapp.tempus.ui.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.projectapp.tempus.ui.social.friends.FriendsViewModel
import com.projectapp.tempus.ui.social.friends.compose.FriendsScreen
import com.projectapp.tempus.ui.theme.TempusTheme

/**
 * Fragment chính cho Social module
 * Hiển thị danh sách bạn bè, lời mời kết bạn
 */
class SocialFragment : Fragment() {

    private val viewModel: FriendsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                TempusTheme {
                    FriendsScreen(
                        viewModel = viewModel,
                        onNavigateToChat = { friendId ->
                            // TODO: Navigate to chat screen
                            // findNavController().navigate(
                            //     SocialFragmentDirections.actionSocialToChat(friendId)
                            // )
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        viewModel.loadData()
    }
}
