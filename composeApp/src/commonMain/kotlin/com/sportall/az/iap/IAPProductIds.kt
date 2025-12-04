package com.sportall.az.iap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.sportall.az.domain.usecases.PurchaseUnlockUseCase
import com.sportall.az.ui.paywall.PaywallType
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.koinInject

object IAPProductIds {
    const val EXCLUSIVE = "com.sportall.exclusive_pack"
    const val EXPORT = "com.sportall.export"
    const val WIPE = "com.sportall.wipe"

    fun fromPaywall(type: PaywallType): String = when (type) {
        PaywallType.EXCLUSIVE -> EXCLUSIVE
        PaywallType.EXPORT -> EXPORT
        PaywallType.WIPE -> WIPE
    }
}

data class IAPProduct(
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val isPurchased: Boolean = false
)

sealed class PurchaseResult {
    object Success : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
    object Cancelled : PurchaseResult()
}

expect class IAPManager() {

    fun initialize(context: Any?)


    suspend fun getProducts(): List<IAPProduct>


    fun isPurchased(productId: String): Boolean


    suspend fun purchase(productId: String): PurchaseResult


    suspend fun restorePurchases(): PurchaseResult


    val purchaseState: StateFlow<Map<String, Boolean>>
}

expect fun createIAPManager(): IAPManager

data class ProcessingPurchaseScreen(val productId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val iap = createIAPManager()

        val purchaseUnlock: PurchaseUnlockUseCase = koinInject()

        LaunchedEffect(productId) {
            val result = iap.purchase(productId)

            when (result) {
                is PurchaseResult.Success -> {

                    when (productId) {
                        IAPProductIds.EXCLUSIVE -> purchaseUnlock.unlockExclusive()
                        IAPProductIds.EXPORT -> purchaseUnlock.unlockExport()
                        IAPProductIds.WIPE -> purchaseUnlock.unlockWipe()
                    }

                    navigator.pop()
                }

                is PurchaseResult.Cancelled -> navigator.pop()
                is PurchaseResult.Error -> navigator.pop()
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}