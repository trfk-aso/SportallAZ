package com.sportall.az.ui.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.sportall.az.ui.catalog.DrillDetailsScreen
import com.sportall.az.ui.paywall.PaywallScreen
import com.sportall.az.ui.paywall.PaywallType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

data object HistoryScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel: HistoryViewModel = koinInject()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            viewModel.load()
        }

        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(Res.drawable.bg_dark),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Scaffold(
                topBar = {
                    HistoryTopBar(
                        onStatsClick = { navigator.push(com.sportall.az.ui.history.StatisticsScreen) }
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when {
                        state.loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        state.error != null -> {
                            ErrorState(
                                message = state.error ?: "Unknown error",
                                onRetry = { viewModel.load() },
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        state.groupedHistory.today.isEmpty() &&
                                state.groupedHistory.yesterday.isEmpty() &&
                                state.groupedHistory.earlier.isEmpty() -> {
                            EmptyHistoryState(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        else -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 16.dp,
                                        bottom = 96.dp
                                    ),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                if (state.groupedHistory.today.isNotEmpty()) {
                                    HistorySection(
                                        title = "Today",
                                        items = state.groupedHistory.today,
                                        onItemClick = { item ->
                                            item.drill?.let {
                                                navigator.push(DrillDetailsScreen(it.id))
                                            }
                                        }
                                    )
                                }

                                if (state.groupedHistory.yesterday.isNotEmpty()) {
                                    HistorySection(
                                        title = "Yesterday",
                                        items = state.groupedHistory.yesterday,
                                        onItemClick = { item ->
                                            item.drill?.let {
                                                navigator.push(DrillDetailsScreen(it.id))
                                            }
                                        }
                                    )
                                }

                                if (state.groupedHistory.earlier.isNotEmpty()) {
                                    HistorySection(
                                        title = "Earlier",
                                        items = state.groupedHistory.earlier,
                                        onItemClick = { item ->
                                            item.drill?.let {
                                                navigator.push(DrillDetailsScreen(it.id))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTopBar(onStatsClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        actions = {
            IconButton(onClick = onStatsClick) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "Statistics",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun HistorySection(
    title: String,
    items: List<HistoryItemWithDrill>,
    onItemClick: (HistoryItemWithDrill) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        items.forEach { item ->
            HistoryItemCard(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
fun HistoryItemCard(
    item: HistoryItemWithDrill,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.sportall.az.ui.theme.SurfaceBlue
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.drill?.name ?: "Unknown drill",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.drill?.let { drill ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = drill.category.displayName,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(24.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = com.sportall.az.ui.theme.DrillCardBlue,
                                labelColor = Color.White
                            )
                        )
                    }

                    Text(
                        text = formatTime(item.record.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val stars = item.record.stars ?: 0
                if (stars > 0) {
                    repeat(3) { index ->
                        Icon(
                            imageVector = if (index < stars) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (index < stars) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Unrated",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No completed drills yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start practicing and your progress will appear here!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

fun formatTime(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}
