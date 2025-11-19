package com.sportall.az.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.*
import cafe.adriel.voyager.core.screen.Screen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.sportall.az.di.ProvideKoin
import com.sportall.az.ui.splash.SplashScreen
import com.sportall.az.ui.home.HomeScreen
import com.sportall.az.ui.settings.SettingsScreen
import com.sportall.az.ui.history.StatisticsScreen
import com.sportall.az.ui.history.HistoryScreen
import com.sportall.az.ui.favorites.FavoritesScreen
import com.sportall.az.ui.search.SearchScreen
import com.sportall.az.ui.theme.DeepBlue
import com.sportall.az.ui.theme.LimeGreen
import com.sportall.az.ui.theme.SurfaceBlue
import com.sportall.az.ui.theme.SportallTheme

@Composable
fun App() {
    ProvideKoin {
        SportallTheme {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    Navigator(SplashScreen)
}

data object MainTabsScreen : Screen {
    @Composable
    override fun Content() {
        TabNavigator(HomeTab) {
            Scaffold(
                bottomBar = { BottomNavigationBar() }
            ) {
                CurrentTab()
            }
        }
    }
}

@Composable
fun BottomNavigationBar() {
    val tabNavigator = LocalTabNavigator.current

    val tabs = listOf(
        HomeTab,
        SearchTab,
        FavoritesTab,
        HistoryTab,
        SettingsTab
    )

    NavigationBar(
        containerColor = DeepBlue,
        contentColor = Color.White
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = tabNavigator.current == tab,
                onClick = { tabNavigator.current = tab },
                icon = { Icon(tab.icon, tab.options.title) },
                label = { Text(tab.options.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LimeGreen,
                    selectedTextColor = LimeGreen,
                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                    unselectedTextColor = Color.White.copy(alpha = 0.6f),
                    indicatorColor = SurfaceBlue
                )
            )
        }
    }
}

interface IconTab : Tab {
    val icon: ImageVector
}

// Screen wrappers for tab content
data object HomeScreenObj : Screen {
    @Composable
    override fun Content() { HomeScreen() }
}

data object SearchScreenObj : Screen {
    @Composable
    override fun Content() {
        SearchScreen()
    }
}

data object FavoritesScreenObj : Screen {
    @Composable
    override fun Content() { FavoritesScreen() }
}

data object HistoryScreenObj : Screen {
    @Composable
    override fun Content() {
        HistoryScreen.Content()
    }
}

data object SettingsScreenObj : Screen {
    @Composable
    override fun Content() { SettingsScreen() }
}

object HomeTab : IconTab {
    override val icon = Icons.Filled.Home

    @Composable
    override fun Content() {
        Navigator(HomeScreenObj)
    }

    override val options: TabOptions
        @Composable get() = remember {
            TabOptions(0u, "Home", null)
        }
}

object SearchTab : IconTab {
    override val icon = Icons.Filled.Search

    @Composable
    override fun Content() {
        Navigator(SearchScreenObj)
    }

    override val options: TabOptions
        @Composable get() = remember {
            TabOptions(1u, "Search", null)
        }
}

object FavoritesTab : IconTab {
    override val icon = Icons.Filled.Favorite

    @Composable
    override fun Content() {
        Navigator(FavoritesScreenObj)
    }

    override val options: TabOptions
        @Composable get() = remember {
            TabOptions(2u, "Journal", null)
        }
}

object HistoryTab : IconTab {
    override val icon = Icons.Filled.History

    @Composable
    override fun Content() {
        Navigator(HistoryScreenObj)
    }

    override val options: TabOptions
        @Composable get() = remember {
            TabOptions(3u, "History", null)
        }
}

object SettingsTab : IconTab {
    override val icon = Icons.Filled.Settings

    @Composable
    override fun Content() {
        Navigator(SettingsScreenObj)
    }

    override val options: TabOptions
        @Composable get() = remember {
            TabOptions(4u, "Settings", null)
        }
}
