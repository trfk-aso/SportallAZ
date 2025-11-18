package com.sportall.az.domain.usecases

import com.sportall.az.repositories.IapRepository

class PurchaseUnlockUseCase(private val iap: IapRepository) {
    fun unlockPremium() = iap.unlockPremium()
    fun unlockExclusive() = iap.unlockExclusive()
}

