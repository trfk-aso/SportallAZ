package com.sportall.az.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.sportall.az.generated.resources.Res
import com.sportall.az.generated.resources.bg_dark
import com.sportall.az.generated.resources.ic_skip
import com.sportall.az.generated.resources.onb_page_0
import com.sportall.az.generated.resources.onb_page_1
import com.sportall.az.generated.resources.onb_page_2
import com.sportall.az.ui.MainTabsScreen
import com.sportall.az.ui.theme.DeepBlue
import com.sportall.az.ui.theme.LimeGreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

data object OnboardingScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel: OnboardingViewModel = koinInject()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val pagerState = rememberPagerState(pageCount = { 3 })
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(state.isCompleted) {
            if (state.isCompleted) {
                navigator.replace(MainTabsScreen)
            }
        }

        LaunchedEffect(state.currentPage) {
            if (pagerState.currentPage != state.currentPage) {
                pagerState.animateScrollToPage(state.currentPage)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(Res.drawable.bg_dark),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 55.dp, end = 25.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.ic_skip),
                            contentDescription = "Skip",
                            modifier = Modifier
                                .size(70.dp)
                                .clickable { viewModel.skipOnboarding() }
                        )
                    }
                },
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1}/3",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                if (pagerState.currentPage < 2) {
                                    coroutineScope.launch {
                                        viewModel.nextPage()
                                    }
                                } else {
                                    viewModel.skipOnboarding()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LimeGreen,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = if (pagerState.currentPage < 2) "Next" else "Get started",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            ) { paddingValues ->

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) { page ->
                    OnboardingPage(page = page)
                }

            }
        }
    }
}

@Composable
fun OnboardingPage(page: Int) {
    val content = when (page) {
        0 -> OnboardingPageContent(Res.drawable.onb_page_0) 
        1 -> OnboardingPageContent(Res.drawable.onb_page_1)
        2 -> OnboardingPageContent(Res.drawable.onb_page_2)
        else -> OnboardingPageContent(Res.drawable.onb_page_0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(content.image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        )
    }
}

data class OnboardingPageContent(
    val image: DrawableResource
)
