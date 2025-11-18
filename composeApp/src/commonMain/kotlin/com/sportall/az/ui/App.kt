package com.sportall.az.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.*
import cafe.adriel.voyager.core.screen.Screen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.sportall.az.di.ProvideKoin
import com.sportall.az.ui.splash.SplashScreen
import com.sportall.az.ui.home.HomeScreen

@Composable
fun App() {
    ProvideKoin {
        MaterialTheme {
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

    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = tabNavigator.current == tab,
                onClick = { tabNavigator.current = tab },
                icon = { Icon(tab.icon, tab.options.title) },
                label = { Text(tab.options.title) }
            )
        }
    }
}

interface IconTab : Tab {
    val icon: ImageVector
}

object HomeTab : IconTab {
    override val icon = Icons.Filled.Home

    @Composable
    override fun Content() { HomeScreen() }

    override val options: TabOptions
        @Composable get() = remember {
            TabOptions(0u, "Home", null)
        }
}

object SearchTab : IconTab {
    override val icon = Icons.Filled.Search

    @Composable
    override fun Content() { SearchScreen() }

    override val options: TabOptions
        @Composable get() = remember {
            TabOptions(1u, "Search", null)
        }
}

object FavoritesTab : IconTab {
    override val icon = Icons.Filled.Favorite

    @Composable
    override fun Content() { FavoritesScreen() }

    override val options: TabOptions
        @Composable get() = remember {
            TabOptions(2u, "Favorites", null)
        }
}

object HistoryTab : IconTab {
    override val icon = Icons.Filled.History

    @Composable
    override fun Content() { HistoryScreen() }

    override val options: TabOptions
        @Composable get() = remember {
            TabOptions(3u, "History", null)
        }
}

object SettingsTab : IconTab {
    override val icon = Icons.Filled.Settings

    @Composable
    override fun Content() { SettingsScreen() }

    override val options: TabOptions
        @Composable get() = remember {
            TabOptions(4u, "Settings", null)
        }
}

@Composable fun SearchScreen() {}
@Composable fun FavoritesScreen() {}
@Composable fun HistoryScreen() {}
@Composable fun SettingsScreen() {}
