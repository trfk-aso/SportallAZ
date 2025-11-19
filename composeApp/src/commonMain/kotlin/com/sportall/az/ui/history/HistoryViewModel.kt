package com.sportall.az.ui.history

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.ClearHistoryUseCase
import com.sportall.az.domain.usecases.GetHistoryUseCase
import com.sportall.az.domain.usecases.GetStatisticsUseCase
import com.sportall.az.domain.usecases.StatisticsResult
import com.sportall.az.domain.usecases.TimeFilter
import com.sportall.az.models.HistoryRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HistoryState(
    val loading: Boolean = true,
    val items: List<HistoryRecord> = emptyList(),
    val statistics: StatisticsResult? = null,
    val selectedFilter: TimeFilter = TimeFilter.ALL_TIME,
    val error: String? = null
)

class HistoryViewModel(
    private val getHistory: GetHistoryUseCase,
    private val clearHistory: ClearHistoryUseCase,
    private val getStatistics: GetStatisticsUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { getHistory() }
                .onSuccess { list -> _state.value = _state.value.copy(loading = false, items = list) }
                .onFailure { e -> _state.value = _state.value.copy(loading = false, error = e.message) }
        }
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
