package com.sportall.az.ui.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.sportall.az.domain.usecases.PurchaseUnlockUseCase
import org.koin.compose.koinInject

enum class PaywallType {
    EXCLUSIVE,
    EXPORT,
    WIPE
}

data class PaywallScreen(val type: PaywallType) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val purchaseUnlock: PurchaseUnlockUseCase = koinInject()

        val content = when (type) {
            PaywallType.EXCLUSIVE -> PaywallContent(
                title = "Unlock Exclusive Pack",
                description = "Get access to premium drills designed by professional coaches",
                features = listOf(
                    "20+ exclusive drills",
                    "Advanced training techniques",
                    "Professional coaching tips",
                    "Lifetime access"
                ),
                price = "$4.99"
            )
            PaywallType.EXPORT -> PaywallContent(
                title = "Unlock Data Export",
                description = "Export your training history and statistics to share or backup",
                features = listOf(
                    "Export to CSV format",
                    "Share your progress",
                    "Backup your data",
                    "One-time purchase"
                ),
                price = "$1.99"
            )
            PaywallType.WIPE -> PaywallContent(
                title = "Unlock Data Wipe",
                description = "Securely delete all your training data and start fresh",
                features = listOf(
                    "Delete all history",
                    "Clear favorites",
                    "Reset statistics",
                    "One-time purchase"
                ),
                price = "$0.99"
            )
        }

        Scaffold(
            topBar = {
                PaywallTopBar(onBackClick = { navigator.pop() })
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Description
                Text(
                    text = content.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Features List
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        content.features.forEach { feature ->
                            FeatureItem(text = feature)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price
                Text(
                    text = content.price,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.weight(1f))

                // Purchase Button
                Button(
                    onClick = {
                        // Mock purchase - just unlock the feature
                        when (type) {
                            PaywallType.EXCLUSIVE -> purchaseUnlock.unlockExclusive()
                            PaywallType.EXPORT -> purchaseUnlock.unlockExport()
                            PaywallType.WIPE -> purchaseUnlock.unlockWipe()
                        }
                        navigator.pop()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB4FF39)
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(
                        text = "Unlock Now",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Restore Purchases Button
                TextButton(
                    onClick = { /* Mock - do nothing */ },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                ) {
                    Text(
                        text = "Restore Purchases",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

@Composable
fun FeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFFB4FF39),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

data class PaywallContent(
    val title: String,
    val description: String,
    val features: List<String>,
    val price: String
)
