package com.sportall.az.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.sportall.az.generated.resources.Res
import com.sportall.az.generated.resources.bg_dark
import com.sportall.az.models.Category
import com.sportall.az.models.Difficulty
import com.sportall.az.ui.catalog.DrillDetailsScreen
import com.sportall.az.ui.home.DrillsGrid
import com.sportall.az.ui.paywall.PaywallScreen
import com.sportall.az.ui.paywall.PaywallType
import com.sportall.az.ui.theme.Gold
import com.sportall.az.ui.theme.LimeGreen
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@Composable
fun SearchScreen() {
    val viewModel: SearchViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    LaunchedEffect(navigator.lastEvent) {
        viewModel.reloadExclusiveState()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(Res.drawable.bg_dark),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {

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

                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
                CategoryFilters(
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onExclusiveClick = { navigator.push(PaywallScreen(PaywallType.EXCLUSIVE)) },
                    isExclusiveUnlocked = state.isExclusiveUnlocked
                )

                Text(
                    text = "Difficulty",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                DifficultyFilters(
                    selectedDifficulty = state.selectedDifficulty,
                    onDifficultySelected = { viewModel.selectDifficulty(it) },
                    onExclusiveClick = { navigator.push(PaywallScreen(PaywallType.EXCLUSIVE)) },
                    isExclusiveUnlocked = state.isExclusiveUnlocked
                )

                if (state.history.isNotEmpty() && state.query.isEmpty()) {
                    Text(
                        text = "Recent queries",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    state.history.forEach { query ->
                        RecentQueryItem(
                            query = query,
                            onQueryClick = { viewModel.selectHistoryQuery(query) },
                            onRemoveClick = { viewModel.removeHistoryItem(query) }
                        )
                    }
                }

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

                    state.results.isEmpty() &&
                            (state.query.isNotEmpty()
                                    || state.selectedCategory != null
                                    || state.selectedDifficulty != null) -> {

                        EmptySearchState(
                            onBackToAll = { viewModel.backToAllDrills() }
                        )
                    }

                    state.results.isNotEmpty() -> {
                        DrillsGrid(
                            drills = state.results,
                            favorites = state.favorites,
                            isExclusiveUnlocked = state.isExclusiveUnlocked,
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
            .padding(top = 65.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Search drills...",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() })
            )

            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.White
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
    onExclusiveClick: () -> Unit,
    isExclusiveUnlocked: Boolean
) {
    val categories = listOf(
        Category.Exclusive to "Exclusive",
        Category.WarmUp to "Warm-up",
        Category.Passing to "Passing",
        Category.Dribbling to "Dribbling",
        Category.Shooting to "Shooting",
        Category.Rondo to "Rondo / Possession",
        Category.Agility to "Agility",
        Category.Goalkeeper to "Goalkeeper",
        Category.Recovery to "Recovery"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {

        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All", color = Color.White) }
            )
        }

        items(categories) { (category, label) ->

            val isSelected = selectedCategory == category
            val isExclusive = category == Category.Exclusive
            val locked = isExclusive && !isExclusiveUnlocked

            if (isExclusive) {
                Box {

                    FilterChip(
                        selected = isSelected && !locked,
                        onClick = {
                            if (locked) onExclusiveClick()
                            else onCategorySelected(category)
                        },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {

                                Text(
                                    text = if (locked) "Buy" else label,
                                    color = Color.White
                                )

                                if (locked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp),
                                        tint = Gold
                                    )
                                }
                            }
                        }
                    )
                }
            } else {

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onCategorySelected(if (isSelected) null else category)
                    },
                    label = { Text(label, color = Color.White) }
                )
            }
        }
    }
}

@Composable
fun DifficultyFilters(
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty?) -> Unit,
    onExclusiveClick: () -> Unit,
    isExclusiveUnlocked: Boolean
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

        item {
            FilterChip(
                selected = selectedDifficulty == Difficulty.Easy,
                onClick = {
                    onDifficultySelected(
                        if (selectedDifficulty == Difficulty.Easy) null else Difficulty.Easy
                    )
                },
                label = { Text("Easy", color = Color.White) }
            )
        }

        item {
            FilterChip(
                selected = selectedDifficulty == Difficulty.Medium,
                onClick = {
                    onDifficultySelected(
                        if (selectedDifficulty == Difficulty.Medium) null else Difficulty.Medium
                    )
                },
                label = { Text("Medium", color = Color.White) }
            )
        }

        item {
            FilterChip(
                selected = selectedDifficulty == Difficulty.Hard,
                onClick = {
                    onDifficultySelected(
                        if (selectedDifficulty == Difficulty.Hard) null else Difficulty.Hard
                    )
                },
                label = { Text("Hard", color = Color.White) }
            )
        }

        item {

            val locked = !isExclusiveUnlocked

            Box(
                contentAlignment = Alignment.TopCenter
            ) {

                FilterChip(
                    selected = selectedDifficulty == Difficulty.Exclusive && !locked,
                    onClick = {
                        if (locked) {
                            onExclusiveClick()
                        } else {
                            onDifficultySelected(
                                if (selectedDifficulty == Difficulty.Exclusive) null else Difficulty.Exclusive
                            )
                        }
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text(
                                text = if (locked) "Buy" else "Exclusive",
                                color = Color.White
                            )

                            if (locked) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                )
            }
        }
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
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onQueryClick,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = query,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
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
            modifier = Modifier.size(80.dp),
            tint = Color.White.copy(alpha = 0.3f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No drills found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )

            Text(
                text = "Try adjusting your filters or search query",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        Button(
            onClick = onBackToAll,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LimeGreen
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
