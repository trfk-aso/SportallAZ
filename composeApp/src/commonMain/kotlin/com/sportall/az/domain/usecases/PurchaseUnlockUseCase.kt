package com.sportall.az.domain.usecases

import com.sportall.az.repositories.IapRepository

class PurchaseUnlockUseCase(private val iap: IapRepository) {
    fun unlockExport() = iap.unlockExport()
    fun unlockWipe() = iap.unlockWipe()
    fun unlockExclusive() = iap.unlockExclusive()
}

