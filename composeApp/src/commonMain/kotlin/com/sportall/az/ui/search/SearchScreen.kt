package com.sportall.az.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.sportall.az.models.Category
import com.sportall.az.models.Difficulty
import com.sportall.az.ui.catalog.DrillDetailsScreen
import com.sportall.az.ui.home.DrillsGrid
import com.sportall.az.ui.paywall.PaywallScreen
import com.sportall.az.ui.paywall.PaywallType
import org.koin.compose.koinInject

@Composable
fun SearchScreen() {
    val viewModel: SearchViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Bar
        SearchTextField(
            query = state.query,
            onQueryChange = { viewModel.onQueryChange(it) },
            onSearch = { viewModel.submitSearch() },
            onClear = { viewModel.clearSearch() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Filters
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            CategoryFilters(
                selectedCategory = state.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) },
                onExclusiveClick = { navigator.push(PaywallScreen(PaywallType.EXCLUSIVE)) }
            )

            // Difficulty Filters
            Text(
                text = "Difficulty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            DifficultyFilters(
                selectedDifficulty = state.selectedDifficulty,
                onDifficultySelected = { viewModel.selectDifficulty(it) },
                onExclusiveClick = { navigator.push(PaywallScreen(PaywallType.EXCLUSIVE)) }
            )

            // Recent Queries
            if (state.history.isNotEmpty() && state.query.isEmpty()) {
                Text(
                    text = "Recent queries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                state.history.forEach { query ->
                    RecentQueryItem(
                        query = query,
                        onQueryClick = { viewModel.selectHistoryQuery(query) },
                        onRemoveClick = { viewModel.removeHistoryItem(query) }
                    )
                }
            }

            // Results or Empty State
            when {
                state.loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.results.isEmpty() && (state.query.isNotEmpty() || state.selectedCategory != null || state.selectedDifficulty != null) -> {
                    EmptySearchState(
                        onBackToAll = { viewModel.backToAllDrills() }
                    )
                }
                state.results.isNotEmpty() -> {
                    DrillsGrid(
                        drills = state.results,
                        favorites = state.favorites,
                        onDrillClick = { drill ->
                            if (drill.isExclusive && !state.isExclusiveUnlocked) {
                                navigator.push(PaywallScreen(PaywallType.EXCLUSIVE))
                            } else {
                                navigator.push(DrillDetailsScreen(drill.id))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))

            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Search drills...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryFilters(
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
    onExclusiveClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // First row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") }
            )

            FilterChip(
                selected = selectedCategory == Category.WarmUp,
                onClick = {
                    onCategorySelected(if (selectedCategory == Category.WarmUp) null else Category.WarmUp)
                },
                label = { Text("Warm-up") }
            )

            FilterChip(
                selected = selectedCategory == Category.Passing,
                onClick = {
                    onCategorySelected(if (selectedCategory == Category.Passing) null else Category.Passing)
                },
                label = { Text("Passing") }
            )

            FilterChip(
                selected = selectedCategory == Category.Dribbling,
                onClick = {
                    onCategorySelected(if (selectedCategory == Category.Dribbling) null else Category.Dribbling)
                },
                label = { Text("Dribbling") }
            )
        }

        // Second row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedCategory == Category.Shooting,
                onClick = {
                    onCategorySelected(if (selectedCategory == Category.Shooting) null else Category.Shooting)
                },
                label = { Text("Shooting") }
            )

            FilterChip(
                selected = selectedCategory == Category.Rondo,
                onClick = {
                    onCategorySelected(if (selectedCategory == Category.Rondo) null else Category.Rondo)
                },
                label = { Text("Rondo / Possession") }
            )

            FilterChip(
                selected = selectedCategory == Category.Agility,
                onClick = {
                    onCategorySelected(if (selectedCategory == Category.Agility) null else Category.Agility)
                },
                label = { Text("Agility") }
            )
        }

        // Third row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedCategory == Category.Goalkeeper,
                onClick = {
                    onCategorySelected(if (selectedCategory == Category.Goalkeeper) null else Category.Goalkeeper)
                },
                label = { Text("Goalkeeper") }
            )

            FilterChip(
                selected = selectedCategory == Category.Recovery,
                onClick = {
                    onCategorySelected(if (selectedCategory == Category.Recovery) null else Category.Recovery)
                },
                label = { Text("Recovery") }
            )

            // Exclusive chip with lock icon
            FilterChip(
                selected = false,
                onClick = onExclusiveClick,
                label = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Exclusive")
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun DifficultyFilters(
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty?) -> Unit,
    onExclusiveClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedDifficulty == Difficulty.Easy,
            onClick = {
                onDifficultySelected(if (selectedDifficulty == Difficulty.Easy) null else Difficulty.Easy)
            },
            label = { Text("Easy") }
        )

        FilterChip(
            selected = selectedDifficulty == Difficulty.Medium,
            onClick = {
                onDifficultySelected(if (selectedDifficulty == Difficulty.Medium) null else Difficulty.Medium)
            },
            label = { Text("Medium") }
        )

        FilterChip(
            selected = selectedDifficulty == Difficulty.Hard,
            onClick = {
                onDifficultySelected(if (selectedDifficulty == Difficulty.Hard) null else Difficulty.Hard)
            },
            label = { Text("Hard") }
        )

        FilterChip(
            selected = false,
            onClick = onExclusiveClick,
            label = { Text("Exclusive") }
        )
    }
}

@Composable
fun RecentQueryItem(
    query: String,
    onQueryClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onQueryClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = query,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptySearchState(
    onBackToAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        Text(
            text = "No drills found.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Button(
            onClick = onBackToAll,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB4FF39)
            )
        ) {
            Text(
                text = "Back to all drills",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
