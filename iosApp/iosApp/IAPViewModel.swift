import Foundation
import StoreKit
import Combine

@available(iOS 15.0, *)
@MainActor
class IAPViewModel: ObservableObject {

    static let shared = IAPViewModel()

    @Published var products: [Product] = []
    @Published var purchasedProductIDs = Set<String>()

    private let productIDs = [
        "com.sportall.exclusive_pack",
        "com.sportall.export",
        "com.sportall.wipe"
    ]

    private var updateListenerTask: Task<Void, Error>?


    init() {
        updateListenerTask = listenForTransactions()

        Task {
            await loadProducts()
            await updatePurchasedProducts()
        }
    }

    deinit {
        updateListenerTask?.cancel()
    }

    func loadProducts() async {
        do {
            products = try await Product.products(for: productIDs)
        } catch {
            print("IAP ERROR loading products: \(error)")
        }
    }

    func updatePurchasedProducts() async {
        var newPurchased = Set<String>()

        for await result in Transaction.currentEntitlements {
            if case .verified(let transaction) = result {
                newPurchased.insert(transaction.productID)
            }
        }

        purchasedProductIDs = newPurchased
    }

    func isPurchased(_ productID: String) -> Bool {
        purchasedProductIDs.contains(productID)
    }

    func purchase(_ product: Product) async throws -> Transaction? {
        let result = try await product.purchase()

        switch result {
        case .success(let verification):
            let transaction = try checkVerified(verification)
            await transaction.finish()
            await updatePurchasedProducts()
            return transaction

        case .userCancelled:
            return nil

        case .pending:
            return nil

        @unknown default:
            return nil
        }
    }

    func restorePurchases() async {
        do {
            try await AppStore.sync()
        } catch {
            print("Restore sync failed: \(error)")
        }
        await updatePurchasedProducts()
    }

    private func listenForTransactions() -> Task<Void, Error> {
        return Task.detached {
            for await result in Transaction.updates {
                do {
                    let transaction = try await self.checkVerified(result)
                    await transaction.finish()
                    await self.updatePurchasedProducts()
                } catch {
                    print("IAP verification error: \(error)")
                }
            }
        }
    }

    private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .verified(let value):
            return value
        case .unverified:
            throw StoreError.failedVerification
        }
    }
}

enum StoreError: Error {
    case failedVerification
}