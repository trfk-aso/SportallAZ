package com.sportall.az.ui.history

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.ClearHistoryUseCase
import com.sportall.az.domain.usecases.GetDrillByIdUseCase
import com.sportall.az.domain.usecases.GetHistoryUseCase
import com.sportall.az.domain.usecases.GetStatisticsUseCase
import com.sportall.az.domain.usecases.IsExportUnlockedUseCase
import com.sportall.az.domain.usecases.IsWipeUnlockedUseCase
import com.sportall.az.domain.usecases.StatisticsResult
import com.sportall.az.domain.usecases.TimeFilter
import com.sportall.az.models.Drill
import com.sportall.az.models.HistoryRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate

data class HistoryItemWithDrill(
    val record: HistoryRecord,
    val drill: Drill?
)

data class GroupedHistory(
    val today: List<HistoryItemWithDrill> = emptyList(),
    val yesterday: List<HistoryItemWithDrill> = emptyList(),
    val earlier: List<HistoryItemWithDrill> = emptyList()
)

data class HistoryState(
    val loading: Boolean = true,
    val items: List<HistoryRecord> = emptyList(),
    val groupedHistory: GroupedHistory = GroupedHistory(),
    val statistics: StatisticsResult? = null,
    val selectedFilter: TimeFilter = TimeFilter.ALL_TIME,
    val exportUnlocked: Boolean = false,
    val wipeUnlocked: Boolean = false,
    val error: String? = null
)

class HistoryViewModel(
    private val getHistory: GetHistoryUseCase,
    private val getDrillById: GetDrillByIdUseCase,
    private val clearHistory: ClearHistoryUseCase,
    private val getStatistics: GetStatisticsUseCase,
    private val isExportUnlocked: IsExportUnlockedUseCase,
    private val isWipeUnlocked: IsWipeUnlockedUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                val records = getHistory()
                val itemsWithDrills = records.map { record ->
                    HistoryItemWithDrill(
                        record = record,
                        drill = getDrillById(record.drillId)
                    )
                }
                val grouped = groupByDate(itemsWithDrills)
                Triple(records, grouped, Pair(isExportUnlocked(), isWipeUnlocked()))
            }.onSuccess { (records, grouped, unlocks) ->
                _state.value = _state.value.copy(
                    loading = false,
                    items = records,
                    groupedHistory = grouped,
                    exportUnlocked = unlocks.first,
                    wipeUnlocked = unlocks.second
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }

    private fun groupByDate(items: List<HistoryItemWithDrill>): GroupedHistory {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val yesterday = LocalDate(today.year, today.monthNumber, today.dayOfMonth).let {
            // Simple yesterday calculation
            val daysBefore = it.dayOfMonth - 1
            if (daysBefore > 0) {
                LocalDate(it.year, it.monthNumber, daysBefore)
            } else {
                // Previous month logic (simplified)
                LocalDate(it.year, it.monthNumber - 1, 1)
            }
        }

        val todayItems = mutableListOf<HistoryItemWithDrill>()
        val yesterdayItems = mutableListOf<HistoryItemWithDrill>()
        val earlierItems = mutableListOf<HistoryItemWithDrill>()

        items.sortedByDescending { it.record.date }.forEach { item ->
            val itemDate = Instant.fromEpochMilliseconds(item.record.date)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date

            when {
                itemDate == today -> todayItems.add(item)
                itemDate == yesterday -> yesterdayItems.add(item)
                else -> earlierItems.add(item)
            }
        }

        return GroupedHistory(
            today = todayItems,
            yesterday = yesterdayItems,
            earlier = earlierItems
        )
    }

    fun loadStatistics(filter: TimeFilter = TimeFilter.ALL_TIME) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, selectedFilter = filter)
            runCatching { getStatistics(filter) }
                .onSuccess { stats ->
                    _state.value = _state.value.copy(loading = false, statistics = stats)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(loading = false, error = e.message)
                }
        }
    }

    fun setTimeFilter(filter: TimeFilter) {
        loadStatistics(filter)
    }

    fun clear() {
        clearHistory()
        load()
    }
}
