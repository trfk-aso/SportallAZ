package com.sportall.az.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.sportall.az.ui.MainTabsScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data object OnboardingScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel: OnboardingViewModel = koinInject()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val pagerState = rememberPagerState(pageCount = { 3 })
        val coroutineScope = rememberCoroutineScope()

        // Navigate when onboarding is completed
        LaunchedEffect(state.isCompleted) {
            if (state.isCompleted) {
                navigator.replace(MainTabsScreen)
            }
        }

        // Sync pager state with ViewModel state
        LaunchedEffect(state.currentPage) {
            if (pagerState.currentPage != state.currentPage) {
                pagerState.animateScrollToPage(state.currentPage)
            }
        }

        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    TextButton(onClick = { viewModel.skipOnboarding() }) {
                        Text("Skip")
                    }
                }
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Page indicator
                    Text(
                        text = "${pagerState.currentPage + 1}/3",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Next / Get Started button
                    Button(
                        onClick = {
                            if (pagerState.currentPage < 2) {
                                coroutineScope.launch {
                                    viewModel.nextPage()
                                }
                            } else {
                                viewModel.skipOnboarding() // "Get started" = finish onboarding
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (pagerState.currentPage < 2) "Next" else "Get started")
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

@Composable
fun OnboardingPage(page: Int) {
    val content = when (page) {
        0 -> OnboardingPageContent(
            title = "50 ready-to-use drills",
            description = "Warm-up, passing, rondo, shooting — no internet required."
        )
        1 -> OnboardingPageContent(
            title = "Visual cards first",
            description = "See the drill layout on the card, details inside."
        )
        2 -> OnboardingPageContent(
            title = "Track what you did",
            description = "Mark drills as done and rate 1–3 stars."
        )
        else -> OnboardingPageContent("", "")
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Text(
                text = content.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = content.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class OnboardingPageContent(
    val title: String,
    val description: String
)
