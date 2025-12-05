package com.sportall.az.ui.home

import androidx.compose.foundation.Image
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
import com.sportall.az.generated.resources.Res
import com.sportall.az.generated.resources.logo_sportall
import org.jetbrains.compose.resources.painterResource
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
import com.sportall.az.ui.utils.getDrillImageResource
import org.koin.compose.koinInject
import androidx.compose.ui.layout.ContentScale
import com.sportall.az.generated.resources.bg_dark
import com.sportall.az.ui.theme.LimeGreen

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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(Res.drawable.bg_dark),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            topBar = {
                HomeTopBar(
                    onSearchClick = { tabNavigator.current = SearchTab },
                    onSettingsClick = { tabNavigator.current = SettingsTab }
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
                                },
                                isExclusiveUnlocked = state.isExclusiveUnlocked
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
                                    isExclusiveUnlocked = state.isExclusiveUnlocked,
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
                Image(
                    painter = painterResource(Res.drawable.logo_sportall),
                    contentDescription = "Sportall AZ Logo",
                    modifier = Modifier.height(350.dp)
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
            containerColor = Color.Transparent
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
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.sportall.az.ui.theme.SurfaceBlue
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = drill.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = drill.category.displayName,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.height(22.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = com.sportall.az.ui.theme.DrillCardBlue,
                        labelColor = Color.White
                    )
                )
            }

            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                val imageResource = getDrillImageResource(drill.id)
                if (imageResource != null) {
                    Image(
                        painter = painterResource(imageResource),
                        contentDescription = drill.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = DrillCardBlue,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = DrillCardBlue,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = drill.visualDescription.take(12),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 8.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = com.sportall.az.ui.theme.LimeGreen
            )
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleSmall,
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
    onCategorySelected: (Category?) -> Unit,
    isExclusiveUnlocked: Boolean
) {
    val sortedCategories = remember(categories) {
        val exclusive = categories.firstOrNull { it == Category.Exclusive }
        val others = categories.filter { it != Category.Exclusive }

        if (exclusive != null) {
            buildList {
                addAll(others.take(1))
                add(exclusive)
                addAll(others.drop(1))
            }
        } else {
            categories
        }
    }

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
                    selectedContainerColor = LimeGreen,
                    selectedLabelColor = Color.Black,
                    containerColor = Color.Transparent,
                    labelColor = Color.White
                )
            )
        }

        items(sortedCategories) { category ->

            val isExclusive = category == Category.Exclusive
            val locked = isExclusive && !isExclusiveUnlocked

            val labelText = if (locked) "Buy" else category.displayName

            Box(
                modifier = Modifier.wrapContentSize()
            ) {

                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(labelText) },
                    leadingIcon = if (locked) {
                        {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Gold
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LimeGreen,
                        selectedLabelColor = Color.Black,
                        containerColor = Color.Transparent,
                        labelColor = Color.White,
                        iconColor = if (locked) Gold else Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun DrillsGrid(
    drills: List<Drill>,
    favorites: Set<Int>,
    isExclusiveUnlocked: Boolean,
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
                isExclusiveUnlocked = isExclusiveUnlocked,
                onClick = { onDrillClick(drill) }
            )
        }
    }
}

@Composable
fun DrillCard(
    drill: Drill,
    isFavorite: Boolean,
    isExclusiveUnlocked: Boolean,
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
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                val imageResource = getDrillImageResource(drill.id)

                if (imageResource != null) {
                    Image(
                        painter = painterResource(imageResource),
                        contentDescription = drill.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (drill.isExclusive) Gold.copy(alpha = 0.4f)
                                else DrillCardBlue
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
                    }
                }

                if (drill.isExclusive && !isExclusiveUnlocked) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.70f))
                    )

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Exclusive",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                    )

                    Text(
                        text = "Buy",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 70.dp)
                    )
                }

                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Gold else Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .size(32.dp)
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
                        containerColor = DrillCardBlue,
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
