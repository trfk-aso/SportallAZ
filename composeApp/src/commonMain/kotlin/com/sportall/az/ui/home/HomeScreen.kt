package com.sportall.az.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.sportall.az.models.Category
import com.sportall.az.models.Drill
import com.sportall.az.ui.SearchTab
import com.sportall.az.ui.SettingsTab
import com.sportall.az.ui.catalog.DrillDetailsScreen
import com.sportall.az.ui.paywall.PaywallScreen
import com.sportall.az.ui.paywall.PaywallType
import com.sportall.az.ui.theme.DrillCardBlue
import com.sportall.az.ui.theme.Gold
import org.koin.compose.koinInject

@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val tabNavigator = LocalTabNavigator.current
    val navigator = LocalNavigator.currentOrThrow

    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                onSearchClick = { tabNavigator.current = SearchTab },
                onSettingsClick = { tabNavigator.current = SettingsTab }
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
                        onRetry = { viewModel.refresh() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state.stats != null && state.stats!!.mostUsed.isNotEmpty()) {
                            val lastDrillId = state.stats!!.mostUsed.first().drillId
                            val lastDrill = state.drills.firstOrNull { it.id == lastDrillId }
                            if (lastDrill != null) {
                                ContinueCard(
                                    drill = lastDrill,
                                    onClick = {
                                        if (lastDrill.isExclusive && !state.isExclusiveUnlocked) {
                                            navigator.push(PaywallScreen(PaywallType.EXCLUSIVE))
                                        } else {
                                            navigator.push(DrillDetailsScreen(lastDrill.id))
                                        }
                                    }
                                )
                            }
                        }

                        Text(
                            text = "Categories",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
                        )

                        CategoryChips(
                            categories = state.categories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { category ->
                                selectedCategory = category
                                viewModel.filterByCategory(category)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Featured / Popular drills",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
                        )

                        if (state.drills.isEmpty()) {
                            EmptyState(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            )
                        } else {
                            DrillsGrid(
                                drills = state.drills,
                                favorites = state.favorites,
                                onDrillClick = { drill ->
                                    if (drill.isExclusive && !state.isExclusiveUnlocked) {
                                        navigator.push(PaywallScreen(PaywallType.EXCLUSIVE))
                                    } else {
                                        navigator.push(DrillDetailsScreen(drill.id))
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
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
fun HomeTopBar(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPORTALL",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "AZ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = com.sportall.az.ui.theme.LimeGreen
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = com.sportall.az.ui.theme.DeepBlue
        )
    )
}

@Composable
fun ContinueCard(
    drill: Drill,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = drill.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
            }

            // Visual placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = DrillCardBlue,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = drill.visualDescription.take(15),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = com.sportall.az.ui.theme.LimeGreen
            )
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun CategoryChips(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = com.sportall.az.ui.theme.LimeGreen,
                    selectedLabelColor = Color.Black,
                    containerColor = Color.Transparent,
                    labelColor = Color.White
                )
            )
        }

        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName) },
                leadingIcon = if (category.displayName == "Exclusive") {
                    { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = com.sportall.az.ui.theme.LimeGreen,
                    selectedLabelColor = Color.Black,
                    containerColor = Color.Transparent,
                    labelColor = Color.White,
                    iconColor = if (category.displayName == "Exclusive") com.sportall.az.ui.theme.Gold else Color.White
                )
            )
        }
    }
}

@Composable
fun DrillsGrid(
    drills: List<Drill>,
    favorites: Set<Int>,
    onDrillClick: (Drill) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(drills) { drill ->
            DrillCard(
                drill = drill,
                isFavorite = drill.id in favorites,
                onClick = { onDrillClick(drill) }
            )
        }
    }
}

@Composable
fun DrillCard(
    drill: Drill,
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.sportall.az.ui.theme.SurfaceBlue
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        color = if (drill.isExclusive) {
                            Gold.copy(alpha = 0.4f)
                        } else {
                            DrillCardBlue
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = drill.visualDescription.take(40),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = if (drill.isExclusive) Color.Black else Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(12.dp)
                )

                if (drill.isExclusive) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Exclusive",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .size(18.dp),
                        tint = Color(0xFF8B7500)
                    )
                }

                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Gold else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .size(20.dp)
                )
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = drill.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 15.sp
                )

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = drill.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.height(26.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = com.sportall.az.ui.theme.DrillCardBlue,
                        labelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Start with warm-up drills.",
            style = MaterialTheme.typography.titleLarge,
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
        modifier = modifier,
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
