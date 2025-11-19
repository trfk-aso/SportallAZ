package com.sportall.az.ui.history

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportall.az.domain.usecases.DrillUsageStats
import com.sportall.az.domain.usecases.TimeFilter
import com.sportall.az.models.Category
import org.koin.compose.koinInject

@Composable
fun StatisticsScreen() {
    val viewModel: HistoryViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    Scaffold(
        topBar = {
            StatisticsTopBar()
        }
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
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.statistics != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Time Filter Chips
                        TimeFilterChips(
                            selectedFilter = state.selectedFilter,
                            onFilterSelected = { viewModel.setTimeFilter(it) }
                        )

                        // Summary Cards
                        SummaryCards(
                            totalDrills = state.statistics!!.total,
                            avgRating = state.statistics!!.avgStars
                        )

                        // Category Breakdown
                        CategoryBreakdown(
                            categoryStats = state.statistics!!.byCategory
                        )

                        // Most Used Drills
                        MostUsedDrills(
                            drills = state.statistics!!.mostUsed
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    )
}

@Composable
fun TimeFilterChips(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        FilterChip(
            selected = selectedFilter == TimeFilter.ALL_TIME,
            onClick = { onFilterSelected(TimeFilter.ALL_TIME) },
            label = { Text("All time") }
        )
        FilterChip(
            selected = selectedFilter == TimeFilter.SEVEN_DAYS,
            onClick = { onFilterSelected(TimeFilter.SEVEN_DAYS) },
            label = { Text("7 days") }
        )
        FilterChip(
            selected = selectedFilter == TimeFilter.THIRTY_DAYS,
            onClick = { onFilterSelected(TimeFilter.THIRTY_DAYS) },
            label = { Text("30 days") }
        )
    }
}

@Composable
fun SummaryCards(
    totalDrills: Int,
    avgRating: Double?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total Drills Done Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total drills done",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = totalDrills.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Avg Rating Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Avg rating ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    repeat(3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (avgRating != null) String.format("%.1f / 3", avgRating) else "N/A",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun CategoryBreakdown(categoryStats: Map<Category, Int>) {
    if (categoryStats.isEmpty()) return

    val maxCount = categoryStats.values.maxOrNull() ?: 1

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categoryStats.toList().sortedBy { it.first.displayName }.forEach { (category, count) ->
            CategoryBar(
                categoryName = category.displayName,
                count = count,
                progress = count.toFloat() / maxCount.toFloat()
            )
        }
    }
}

@Composable
fun CategoryBar(
    categoryName: String,
    count: Int,
    progress: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = categoryName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(100.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .background(
                    color = Color(0xFFB4FF39), // Lime green
                    shape = RoundedCornerShape(4.dp)
                )
                .fillMaxWidth(progress)
        )

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(30.dp)
                .padding(start = 8.dp)
        )
    }
}

@Composable
fun MostUsedDrills(drills: List<DrillUsageStats>) {
    if (drills.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Most Used Drills",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        drills.take(4).forEachIndexed { index, drill ->
            DrillUsageRow(
                position = index + 1,
                drill = drill
            )
        }
    }
}

@Composable
fun DrillUsageRow(
    position: Int,
    drill: DrillUsageStats
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "$position. ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = drill.drillName,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stars based on avg rating
            val avgRating = drill.avgRating ?: 0.0
            val fullStars = avgRating.toInt().coerceIn(0, 3)
            repeat(fullStars) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = drill.usageCount.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}
