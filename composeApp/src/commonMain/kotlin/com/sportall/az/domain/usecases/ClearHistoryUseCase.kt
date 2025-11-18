package com.sportall.az.domain.usecases

import com.sportall.az.repositories.HistoryRepository

class ClearHistoryUseCase(private val history: HistoryRepository) {
    operator fun invoke() = history.clear()
}

