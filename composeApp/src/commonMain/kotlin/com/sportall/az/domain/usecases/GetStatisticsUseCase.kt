package com.sportall.az.domain.usecases

import com.sportall.az.models.Category
import com.sportall.az.models.Drill
import com.sportall.az.models.HistoryRecord
import com.sportall.az.repositories.DrillRepository
import com.sportall.az.repositories.HistoryRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

enum class TimeFilter {
    ALL_TIME,
    SEVEN_DAYS,
    THIRTY_DAYS
}

class GetStatisticsUseCase(
    private val history: HistoryRepository,
    private val drills: DrillRepository
) {
    suspend operator fun invoke(timeFilter: TimeFilter = TimeFilter.ALL_TIME): StatisticsResult {
        val allRecords = history.getAll()
        val filteredRecords = filterByTime(allRecords, timeFilter)

        val avg = filteredRecords.mapNotNull { it.stars?.toDouble() }
            .average()
            .let { if (it.isNaN()) null else it }

        val drillIndex: Map<Int, Drill> = drills.getAll().associateBy { it.id }
        val byCat: Map<Category, Int> = filteredRecords
            .mapNotNull { rec -> drillIndex[rec.drillId]?.category }
            .groupingBy { it }
            .eachCount()

        val usageCounts = filteredRecords.groupingBy { it.drillId }.eachCount()
        val drillRatings = filteredRecords
            .filter { it.stars != null }
            .groupBy { it.drillId }
            .mapValues { (_, records) ->
                records.mapNotNull { it.stars?.toDouble() }.average()
            }

        val mostUsed = usageCounts.entries
            .sortedByDescending { it.value }
            .take(10)
            .mapNotNull { (drillId, count) ->
                val drill = drillIndex[drillId] ?: return@mapNotNull null
                DrillUsageStats(
                    drillId = drillId,
                    drillName = drill.name,
                    usageCount = count,
                    avgRating = drillRatings[drillId]
                )
            }

        return StatisticsResult(
            total = filteredRecords.size,
            avgStars = avg,
            byCategory = byCat,
            mostUsed = mostUsed
        )
    }

    private fun filterByTime(records: List<HistoryRecord>, filter: TimeFilter): List<HistoryRecord> {
        if (filter == TimeFilter.ALL_TIME) return records

        val now = Clock.System.now()
        val cutoff = when (filter) {
            TimeFilter.SEVEN_DAYS -> now.minus(7.days)
            TimeFilter.THIRTY_DAYS -> now.minus(30.days)
            TimeFilter.ALL_TIME -> Instant.DISTANT_PAST
        }

        return records.filter { Instant.fromEpochMilliseconds(it.date) >= cutoff }
    }
}

