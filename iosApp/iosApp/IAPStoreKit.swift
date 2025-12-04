import Foundation
import StoreKit
import ComposeApp

@available(iOS 15.0, *)
@objc public class IAPStoreKit: NSObject {

    @objc public static let shared = IAPStoreKit()

    private var products: [Product] = []
    private var updateListenerTask: Task<Void, Error>?

    private var purchaseStateCallback: (([String: Bool]) -> Void)?

    private let productIds: [String] = [
        "com.sportall.exclusive_pack",
        "com.sportall.export",
        "com.sportall.wipe"
    ]

    @objc public override init() {
        super.init()
        updateListenerTask = listenForTransactions()

        Task {
            await loadProducts()
        }
    }

    deinit {
        updateListenerTask?.cancel()
    }

    @objc public func setPurchaseStateCallback(_ callback: @escaping ([String: Bool]) -> Void) {
        self.purchaseStateCallback = callback
    }

    private func loadProducts() async {
        do {
            products = try await Product.products(for: productIds)
            await updatePurchaseStates()
        } catch {
            print("IAP ERROR loading products: \(error)")
        }
    }

    @objc public func getProductsAsync(completion: @escaping ([NSDictionary]) -> Void) {
        Task {
            if products.isEmpty {
                await loadProducts()
            }

            var items: [NSDictionary] = []

            for product in products {
                let isPurchased = await checkPurchased(productId: product.id)

                let dict: NSDictionary = [
                    "id": product.id,
                    "title": product.displayName,
                    "description": product.description,
                    "price": product.displayPrice,
                    "isPurchased": isPurchased
                ]

                items.append(dict)
            }

            completion(items)
        }
    }

    @objc public func isPurchasedAsync(id: String, completion: @escaping (Bool) -> Void) {
        Task {
            completion(await checkPurchased(productId: id))
        }
    }

    private func checkPurchased(productId: String) async -> Bool {
        for await result in Transaction.currentEntitlements {
            if case .verified(let transaction) = result, transaction.productID == productId {
                return true
            }
        }
        return false
    }

    @objc public func purchaseAsync(id: String, completion: @escaping (Bool, String?) -> Void) {
        Task {
            if products.isEmpty {
                await loadProducts()
            }

            guard let product = products.first(where: { $0.id == id }) else {
                completion(false, "Product not found")
                return
            }

            do {
                let result = try await product.purchase()

                switch result {
                case .success(let verification):
                    switch verification {
                    case .verified(let transaction):
                        await transaction.finish()
                        await updatePurchaseStates()
                        completion(true, nil)

                    case .unverified:
                        completion(false, "Verification failed")
                    }

                case .userCancelled:
                    completion(false, "cancelled")

                case .pending:
                    completion(false, "pending")

                @unknown default:
                    completion(false, "unknown")
                }

            } catch {
                completion(false, error.localizedDescription)
            }
        }
    }

    @objc public func restorePurchasesAsync(completion: @escaping (Bool, String?) -> Void) {
        Task {
            do {
                try await AppStore.sync()
                await updatePurchaseStates()
                completion(true, nil)
            } catch {
                completion(false, error.localizedDescription)
            }
        }
    }

    private func updatePurchaseStates() async {
        var states: [String: Bool] = [:]

        for id in productIds {
            states[id] = await checkPurchased(productId: id)
        }

        DispatchQueue.main.async {
            self.purchaseStateCallback?(states)
        }
    }

    private func listenForTransactions() -> Task<Void, Error> {
        return Task.detached {
            for await update in Transaction.updates {
                do {
                    let transaction = try self.checkVerified(update)
                    await transaction.finish()
                    await self.updatePurchaseStates()
                } catch {
                    print("IAP verification error: \(error)")
                }
            }
        }
    }

    private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .verified(let safe):
            return safe
        case .unverified:
            throw IAPError.failedVerification
        }
    }
}

enum IAPError: Error {
    case failedVerification
}