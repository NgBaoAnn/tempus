package com.projectapp.tempus.ui.ai.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.projectapp.tempus.ui.theme.TempusDesignSystem
import com.projectapp.tempus.ui.components.TempusCard
import com.projectapp.tempus.R
import com.projectapp.tempus.domain.model.ChatMode
import com.projectapp.tempus.domain.model.AgentState
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
    
    Scaffold(
        topBar = {
            PremiumChatHeader(
                isLoading = isLoading,
                chatMode = chatMode,
                onClearChat = { viewModel.clearChat() }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
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
        containerColor = MaterialTheme.colorScheme.background,
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
                    PremiumEmptyState(
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
            color = MaterialTheme.colorScheme.tertiary
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
            color = TempusDesignSystem.Success
        )
    }
}

/**
 * Premium Chat Header with gradient and glow effects
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumChatHeader(
    isLoading: Boolean,
    chatMode: ChatMode,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Animated status glow
    val infiniteTransition = rememberInfiniteTransition(label = "statusGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AI Avatar with glow
            Box {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .blur(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = glowAlpha * 0.5f))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .blur(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = if (isLoading) listOf(
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.secondaryContainer
                                ) else listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_ai),
                        contentDescription = "AI Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .clip(CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tiramisu AI",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    // Premium Mode badge
                    PremiumModeIndicator(mode = chatMode)
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Animated status dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isLoading) MaterialTheme.colorScheme.secondary.copy(alpha = glowAlpha)
                                else TempusDesignSystem.Success
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = if (isLoading) "Đang xử lý..." else "Sẵn sàng hỗ trợ",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isLoading) MaterialTheme.colorScheme.secondary else TempusDesignSystem.Success
                    )
                }
            }
            
            // Clear button with glass effect
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha=0.2f),
                        shape = CircleShape
                    )
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Premium mode indicator badge
 */
@Composable
private fun PremiumModeIndicator(
    mode: ChatMode,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (mode) {
        ChatMode.ASK -> "Ask" to MaterialTheme.colorScheme.tertiary
        ChatMode.AGENT -> "Agent" to MaterialTheme.colorScheme.primary
        ChatMode.LIFE_PLANNER -> "Planner" to TempusDesignSystem.Success
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

/**
 * Premium Empty state with gradient and suggestions
 */
@Composable
private fun PremiumEmptyState(
    chatMode: ChatMode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing AI avatar
        Box {
            // Glow
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .blur(24.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ai),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp)
                        .clip(CircleShape)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        // Mode title
        val (title, icon) = when (chatMode) {
            ChatMode.ASK -> "Chế độ Ask" to "💬"
            ChatMode.AGENT -> "Chế độ Agent" to "🤖"
            ChatMode.LIFE_PLANNER -> "Life Planner" to "🎯"
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = when (chatMode) {
                ChatMode.ASK -> "Hỏi đáp, tư vấn về quản lý thời gian.\nKhông thực hiện hành động."
                ChatMode.AGENT -> "Yêu cầu tạo, sửa, xóa lịch.\nXem trước và xác nhận trước khi thực hiện."
                ChatMode.LIFE_PLANNER -> "Lên kế hoạch dài hạn cho mục tiêu của bạn.\nAI sẽ tạo milestones và lịch học/làm việc."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Quick suggestions
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "THỬ HỎI",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.5.sp
            )
            
            when (chatMode) {
                ChatMode.ASK -> {
                    PremiumSuggestionChip(text = "Làm sao để quản lý thời gian tốt hơn?")
                    PremiumSuggestionChip(text = "Kỹ thuật Pomodoro là gì?")
                }
                ChatMode.AGENT -> {
                    PremiumSuggestionChip(text = "Lên lịch học bài cho tôi")
                    PremiumSuggestionChip(text = "Tạo lịch làm việc từ 8h-17h")
                }
                ChatMode.LIFE_PLANNER -> {
                    PremiumSuggestionChip(text = "Tôi muốn học IELTS 7.0 trong 3 tháng")
                    PremiumSuggestionChip(text = "Chuẩn bị thi cuối kỳ 5 môn trong 2 tuần")
                }
            }
        }
    }
}

/**
 * Premium suggestion chip with glass effect
 */
@Composable
private fun PremiumSuggestionChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha=0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = "\"$text\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
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
            color = MaterialTheme.colorScheme.tertiary
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
            color = TempusDesignSystem.Success
        )
    }
}
