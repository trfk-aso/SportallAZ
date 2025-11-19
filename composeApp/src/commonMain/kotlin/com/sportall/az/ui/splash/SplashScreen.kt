package com.sportall.az.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.sportall.az.ui.MainTabsScreen
import com.sportall.az.ui.onboarding.OnboardingScreen
import com.sportall.az.ui.theme.DeepBlue
import com.sportall.az.ui.theme.LimeGreen
import org.koin.compose.koinInject

data object SplashScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel: SplashViewModel = koinInject()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(state.shouldShowOnboarding) {
            when (state.shouldShowOnboarding) {
                true -> navigator.replace(OnboardingScreen)
                false -> navigator.replace(MainTabsScreen)
                null -> { /* Still loading */ }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepBlue),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SPORTALL AZ",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "50 offline football drills",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LimeGreen
                )
            }
        }
    }
}
