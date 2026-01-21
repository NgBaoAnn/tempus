package com.projectapp.tempus.ui.ai.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.R
import com.projectapp.tempus.data.ai.ChatMessage
import com.projectapp.tempus.ui.ai.AIViewModel

/**
 * Main Chat Screen composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: AIViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val suggestions by viewModel.suggestions.observeAsState(emptyList())
    val showSuggestionSheet by viewModel.showSuggestionSheet.observeAsState(false)
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    ChatTheme {
        Scaffold(
            topBar = {
                ChatTopBar(
                    isLoading = isLoading,
                    onClearChat = { viewModel.clearChat() }
                )
            },
            bottomBar = {
                ChatInput(
                    value = inputText,
                    onValueChange = { inputText = it },
                    onSend = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    enabled = !isLoading,
                    modifier = Modifier.imePadding()
                )
            },
            containerColor = ChatColors.Background,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Empty state
                AnimatedVisibility(
                    visible = messages.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    EmptyState(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Messages list
                AnimatedVisibility(
                    visible = messages.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(
                            items = messages,
                            key = { it.timestamp }
                        ) { message ->
                            MessageBubble(message = message)
                        }
                        
                        // Typing indicator
                        if (isLoading) {
                            item(key = "typing") {
                                TypingIndicator(visible = true)
                            }
                        }
                    }
                }
            }
        }
        
        // Schedule Suggestion Sheet
        if (showSuggestionSheet && suggestions.isNotEmpty()) {
            ScheduleSuggestionSheet(
                suggestions = suggestions,
                onAccept = { accepted -> viewModel.acceptSuggestions(accepted) },
                onDismiss = { viewModel.dismissSuggestions() }
            )
        }
    }
}

/**
 * Chat top app bar with avatar and status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    isLoading: Boolean,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Surface(
        shadowElevation = 2.dp,
        color = ChatColors.Surface,
        modifier = modifier
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Image(
                        painter = painterResource(id = R.drawable.ic_ai),
                        contentDescription = "AI Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = "Tiramisu AI",
                            style = MaterialTheme.typography.titleLarge,
                            color = ChatColors.TextPrimary
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isLoading) ChatColors.Typing 
                                        else ChatColors.Online
                                    )
                            )
                            
                            Spacer(modifier = Modifier.width(6.dp))
                            
                            Text(
                                text = if (isLoading) "Đang suy nghĩ..." else "Sẵn sàng giúp bạn",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isLoading) ChatColors.Typing else ChatColors.Online
                            )
                        }
                    }
                }
            },
            actions = {
                // Clear chat button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = rememberRipple(bounded = true),
                            onClick = onClearChat
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Xóa cuộc trò chuyện",
                        tint = ChatColors.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ChatColors.Surface
            )
        )
    }
}

/**
 * Empty state when no messages
 */
@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_ai),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            alpha = 0.6f
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Xin chào! Tôi là Tiramisu AI",
            style = MaterialTheme.typography.titleLarge,
            color = ChatColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tôi có thể giúp bạn lên lịch, quản lý công việc\nvà đưa ra lời khuyên về quản lý thời gian.",
            style = MaterialTheme.typography.bodyMedium,
            color = ChatColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Quick suggestions
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Thử hỏi:",
                style = MaterialTheme.typography.labelSmall,
                color = ChatColors.TextMuted
            )
            
            SuggestionChip(text = "Lên lịch học bài cho tôi")
            SuggestionChip(text = "Gợi ý thời gian nghỉ ngơi")
            SuggestionChip(text = "Tôi cần làm gì hôm nay?")
        }
    }
}

/**
 * Suggestion chip for empty state
 */
@Composable
private fun SuggestionChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = ChatColors.SurfaceVariant,
        modifier = modifier
    ) {
        Text(
            text = "\"$text\"",
            style = MaterialTheme.typography.bodySmall,
            color = ChatColors.Accent,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
