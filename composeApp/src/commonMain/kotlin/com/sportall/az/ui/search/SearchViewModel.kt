package com.sportall.az.ui.search

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.AddSearchQueryUseCase
import com.sportall.az.domain.usecases.GetDrillsUseCase
import com.sportall.az.domain.usecases.GetSearchHistoryUseCase
import com.sportall.az.domain.usecases.SearchDrillsUseCase
import com.sportall.az.models.Drill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SearchState(
    val query: String = "",
    val results: List<Drill> = emptyList(),
    val history: List<String> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class SearchViewModel(
    private val searchDrills: SearchDrillsUseCase,
    private val addSearchQuery: AddSearchQueryUseCase,
    private val getSearchHistory: GetSearchHistoryUseCase,
    private val getDrills: GetDrillsUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state

    fun onQueryChange(newQuery: String) {
        _state.value = _state.value.copy(query = newQuery)
    }

    fun submitSearch() {
        val q = _state.value.query
        if (q.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                addSearchQuery(q)
                searchDrills(q)
            }.onSuccess { results ->
                _state.value = _state.value.copy(loading = false, results = results)
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(history = getSearchHistory())
        }
    }
}
