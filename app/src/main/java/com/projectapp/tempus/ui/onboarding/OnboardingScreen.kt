package com.projectapp.tempus.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.ui.res.stringResource
import com.projectapp.tempus.R
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPageData(

            icon = Icons.Rounded.CheckCircle,
            title = stringResource(R.string.onboarding_title_1),
            description = stringResource(R.string.onboarding_desc_1),
            gradientColors = OnboardingColors.Page1Gradient
        ),
        OnboardingPageData(
            icon = Icons.Rounded.Notifications,
            title = stringResource(R.string.onboarding_title_2),
            description = stringResource(R.string.onboarding_desc_2),
            gradientColors = OnboardingColors.Page2Gradient
        ),
        OnboardingPageData(
            icon = Icons.Rounded.TrendingUp,
            title = stringResource(R.string.onboarding_title_3),
            description = stringResource(R.string.onboarding_desc_3),
            gradientColors = OnboardingColors.Page3Gradient
        ),
        OnboardingPageData(
            icon = Icons.Rounded.Spa,
            title = stringResource(R.string.onboarding_title_4),
            description = stringResource(R.string.onboarding_desc_4),
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
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)  
                .fillMaxWidth()
        ) { pageIndex ->
            OnboardingPage(
                pageData = pages[pageIndex],
                isVisible = pagerState.currentPage == pageIndex
            )
        }
        
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            OnboardingPageIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            
            OnboardingButton(
                text = if (isLastPage) stringResource(R.string.onboarding_start_now) else stringResource(R.string.onboarding_continue),
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
            
            
            AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                OnboardingButton(
                    text = stringResource(R.string.onboarding_skip),
                    onClick = onFinish,
                    isPrimary = false
                )
            }
        }
    }
}
