package com.sportall.az.ui.history

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.ClearHistoryUseCase
import com.sportall.az.domain.usecases.GetHistoryUseCase
import com.sportall.az.models.HistoryRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HistoryState(
    val loading: Boolean = true,
    val items: List<HistoryRecord> = emptyList(),
    val error: String? = null
)

class HistoryViewModel(
    private val getHistory: GetHistoryUseCase,
    private val clearHistory: ClearHistoryUseCase
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

    fun clear() {
        clearHistory()
        load()
    }
}
