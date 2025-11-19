package com.sportall.az.domain.usecases

import com.sportall.az.repositories.FavoritesRepository
import com.sportall.az.repositories.HistoryRepository

class WipeDataUseCase(
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository
) {
    operator fun invoke() {
        historyRepository.clear()
        favoritesRepository.clear()
    }
}
