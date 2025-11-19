package com.sportall.az.ui.search

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.AddSearchQueryUseCase
import com.sportall.az.domain.usecases.GetDrillsUseCase
import com.sportall.az.domain.usecases.GetFavoritesUseCase
import com.sportall.az.domain.usecases.GetSearchHistoryUseCase
import com.sportall.az.domain.usecases.IsExclusiveUnlockedUseCase
import com.sportall.az.domain.usecases.SearchDrillsUseCase
import com.sportall.az.models.Category
import com.sportall.az.models.Difficulty
import com.sportall.az.models.Drill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SearchState(
    val query: String = "",
    val results: List<Drill> = emptyList(),
    val allDrills: List<Drill> = emptyList(),
    val history: List<String> = emptyList(),
    val favorites: Set<Int> = emptySet(),
    val isExclusiveUnlocked: Boolean = false,
    val selectedCategory: Category? = null,
    val selectedDifficulty: Difficulty? = null,
    val loading: Boolean = false,
    val error: String? = null
)

class SearchViewModel(
    private val searchDrills: SearchDrillsUseCase,
    private val addSearchQuery: AddSearchQueryUseCase,
    private val getSearchHistory: GetSearchHistoryUseCase,
    private val getDrills: GetDrillsUseCase,
    private val getFavorites: GetFavoritesUseCase,
    private val isExclusiveUnlocked: IsExclusiveUnlockedUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state

    init {
        loadAllDrills()
        loadHistory()
        loadFavorites()
        loadExclusiveUnlockStatus()
    }

    private fun loadAllDrills() {
        viewModelScope.launch {
            runCatching {
                getDrills()
            }.onSuccess { drills ->
                _state.value = _state.value.copy(allDrills = drills, results = drills)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _state.value = _state.value.copy(query = newQuery)
        if (newQuery.isEmpty()) {
            applyFilters()
        }
    }

    fun submitSearch() {
        val q = _state.value.query
        if (q.isBlank()) {
            applyFilters()
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                addSearchQuery(q)
                searchDrills(q)
            }.onSuccess { results ->
                val filtered = filterResults(results)
                _state.value = _state.value.copy(loading = false, results = filtered)
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }

    fun clearSearch() {
        _state.value = _state.value.copy(query = "")
        applyFilters()
    }

    fun selectCategory(category: Category?) {
        _state.value = _state.value.copy(selectedCategory = category)
        applyFilters()
    }

    fun selectDifficulty(difficulty: Difficulty?) {
        _state.value = _state.value.copy(selectedDifficulty = difficulty)
        applyFilters()
    }

    fun removeHistoryItem(query: String) {
        val updatedHistory = _state.value.history.filter { it != query }
        _state.value = _state.value.copy(history = updatedHistory)
    }

    fun selectHistoryQuery(query: String) {
        _state.value = _state.value.copy(query = query)
        submitSearch()
    }

    fun backToAllDrills() {
        _state.value = _state.value.copy(
            query = "",
            selectedCategory = null,
            selectedDifficulty = null
        )
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = filterResults(_state.value.allDrills)
        _state.value = _state.value.copy(results = filtered)
    }

    private fun filterResults(drills: List<Drill>): List<Drill> {
        var filtered = drills

        _state.value.selectedCategory?.let { category ->
            filtered = filtered.filter { it.category == category }
        }

        _state.value.selectedDifficulty?.let { difficulty ->
            filtered = filtered.filter { it.difficulty == difficulty }
        }

        return filtered
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(history = getSearchHistory())
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _state.value = _state.value.copy(favorites = getFavorites().toSet())
        }
    }

    private fun loadExclusiveUnlockStatus() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExclusiveUnlocked = isExclusiveUnlocked())
        }
    }
}
