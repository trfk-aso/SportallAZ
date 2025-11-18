package com.sportall.az.domain.usecases

import com.sportall.az.repositories.IapRepository

class IsWipeUnlockedUseCase(private val iap: IapRepository) {
    operator fun invoke(): Boolean = iap.isWipeUnlocked()
}
