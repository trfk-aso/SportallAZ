package com.sportall.az.export

import com.sportall.az.models.HistoryRecord
import com.sportall.az.repositories.FavoritesRepository
import com.sportall.az.repositories.HistoryRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class ExportPayload(
    val generatedAt: String,
    val favorites: List<Int>,
    val history: List<HistoryRecord>
)

sealed class ExportResult {
    data class Ok(val location: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

expect class PdfExporter() {
    suspend fun export(payload: ExportPayload, fileName: String = defaultFileName()): ExportResult
}

expect class ExportViewer() {
    fun view(location: String)
}

@OptIn(ExperimentalTime::class)
fun defaultFileName(): String = "SportallExport_${Clock.System.now()}.pdf"

class BuildExportPayloadUseCase(
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository
) {
    @OptIn(ExperimentalTime::class)
    operator fun invoke(): ExportPayload {
        val now = Clock.System.now().toString().replace('T',' ')
        return ExportPayload(
            generatedAt = now,
            favorites = favoritesRepository.getFavorites(),
            history = historyRepository.getAll()
        )
    }
}