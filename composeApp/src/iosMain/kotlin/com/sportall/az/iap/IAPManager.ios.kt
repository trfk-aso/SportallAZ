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

    private var productsCallback: ((List<SKProduct>) -> Unit)? = null
    private var purchaseCallback: ((PurchaseResult) -> Unit)? = null

    private var productRequest: SKProductsRequest? = null

    private val productIds = setOf(
        IAPProductIds.EXCLUSIVE,
        IAPProductIds.EXPORT,
        IAPProductIds.WIPE
    )

    init {
        SKPaymentQueue.defaultQueue().addTransactionObserver(this)
        loadPurchasedProducts()

        PromotionConnector.onPromotionReceived = { productId ->
            println(" IOSBillingDelegate processing promotion → $productId")
            scope.launch {
                autoPurchaseFromPromotion(productId)
            }
        }
    }

    private suspend fun autoPurchaseFromPromotion(productId: String) {
        println("Promotion → auto-purchase triggered for: $productId")

        withContext(Dispatchers.Main) {
            suspendCoroutine { continuation ->
                val request = SKProductsRequest(
                    productIdentifiers = setOf(productId) as Set<Any>
                )

                request.delegate = object : NSObject(), SKProductsRequestDelegateProtocol {
                    override fun productsRequest(
                        request: SKProductsRequest,
                        didReceiveResponse: SKProductsResponse
                    ) {
                        val products = didReceiveResponse.products as List<SKProduct>

                        if (products.isEmpty()) {
                            println("Promotion → Product not found in StoreKit: $productId")
                            continuation.resume(Unit)
                            return
                        }

                        val product = products.first()

                        println("Promotion → Found product: ${product.productIdentifier}")
                        println("Promotion → Starting payment...")

                        SKPaymentQueue.defaultQueue().addPayment(
                            SKPayment.paymentWithProduct(product)
                        )

                        continuation.resume(Unit)
                    }

                    override fun request(request: SKRequest, didFailWithError: NSError) {
                        println("Promotion → Failed to load product: ${didFailWithError.localizedDescription}")
                        continuation.resume(Unit)
                    }
                }

                request.start()
            }
        }
    }

    actual fun initialize(context: Any?) {
    }

    actual suspend fun getProducts(): List<IAPProduct> = withContext(Dispatchers.Main) {
        suspendCoroutine { cont ->
            val request = SKProductsRequest(productIdentifiers = productIds as Set<Any>)
            productRequest = request

            productsCallback = { products ->
                val mapped = products.map { p ->
                    val formatter = NSNumberFormatter().apply {
                        numberStyle = NSNumberFormatterCurrencyStyle
                        locale = p.priceLocale
                    }

                    IAPProduct(
                        id = p.productIdentifier,
                        title = p.localizedTitle,
                        description = p.localizedDescription,
                        price = formatter.stringFromNumber(p.price) ?: "",
                        isPurchased = isPurchased(p.productIdentifier)
                    )
                }

                cont.resume(mapped)
            }

            request.delegate = this@IAPManager
            request.start()
        }
    }

    override fun productsRequest(
        request: SKProductsRequest,
        didReceiveResponse: SKProductsResponse
    ) {
        productsCallback?.invoke(didReceiveResponse.products as List<SKProduct>)
        productsCallback = null
    }

    override fun request(request: SKRequest, didFailWithError: NSError) {
        productsCallback?.invoke(emptyList())
        productsCallback = null
    }


    actual suspend fun purchase(productId: String): PurchaseResult =
        withContext(Dispatchers.Main) {
            suspendCoroutine { cont ->

                if (!SKPaymentQueue.canMakePayments()) {
                    cont.resume(PurchaseResult.Error("Purchases disabled"))
                    return@suspendCoroutine
                }

                val request = SKProductsRequest(productIdentifiers = setOf(productId) as Set<Any>)
                request.delegate = object : NSObject(), SKProductsRequestDelegateProtocol {

                    override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
                        val products = didReceiveResponse.products as List<SKProduct>
                        if (products.isEmpty()) {
                            cont.resume(PurchaseResult.Error("Product not found"))
                            return
                        }

                        val payment = SKPayment.paymentWithProduct(products.first())
                        purchaseCallback = { result -> cont.resume(result) }
                        SKPaymentQueue.defaultQueue().addPayment(payment)
                    }

                    override fun request(request: SKRequest, didFailWithError: NSError) {
                        cont.resume(PurchaseResult.Error(didFailWithError.localizedDescription))
                    }
                }

                request.start()
            }
        }


    actual suspend fun restorePurchases(): PurchaseResult =
        withContext(Dispatchers.Main) {
            suspendCoroutine { cont ->

                purchaseCallback = { result -> cont.resume(result) }

                SKPaymentQueue.defaultQueue().restoreCompletedTransactions()
            }
        }


    override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
        val txList = updatedTransactions as List< SKPaymentTransaction>

        txList.forEach { tx ->
            when (tx.transactionState) {

                SKPaymentTransactionState.SKPaymentTransactionStatePurchased -> {
                    SKPaymentQueue.defaultQueue().finishTransaction(tx)

                    val id = tx.payment.productIdentifier
                    savePurchased(id)

                    purchaseCallback?.invoke(PurchaseResult.Success)
                    purchaseCallback = null
                }

                SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                    SKPaymentQueue.defaultQueue().finishTransaction(tx)

                    val isCancelled = tx.error?.code == 2L
                    val errorMsg = tx.error?.localizedDescription

                    purchaseCallback?.invoke(
                        if (isCancelled) PurchaseResult.Cancelled
                        else PurchaseResult.Error(errorMsg ?: "Purchase failed")
                    )
                    purchaseCallback = null
                }

                SKPaymentTransactionState.SKPaymentTransactionStateRestored -> {
                    SKPaymentQueue.defaultQueue().finishTransaction(tx)
                    savePurchased(tx.payment.productIdentifier)
                }

                else -> {}
            }
        }
    }

    override fun paymentQueueRestoreCompletedTransactionsFinished(queue: SKPaymentQueue) {
        purchaseCallback?.invoke(PurchaseResult.Success)
        purchaseCallback = null
    }

    override fun paymentQueue(
        queue: SKPaymentQueue,
        restoreCompletedTransactionsFailedWithError: NSError
    ) {
        purchaseCallback?.invoke(PurchaseResult.Error(restoreCompletedTransactionsFailedWithError.localizedDescription))
        purchaseCallback = null
    }

    actual fun isPurchased(productId: String): Boolean {
        return _purchaseState.value[productId] ?: false
    }

    private fun savePurchased(productId: String) {
        val map = _purchaseState.value.toMutableMap()
        map[productId] = true
        _purchaseState.value = map

        NSUserDefaults.standardUserDefaults.setBool(true, forKey = "iap_$productId")
        NSUserDefaults.standardUserDefaults.synchronize()
    }

    private fun loadPurchasedProducts() {
        val map = mutableMapOf<String, Boolean>()

        productIds.forEach { id ->
            val purchased = NSUserDefaults.standardUserDefaults.boolForKey("iap_$id")
            if (purchased) map[id] = true
        }

        _purchaseState.value = map
    }
}

actual fun createIAPManager(): IAPManager = IAPManager()