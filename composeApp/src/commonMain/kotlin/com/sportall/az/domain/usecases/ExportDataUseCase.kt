package com.sportall.az.domain.usecases

import com.sportall.az.repositories.FavoritesRepository
import com.sportall.az.repositories.HistoryRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ExportData(
    val favorites: List<Int>,
    val history: List<com.sportall.az.models.HistoryRecord>
)

class ExportDataUseCase(
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val json: Json = Json { prettyPrint = true }
) {
    operator fun invoke(): String {
        val data = ExportData(
            favorites = favoritesRepository.getFavorites(),
            history = historyRepository.getAll()
        )
        return json.encodeToString(ExportData.serializer(), data)
    }
}

