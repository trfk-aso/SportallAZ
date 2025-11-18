package com.sportall.az.domain.usecases

import com.sportall.az.models.HistoryRecord
import com.sportall.az.repositories.HistoryRepository

class AddHistoryItemUseCase(private val history: HistoryRepository) {
    operator fun invoke(record: HistoryRecord) = history.add(record)
}

