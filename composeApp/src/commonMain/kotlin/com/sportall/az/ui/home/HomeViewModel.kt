package com.sportall.az.ui.home

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.FilterDrillsByCategoryUseCase
import com.sportall.az.domain.usecases.FilterDrillsByDifficultyUseCase
import com.sportall.az.domain.usecases.GetDrillsUseCase
import com.sportall.az.domain.usecases.GetFavoritesUseCase
import com.sportall.az.domain.usecases.GetStatisticsUseCase
import com.sportall.az.domain.usecases.LoadCategoriesUseCase
import com.sportall.az.domain.usecases.StatisticsResult
import com.sportall.az.models.Category
import com.sportall.az.models.Difficulty
import com.sportall.az.models.Drill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeState(
    val loading: Boolean = true,
    val drills: List<Drill> = emptyList(),
    val categories: List<Category> = emptyList(),
    val favorites: Set<Int> = emptySet(),
    val stats: StatisticsResult? = null,
    val error: String? = null
)

class HomeViewModel(
    private val getDrills: GetDrillsUseCase,
    private val loadCategories: LoadCategoriesUseCase,
    private val filterDrillsByCategory: FilterDrillsByCategoryUseCase,
    private val filterDrillsByDifficulty: FilterDrillsByDifficultyUseCase,
    private val getFavorites: GetFavoritesUseCase,
    private val getStatistics: GetStatisticsUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                val drills = getDrills()
                val categories = loadCategories()
                val favorites = getFavorites().toSet()
                val stats = getStatistics()

                HomeState(
                    loading = false,
                    drills = drills,
                    categories = categories,
                    favorites = favorites,
                    stats = stats
                )
            }.onSuccess { newState ->
                _state.value = newState
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }

    fun filterByCategory(category: Category?) {
        viewModelScope.launch {
            val list =
                if (category == null) getDrills()
                else filterDrillsByCategory(category)

            _state.value = _state.value.copy(drills = list)
        }
    }

    fun filterByDifficulty(level: Difficulty?) {
        viewModelScope.launch {
            val list =
                if (level == null) getDrills()
                else filterDrillsByDifficulty(level)

            _state.value = _state.value.copy(drills = list)
        }
    }
}
