package com.sportall.az.ui.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.sportall.az.domain.usecases.DrillUsageStats
import com.sportall.az.domain.usecases.TimeFilter
import com.sportall.az.generated.resources.Res
import com.sportall.az.generated.resources.bg_dark
import com.sportall.az.models.Category
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

data object StatisticsScreen : Screen {
    @Composable
    override fun Content() {
        StatisticsScreenContent()
    }
}

@Composable
fun StatisticsScreenContent() {
    val viewModel: HistoryViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val navigator = LocalNavigator.currentOrThrow

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
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
                StatisticsTopBar(
                    onBackClick = { navigator.pop() }
                )
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
                            TimeFilterChips(
                                selectedFilter = state.selectedFilter,
                                onFilterSelected = { viewModel.setTimeFilter(it) }
                            )

                            SummaryCards(
                                totalDrills = state.statistics!!.total,
                                avgRating = state.statistics!!.avgStars
                            )

                            CategoryBreakdown(
                                categoryStats = state.statistics!!.byCategory
                            )

                            MostUsedDrills(
                                drills = state.statistics!!.mostUsed
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
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
            label = { Text("All time") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = com.sportall.az.ui.theme.LimeGreen,
                selectedLabelColor = Color.Black,
                containerColor = Color.Transparent,
                labelColor = Color.White
            )
        )
        FilterChip(
            selected = selectedFilter == TimeFilter.SEVEN_DAYS,
            onClick = { onFilterSelected(TimeFilter.SEVEN_DAYS) },
            label = { Text("7 days") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = com.sportall.az.ui.theme.LimeGreen,
                selectedLabelColor = Color.Black,
                containerColor = Color.Transparent,
                labelColor = Color.White
            )
        )
        FilterChip(
            selected = selectedFilter == TimeFilter.THIRTY_DAYS,
            onClick = { onFilterSelected(TimeFilter.THIRTY_DAYS) },
            label = { Text("30 days") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = com.sportall.az.ui.theme.LimeGreen,
                selectedLabelColor = Color.Black,
                containerColor = Color.Transparent,
                labelColor = Color.White
            )
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
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = com.sportall.az.ui.theme.SurfaceBlue
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
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = totalDrills.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = com.sportall.az.ui.theme.SurfaceBlue
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
                        color = Color.White
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
                    text = if (avgRating != null) {
                        val rounded = (avgRating * 10).toInt() / 10.0
                        "$rounded / 3"
                    } else "N/A",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
            modifier = Modifier.width(100.dp),
            color = Color.White
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
                .padding(start = 8.dp),
            color = Color.White
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
            fontWeight = FontWeight.Bold,
            color = Color.White
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
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Text(
                text = drill.drillName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                fontWeight = FontWeight.Bold,
                color = Color.White
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
