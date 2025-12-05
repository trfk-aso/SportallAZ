package com.sportall.az.iap

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSError
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSUserDefaults
import platform.StoreKit.SKPayment
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState
import platform.StoreKit.SKProduct
import platform.StoreKit.SKProductsRequest
import platform.StoreKit.SKProductsRequestDelegateProtocol
import platform.StoreKit.SKProductsResponse
import platform.StoreKit.SKRequest
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
actual class IAPManager : NSObject(),
    SKProductsRequestDelegateProtocol,
    SKPaymentTransactionObserverProtocol {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _purchaseState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    actual val purchaseState: StateFlow<Map<String, Boolean>> = _purchaseState

    private val products = mutableMapOf<String, SKProduct>()

    private val callbacks = mutableMapOf<String, (PurchaseResult) -> Unit>()

    private val productIds = setOf(
        IAPProductIds.EXCLUSIVE,
        IAPProductIds.EXPORT,
        IAPProductIds.WIPE
    )

    init {
        SKPaymentQueue.defaultQueue().addTransactionObserver(this)
        loadPurchasedProducts()
        fetchProducts()

        PromotionConnector.onPromotionReceived = { productId ->
            println("PROMO → Received promotion for: $productId")
            val product = products[productId]
            if (product != null) {
                SKPaymentQueue.defaultQueue().addPayment(
                    SKPayment.paymentWithProduct(product)
                )
            } else {
                println("PROMO → Product not loaded yet")
            }
        }
    }

    private fun fetchProducts() {
        val request = SKProductsRequest(productIdentifiers = productIds as Set<Any>)
        request.delegate = this
        request.start()
    }

    actual suspend fun getProducts(): List<IAPProduct> =
        withContext(Dispatchers.Main) {
            products.values.map { sk ->
                val formatter = NSNumberFormatter().apply {
                    numberStyle = NSNumberFormatterCurrencyStyle
                    locale = sk.priceLocale
                }
                IAPProduct(
                    id = sk.productIdentifier,
                    title = sk.localizedTitle,
                    description = sk.localizedDescription,
                    price = formatter.stringFromNumber(sk.price) ?: "",
                    isPurchased = isPurchased(sk.productIdentifier)
                )
            }
        }

    override fun productsRequest(
        request: SKProductsRequest,
        didReceiveResponse: SKProductsResponse
    ) {
        (didReceiveResponse.products as List<SKProduct>).forEach { p ->
            products[p.productIdentifier] = p
        }
        println("IAP → Loaded products: ${products.keys}")
    }


    actual suspend fun purchase(productId: String): PurchaseResult =
        withContext(Dispatchers.Main) {
            suspendCoroutine { cont ->

                val product = products[productId]
                if (product == null) {
                    cont.resume(PurchaseResult.Error("Product not loaded"))
                    return@suspendCoroutine
                }

                callbacks[productId] = { result ->
                    cont.resume(result)
                }

                SKPaymentQueue.defaultQueue().addPayment(
                    SKPayment.paymentWithProduct(product)
                )
            }
        }


    actual suspend fun restorePurchases(): PurchaseResult =
        withContext(Dispatchers.Main) {
            suspendCoroutine { cont ->
                callbacks["restore"] = { cont.resume(it) }
                SKPaymentQueue.defaultQueue().restoreCompletedTransactions()
            }
        }


    override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
        updatedTransactions.forEach { any ->
            val tx = any as? SKPaymentTransaction ?: return@forEach
            val id = tx.payment.productIdentifier

            when (tx.transactionState) {

                SKPaymentTransactionState.SKPaymentTransactionStatePurchased -> {
                    savePurchased(id)
                    callbacks.remove(id)?.invoke(PurchaseResult.Success)
                    SKPaymentQueue.defaultQueue().finishTransaction(tx)
                }

                SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                    val isCancelled = tx.error?.code == 2L
                    val msg = tx.error?.localizedDescription ?: "Purchase failed"

                    callbacks.remove(id)?.invoke(
                        if (isCancelled) PurchaseResult.Cancelled
                        else PurchaseResult.Error(msg)
                    )

                    SKPaymentQueue.defaultQueue().finishTransaction(tx)
                }

                SKPaymentTransactionState.SKPaymentTransactionStateRestored -> {
                    savePurchased(id)
                    callbacks.remove("restore")?.invoke(PurchaseResult.Success)
                    SKPaymentQueue.defaultQueue().finishTransaction(tx)
                }

                else -> {}
            }
        }
    }

    override fun paymentQueueRestoreCompletedTransactionsFinished(queue: SKPaymentQueue) {
        callbacks.remove("restore")?.invoke(PurchaseResult.Success)
    }

    override fun paymentQueue(
        queue: SKPaymentQueue,
        restoreCompletedTransactionsFailedWithError: NSError
    ) {
        callbacks.remove("restore")?.invoke(
            PurchaseResult.Error(restoreCompletedTransactionsFailedWithError.localizedDescription)
        )
    }

    actual fun isPurchased(productId: String): Boolean =
        _purchaseState.value[productId] ?: false

    private fun savePurchased(productId: String) {
        val map = _purchaseState.value.toMutableMap()
        map[productId] = true
        _purchaseState.value = map

        NSUserDefaults.standardUserDefaults.setBool(true, forKey = "iap_$productId")
        NSUserDefaults.standardUserDefaults.synchronize()
    }

    private fun loadPurchasedProducts() {
        val result = mutableMapOf<String, Boolean>()
        productIds.forEach { id ->
            if (NSUserDefaults.standardUserDefaults.boolForKey("iap_$id")) {
                result[id] = true
            }
        }
        _purchaseState.value = result
    }

    actual fun initialize(context: Any?) {}
}

private val sharedIAPManager = IAPManager()
actual fun createIAPManager(): IAPManager = sharedIAPManager