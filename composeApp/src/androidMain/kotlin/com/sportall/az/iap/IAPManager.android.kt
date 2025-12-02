package com.sportall.az.iap

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.sportall.az.ui.paywall.PaywallType
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class IAPManager {

    companion object {
        @Volatile
        private var instance: IAPManager? = null
        private var appContext: Context? = null

        fun getInstance(context: Context? = null): IAPManager {
            if (context != null && appContext == null) {
                appContext = context.applicationContext
            }
            return instance ?: synchronized(this) {
                instance ?: IAPManager().also {
                    instance = it
                    it.initialize(appContext)
                }
            }
        }

        operator fun invoke(): IAPManager = getInstance()
    }

    private lateinit var billingClient: BillingClient
    private var currentActivity: Activity? = null

    private val _purchaseState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    actual val purchaseState: StateFlow<Map<String, Boolean>> = _purchaseState.asStateFlow()

    private val supportedProducts = listOf(
        IAPProductIds.EXCLUSIVE,
        IAPProductIds.EXPORT,
        IAPProductIds.WIPE
    )

    actual fun initialize(context: Any?) {
        if (context is Context && !this::billingClient.isInitialized) {
            billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener { billingResult, purchases ->
                    handlePurchases(billingResult, purchases)
                }
                .build()

            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        restoreInternal()
                    }
                }

                override fun onBillingServiceDisconnected() {}
            })
        }
    }

    fun setActivity(activity: Activity) {
        currentActivity = activity
    }

    actual suspend fun getProducts(): List<IAPProduct> {
        if (!billingClient.isReady) return emptyList()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                supportedProducts.map {
                    QueryProductDetailsParams.Product
                        .newBuilder()
                        .setProductId(it)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }
            )
            .build()

        return suspendCancellableCoroutine { cont ->
            billingClient.queryProductDetailsAsync(params) { result, products ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    cont.resume(emptyList())
                    return@queryProductDetailsAsync
                }

                val mapped = products.map { p ->
                    IAPProduct(
                        id = p.productId,
                        title = p.name,
                        description = p.description,
                        price = p.oneTimePurchaseOfferDetails?.formattedPrice ?: "$0.99",
                        isPurchased = isPurchased(p.productId)
                    )
                }

                cont.resume(mapped)
            }
        }
    }

    actual suspend fun purchase(productId: String): PurchaseResult {
        if (!billingClient.isReady) return PurchaseResult.Error("Billing not ready")
        if (currentActivity == null) return PurchaseResult.Error("Activity missing")

        return suspendCancellableCoroutine { cont ->

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product
                            .newBuilder()
                            .setProductId(productId)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                )
                .build()

            billingClient.queryProductDetailsAsync(params) { result, details ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK || details.isEmpty()) {
                    cont.resume(PurchaseResult.Error("Product not found"))
                    return@queryProductDetailsAsync
                }

                val product = details[0]

                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(product)
                                .build()
                        )
                    )
                    .build()

                val response = billingClient.launchBillingFlow(currentActivity!!, flowParams)

                if (response.responseCode != BillingClient.BillingResponseCode.OK) {
                    cont.resume(PurchaseResult.Error(response.debugMessage))
                } else {
                    purchaseCallback = cont
                }
            }
        }
    }

    private var purchaseCallback: CancellableContinuation<PurchaseResult>? = null

    private fun handlePurchases(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {

            val updated = supportedProducts.associateWith { false }.toMutableMap()

            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

                    if (!purchase.isAcknowledged) {
                        val params = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()

                        billingClient.acknowledgePurchase(params) {}
                    }

                    purchase.products.forEach { productId ->
                        updated[productId] = true
                    }
                }
            }

            _purchaseState.value = updated

            purchaseCallback?.resume(PurchaseResult.Success)
            purchaseCallback = null
        }
    }

    actual suspend fun restorePurchases(): PurchaseResult {
        return restoreInternal()
    }

    private fun restoreInternal(): PurchaseResult {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            handlePurchases(result, purchases)
        }

        return PurchaseResult.Success
    }

    actual fun isPurchased(productId: String): Boolean {
        return _purchaseState.value[productId] == true
    }
}

actual fun createIAPManager(): IAPManager = IAPManager.getInstance()