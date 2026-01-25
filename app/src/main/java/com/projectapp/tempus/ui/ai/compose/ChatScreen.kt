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
// Note: Using simple clickable without explicit indication for compatibility
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
import com.projectapp.tempus.domain.model.AgentState
import com.projectapp.tempus.domain.model.ChatMode
import com.projectapp.tempus.domain.model.LifePlanState
import com.projectapp.tempus.ui.ai.AIViewModel

/**
 * Main Chat Screen composable with Ask/Agent modes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: AIViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val chatMode by viewModel.chatMode.observeAsState(ChatMode.ASK)
    val agentState by viewModel.agentState.observeAsState(AgentState.Idle)
    val lifePlanState by viewModel.lifePlanState.observeAsState(LifePlanState.Idle)
    
    // Legacy support
    val suggestions by viewModel.suggestions.observeAsState(emptyList())
    val showSuggestionSheet by viewModel.showSuggestionSheet.observeAsState(false)
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size, isLoading, agentState, lifePlanState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    ChatTheme {
        Scaffold(
            topBar = {
                ChatTopBar(
                    isLoading = isLoading,
                    chatMode = chatMode,
                    onClearChat = { viewModel.clearChat() }
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ChatColors.Surface)
                ) {
                    // Mode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ModeToggle(
                            currentMode = chatMode,
                            onModeChange = { viewModel.setMode(it) },
                            enabled = agentState is AgentState.Idle && !isLoading
                        )
                    }
                    
                    // Chat Input
                    ChatInput(
                        value = inputText,
                        onValueChange = { inputText = it },
                        onSend = {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        },
                        enabled = !isLoading && agentState !is AgentState.AwaitingAccept && lifePlanState !is LifePlanState.AwaitingApproval,
                        placeholder = when (chatMode) {
                            ChatMode.ASK -> "Hỏi điều gì đó..."
                            ChatMode.AGENT -> "Yêu cầu một hành động..."
                            ChatMode.LIFE_PLANNER -> "Chia sẻ mục tiêu của bạn..."
                        },
                        modifier = Modifier.imePadding()
                    )
                }
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
                        chatMode = chatMode,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Messages list with Proposal Card
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
                        if (isLoading && agentState !is AgentState.Proposing) {
                            item(key = "typing") {
                                TypingIndicator(visible = true)
                            }
                        }
                        
                        // Proposing indicator
                        if (agentState is AgentState.Proposing) {
                            item(key = "proposing") {
                                ProposingIndicator()
                            }
                        }
                        
                        // Proposal Card (AwaitingAccept state)
                        if (agentState is AgentState.AwaitingAccept) {
                            item(key = "proposal") {
                                val proposal = (agentState as AgentState.AwaitingAccept).proposal
                                ProposalCard(
                                    proposal = proposal,
                                    onAccept = { viewModel.acceptProposal() },
                                    onCancel = { viewModel.cancelProposal() },
                                    isExecuting = false
                                )
                            }
                        }
                        
                        // Executing state
                        if (agentState is AgentState.Executing) {
                            item(key = "executing") {
                                ExecutingIndicator()
                            }
                        }
                        
                        // Execution result (Done state)
                        if (agentState is AgentState.Done) {
                            item(key = "result") {
                                val result = (agentState as AgentState.Done).result
                                ExecutionFeedback(result = result)
                            }
                        }
                        
                        // Life Planner: Analyzing indicator
                        if (lifePlanState is LifePlanState.Analyzing) {
                            item(key = "lifePlanAnalyzing") {
                                LifePlanAnalyzingIndicator()
                            }
                        }
                        
                        // Life Planner: Preview Card
                        if (lifePlanState is LifePlanState.AwaitingApproval) {
                            item(key = "lifePlanPreview") {
                                val proposal = (lifePlanState as LifePlanState.AwaitingApproval).proposal
                                LifePlanPreviewCard(
                                    proposal = proposal,
                                    onAccept = { viewModel.acceptLifePlan() },
                                    onReject = { viewModel.rejectLifePlan() },
                                    isLoading = false,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        
                        // Life Planner: Creating indicator
                        if (lifePlanState is LifePlanState.Creating) {
                            item(key = "lifePlanCreating") {
                                LifePlanCreatingIndicator()
                            }
                        }
                    }
                }
            }
        }
        
        // Legacy Schedule Suggestion Sheet
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
 * Proposing indicator for Agent Mode
 */
@Composable
private fun ProposingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🤖", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Đang phân tích và tạo đề xuất...",
            style = MaterialTheme.typography.bodyMedium,
            color = ChatColors.Accent
        )
    }
}

/**
 * Executing indicator for Agent Mode
 */
@Composable
private fun ExecutingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "⚡", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Đang thực hiện các thay đổi...",
            style = MaterialTheme.typography.bodyMedium,
            color = ChatColors.Online
        )
    }
}

/**
 * Chat top app bar with avatar and status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    isLoading: Boolean,
    chatMode: ChatMode,
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tiramisu AI",
                                style = MaterialTheme.typography.titleLarge,
                                color = ChatColors.TextPrimary
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Mode badge
                            ModeIndicator(mode = chatMode)
                        }
                        
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
                                text = if (isLoading) "Đang xử lý..." else "Sẵn sàng",
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
                            indication = null,
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
    chatMode: ChatMode,
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
            text = when (chatMode) {
                ChatMode.ASK -> "Chế độ Ask 💬"
                ChatMode.AGENT -> "Chế độ Agent 🤖"
                ChatMode.LIFE_PLANNER -> "Life Planner 🎯"
            },
            style = MaterialTheme.typography.titleLarge,
            color = ChatColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = when (chatMode) {
                ChatMode.ASK -> "Hỏi đáp, tư vấn về quản lý thời gian.\nKhông thực hiện hành động."
                ChatMode.AGENT -> "Yêu cầu tạo, sửa, xóa lịch.\nXem trước và xác nhận trước khi thực hiện."
                ChatMode.LIFE_PLANNER -> "Lên kế hoạch dài hạn cho mục tiêu của bạn.\nAI sẽ tạo milestones và lịch học/làm việc."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = ChatColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Quick suggestions based on mode
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Thử hỏi:",
                style = MaterialTheme.typography.labelSmall,
                color = ChatColors.TextMuted
            )
            
            when (chatMode) {
                ChatMode.ASK -> {
                    SuggestionChip(text = "Làm sao để quản lý thời gian tốt hơn?")
                    SuggestionChip(text = "Kỹ thuật Pomodoro là gì?")
                }
                ChatMode.AGENT -> {
                    SuggestionChip(text = "Lên lịch học bài cho tôi")
                    SuggestionChip(text = "Tạo lịch làm việc từ 8h-17h")
                }
                ChatMode.LIFE_PLANNER -> {
                    SuggestionChip(text = "Tôi muốn học IELTS 7.0 trong 3 tháng")
                    SuggestionChip(text = "Chuẩn bị thi cuối kỳ 5 môn trong 2 tuần")
                }
            }
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

/**
 * Analyzing indicator for Life Planner Mode
 */
@Composable
private fun LifePlanAnalyzingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🎯", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Đang phân tích mục tiêu và tạo kế hoạch...",
            style = MaterialTheme.typography.bodyMedium,
            color = ChatColors.Accent
        )
    }
}

/**
 * Creating indicator for Life Planner Mode
 */
@Composable
private fun LifePlanCreatingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "📅", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Đang tạo lịch từ kế hoạch...",
            style = MaterialTheme.typography.bodyMedium,
            color = ChatColors.Online
        )
    }
}
