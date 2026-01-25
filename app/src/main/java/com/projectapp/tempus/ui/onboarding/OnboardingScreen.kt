package com.projectapp.tempus.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Premium Light Onboarding Screen
 * 
 * Fixed layout:
 * - Content stays in upper portion
 * - Buttons are at bottom, separate from content
 * - No overlap between text and buttons
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPageData(
            icon = Icons.Rounded.CheckCircle,
            title = "Quản lý công việc\nthông minh",
            description = "Tạo, tổ chức và theo dõi tất cả nhiệm vụ hàng ngày với giao diện trực quan và đơn giản.",
            gradientColors = OnboardingColors.Page1Gradient
        ),
        OnboardingPageData(
            icon = Icons.Rounded.Notifications,
            title = "Không bao giờ\nbỏ lỡ deadline",
            description = "Hệ thống nhắc nhở thông minh giúp bạn luôn on-track với các mục tiêu quan trọng.",
            gradientColors = OnboardingColors.Page2Gradient
        ),
        OnboardingPageData(
            icon = Icons.Rounded.TrendingUp,
            title = "Theo dõi\ntiến độ",
            description = "Xem thống kê chi tiết về streak, điểm số và năng suất để luôn có động lực.",
            gradientColors = OnboardingColors.Page3Gradient
        ),
        OnboardingPageData(
            icon = Icons.Rounded.Spa,
            title = "Sẵn sàng\nbắt đầu?",
            description = "Biến năng suất thành thói quen với TEMPUS. Trồng cây, kiếm điểm và chinh phục mục tiêu!",
            gradientColors = OnboardingColors.Page4Gradient
        )
    )
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingColors.BackgroundGradient)
    ) {
        // === PAGER CONTENT (takes available space) ===
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)  // Takes remaining space
                .fillMaxWidth()
        ) { pageIndex ->
            OnboardingPage(
                pageData = pages[pageIndex],
                isVisible = pagerState.currentPage == pageIndex
            )
        }
        
        // === BOTTOM CONTROLS (fixed height) ===
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicator
            OnboardingPageIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Primary button
            OnboardingButton(
                text = if (isLastPage) "Bắt đầu ngay" else "Tiếp tục",
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                isPrimary = true
            )
            
            // Skip button - hidden on last page
            AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                OnboardingButton(
                    text = "Bỏ qua",
                    onClick = onFinish,
                    isPrimary = false
                )
            }
        }
    }
}
