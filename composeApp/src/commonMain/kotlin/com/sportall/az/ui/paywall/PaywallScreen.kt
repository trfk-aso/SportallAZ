package com.sportall.az.ui.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.sportall.az.iap.IAPProductIds
import com.sportall.az.iap.ProcessingPurchaseScreen
import com.sportall.az.iap.createIAPManager
import org.koin.compose.koinInject

enum class PaywallType {
    EXCLUSIVE,
    EXPORT,
    WIPE
}

data class PaywallScreen(val type: PaywallType) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val purchaseUnlock: PurchaseUnlockUseCase = koinInject()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        val iap = createIAPManager()

        LaunchedEffect(Unit) {
            iap.purchaseState.collect { stateMap ->
                val productId = IAPProductIds.fromPaywall(type)
                if (stateMap[productId] == true) {
                    navigator.pop()
                }
            }
        }

        val content = when (type) {
            PaywallType.EXCLUSIVE -> PaywallContent(
                title = "Unlock Exclusive Pack",
                description = "Get access to premium drills designed by professional coaches",
                features = listOf(
                    "5+ exclusive drills",
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

        ModalBottomSheet(
            onDismissRequest = { navigator.pop() },
            sheetState = sheetState,
            containerColor = com.sportall.az.ui.theme.DeepBlue,
            contentColor = Color.White,
            dragHandle = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                Text(
                    text = content.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

                Text(
                    text = content.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.sportall.az.ui.theme.SurfaceBlue
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

                Text(
                    text = content.price,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val iap = createIAPManager()

                        val productId = IAPProductIds.fromPaywall(type)

                        navigator.push(ProcessingPurchaseScreen(productId))
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

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
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
            modifier = Modifier.weight(1f),
            color = Color.White
        )
    }
}

data class PaywallContent(
    val title: String,
    val description: String,
    val features: List<String>,
    val price: String
)
