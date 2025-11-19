package com.sportall.az.ui.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.sportall.az.ui.paywall.PaywallScreen
import com.sportall.az.ui.paywall.PaywallType
import org.koin.compose.koinInject

@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    val scrollState = rememberScrollState()

    var showWipeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            SettingsTopBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Language Section
            LanguageSection()

            // Data Section
            DataSection(
                exportUnlocked = state.exportUnlocked,
                wipeUnlocked = state.wipeUnlocked,
                onWipeClick = {
                    if (state.wipeUnlocked) {
                        showWipeDialog = true
                    } else {
                        navigator.push(PaywallScreen(PaywallType.WIPE))
                    }
                },
                onExportClick = {
                    if (state.exportUnlocked) {
                        val data = viewModel.exportData()
                        // TODO: Save to file / share
                        println("Exported data: $data")
                    } else {
                        navigator.push(PaywallScreen(PaywallType.EXPORT))
                    }
                }
            )

            // Other Section
            OtherSection(
                onAboutClick = { navigator.push(AboutScreen) },
                onRateClick = { /* TODO: Open app store */ }
            )
        }

        // Wipe Confirmation Dialog
        if (showWipeDialog) {
            WipeConfirmationDialog(
                onConfirm = {
                    viewModel.wipeData()
                    showWipeDialog = false
                },
                onDismiss = { showWipeDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = com.sportall.az.ui.theme.DeepBlue
        )
    )
}

@Composable
fun LanguageSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Language",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "English (default)",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

@Composable
fun DataSection(
    exportUnlocked: Boolean,
    wipeUnlocked: Boolean,
    onWipeClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Data",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Wipe Data Button
        Button(
            onClick = onWipeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (wipeUnlocked) {
                    Color(0xFFFF6B6B) // Red
                } else {
                    Color(0xFF8B0000).copy(alpha = 0.7f) // Dark red (locked)
                }
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wipe data (IAP)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (!wipeUnlocked) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Export Data Button
        Button(
            onClick = onExportClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (exportUnlocked) {
                    Color(0xFFB4FF39) // Lime green
                } else {
                    Color(0xFF4CAF50).copy(alpha = 0.7f) // Darker green (locked)
                }
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Export data (IAP)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                if (!exportUnlocked) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OtherSection(
    onAboutClick: () -> Unit,
    onRateClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Other",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // About Row
        SettingsRow(
            title = "About",
            onClick = onAboutClick
        )

        // Rate App Row
        SettingsRow(
            title = "Rate app",
            onClick = onRateClick
        )
    }
}

@Composable
fun SettingsRow(
    title: String,
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
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun WipeConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Wipe data (IAP)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "You are about to wipe all training history. Continue?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6B6B) // Red
                )
            ) {
                Text(
                    text = "Wipe",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
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
