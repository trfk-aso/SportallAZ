package com.sportall.az.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.sportall.az.generated.resources.Res
import com.sportall.az.generated.resources.bg_dark
import com.sportall.az.iap.IAPProductIds
import com.sportall.az.iap.createIAPManager
import com.sportall.az.ui.paywall.PaywallScreen
import com.sportall.az.ui.paywall.PaywallType
import com.sportall.az.ui.theme.Gold
import com.sportall.az.ui.theme.LimeGreen
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    val scrollState = rememberScrollState()
    var showWipeDialog by remember { mutableStateOf(false) }

    val bottomPadding = rememberBottomBarPadding()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { SettingsTopBar() }
    ) { paddingValues ->

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
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = bottomPadding
                    )
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                LanguageSection()

                ExclusiveSettingsSection(
                    isUnlocked = state.exclusiveUnlocked,
                    onClick = {
                        if (!state.exclusiveUnlocked) {
                            navigator.push(PaywallScreen(PaywallType.EXCLUSIVE))
                        }
                    }
                )

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
                            viewModel.exportPdf()
                        } else {
                            navigator.push(PaywallScreen(PaywallType.EXPORT))
                        }
                    }
                )

                RestorePurchasesSection(
                    onRestoreClick = {
                        viewModel.restorePurchases()
                    }
                )

                val uriHandler = LocalUriHandler.current

                OtherSection(
                    onAboutClick = { navigator.push(AboutScreen) },
                    onRateClick = {
                        uriHandler.openUri("https://apps.apple.com/us/app/sportall-az/id6756007920")
                    }
                )
            }

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
}

@Composable
fun rememberBottomBarPadding(): Dp {
    val insets = WindowInsets.navigationBars.asPaddingValues()
    val bottomSystemInset = insets.calculateBottomPadding()

    val bottomBarHeight = 70.dp

    return bottomSystemInset + bottomBarHeight
}

@Composable
fun ExclusiveSettingsSection(
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = "Exclusive Pack",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        ExclusiveSettingsButton(
            isUnlocked = isUnlocked,
            onClick = onClick
        )
    }
}

@Composable
fun ExclusiveSettingsButton(
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val height = 52.dp
    val cornerRadius = 14.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .border(
                width = 2.dp,
                color = Gold,
                shape = RoundedCornerShape(cornerRadius)
            )
            .background(Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {

        if (!isUnlocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .size(30.dp)
            )
        }

        Text(
            text = if (isUnlocked) "Exclusive (Unlocked)" else "Exclusive",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun RestorePurchasesSection(onRestoreClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Restore Purchases",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(onClick = onRestoreClick)
                .padding(8.dp)
        )

        Divider(
            color = Color.White.copy(alpha = 0.3f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
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

        Button(
            onClick = onWipeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (wipeUnlocked) {
                    Color(0xFFFF3A3A)
                } else {
                    Color(0xFF8B0000).copy(alpha = 0.7f)
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

        Button(
            onClick = onExportClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (exportUnlocked) {
                    Color(0xFFB4FF39)
                } else {
                    Color(0xFF4CAF50).copy(alpha = 0.7f)
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

        SettingsRow(
            title = "About",
            onClick = onAboutClick
        )

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
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF345DED),
                            Color(0xFF0E1E63)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(24.dp)
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                Text(
                    text = "Wipe data (IAP)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "You are about to wipe all training history.\nContinue?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xCCFFFFFF),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x668A96B0)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF3A3A)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            text = "Wipe",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
