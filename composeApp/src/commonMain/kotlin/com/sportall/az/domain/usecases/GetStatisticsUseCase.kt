package com.sportall.az.domain.usecases

import com.sportall.az.models.Category
import com.sportall.az.models.Drill
import com.sportall.az.repositories.DrillRepository
import com.sportall.az.repositories.HistoryRepository
import kotlinx.coroutines.runBlocking

class GetStatisticsUseCase(
    private val history: HistoryRepository,
    private val drills: DrillRepository
) {
    operator fun invoke(): StatisticsResult {
        val all = history.getAll()
        val avg = all.mapNotNull { it.stars?.toDouble() }.average().let { if (it.isNaN()) null else it }

        val drillIndex: Map<Int, Drill> = runBlocking { drills.getAll() }.associateBy { it.id }
        val byCat: Map<Category, Int> = all.mapNotNull { rec -> drillIndex[rec.drillId]?.category }.groupingBy { it }.eachCount()

        val mostUsed = all.groupingBy { it.drillId }.eachCount().entries.sortedByDescending { it.value }.map { it.key }

        return StatisticsResult(total = all.size, avgStars = avg, byCategory = byCat, mostUsed = mostUsed)
    }
}

