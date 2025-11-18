package com.sportall.az.domain.usecases

import com.sportall.az.models.HistoryRecord
import com.sportall.az.repositories.HistoryRepository

class GetHistoryUseCase(private val history: HistoryRepository) {
    operator fun invoke(): List<HistoryRecord> = history.getAll()
}

