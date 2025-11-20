package com.sportall.az.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.sportall.az.models.Drill
import org.koin.compose.koinInject

data class PracticeScreen(val drill: Drill) : Screen {

    @Composable
    override fun Content() {
        val viewModel: PracticeViewModel = koinInject()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val scrollState = rememberScrollState()

        LaunchedEffect(drill.id) {
            viewModel.initialize(drill.id)
        }

        Scaffold(
            topBar = {
                PracticeTopBar(
                    drillName = drill.name,
                    onBackClick = { navigator.pop() }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        DurationSelection(
                            selectedDuration = state.selectedDurationMinutes,
                            onDurationSelected = { viewModel.selectDuration(it) },
                            enabled = state.timerState == TimerState.IDLE
                        )

                        TimerDisplay(
                            remainingSeconds = state.remainingSeconds,
                            timerState = state.timerState,
                            onStartClick = { viewModel.startTimer() },
                            onPauseClick = { viewModel.pauseTimer() },
                            onResumeClick = { viewModel.resumeTimer() }
                        )

                        ShortSteps(drill = drill)
                    }

                    Button(
                        onClick = { viewModel.completePractice() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp, top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB4FF39)
                        )
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                if (state.showRatingDialog) {
                    RatingDialog(
                        onSave = { rating ->
                            viewModel.saveToHistory(rating)
                            navigator.pop()
                        },
                        onCancel = { viewModel.cancelRating() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeTopBar(
    drillName: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = drillName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

@Composable
fun DurationSelection(
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Select duration:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(5, 10, 15).forEach { duration ->
                FilterChip(
                    selected = selectedDuration == duration,
                    onClick = { if (enabled) onDurationSelected(duration) },
                    label = { Text("$duration min") },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
fun TimerDisplay(
    remainingSeconds: Int,
    timerState: TimerState,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Timer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = formatTime(remainingSeconds),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )

        when (timerState) {
            TimerState.IDLE -> {
                Button(
                    onClick = onStartClick,
                    modifier = Modifier.fillMaxWidth(0.6f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB4FF39)
                    )
                ) {
                    Text(
                        text = "Start timer",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            TimerState.RUNNING -> {
                Button(
                    onClick = onPauseClick,
                    modifier = Modifier.fillMaxWidth(0.6f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pause")
                }
            }
            TimerState.PAUSED -> {
                Button(
                    onClick = onResumeClick,
                    modifier = Modifier.fillMaxWidth(0.6f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB4FF39)
                    )
                ) {
                    Text(
                        text = "Resume",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            TimerState.COMPLETED -> {
                Text(
                    text = "Time's up!",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFB4FF39)
                )
            }
        }
    }
}

@Composable
fun ShortSteps(drill: Drill) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Short Steps",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        val shortSteps = (drill.setup + drill.execution).take(4)

        shortSteps.forEach { step ->
            Row(
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "• ",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun RatingDialog(
    onSave: (Int?) -> Unit,
    onCancel: () -> Unit
) {
    var selectedRating by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        title = {
            Text(
                text = "Rate this drill",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "How well did you complete it?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (1..3).forEach { rating ->
                        Icon(
                            imageVector = if (selectedRating != null && rating <= selectedRating!!) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.StarBorder
                            },
                            contentDescription = "Rating $rating",
                            tint = if (selectedRating != null && rating <= selectedRating!!) {
                                Color(0xFFFFD700)
                            } else {
                                Color.Gray
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    selectedRating = if (selectedRating == rating) null else rating
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedRating) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB4FF39)
                )
            ) {
                Text(
                    text = "Save to History",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    val minutesStr = if (minutes < 10) "0$minutes" else "$minutes"
    val secsStr = if (secs < 10) "0$secs" else "$secs"
    return "$minutesStr:$secsStr"
}
