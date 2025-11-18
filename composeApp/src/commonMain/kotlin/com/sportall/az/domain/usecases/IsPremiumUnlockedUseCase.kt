package com.sportall.az.domain.usecases

import com.sportall.az.repositories.IapRepository

class IsPremiumUnlockedUseCase(private val iap: IapRepository) {
    operator fun invoke(): Boolean = iap.isPremiumUnlocked()
}

