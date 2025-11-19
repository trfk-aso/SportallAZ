package com.sportall.az.ui.favorites

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.GetDrillsUseCase
import com.sportall.az.domain.usecases.GetFavoritesUseCase
import com.sportall.az.domain.usecases.IsExclusiveUnlockedUseCase
import com.sportall.az.models.Drill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FavoritesState(
    val loading: Boolean = true,
    val drills: List<Drill> = emptyList(),
    val isExclusiveUnlocked: Boolean = false,
    val error: String? = null
)

class FavoritesViewModel(
    private val getFavorites: GetFavoritesUseCase,
    private val getDrills: GetDrillsUseCase,
    private val isExclusiveUnlocked: IsExclusiveUnlockedUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                val favIds = getFavorites().toSet()
                val drills = getDrills().filter { it.id in favIds }
                val exclusiveUnlocked = isExclusiveUnlocked()
                Pair(drills, exclusiveUnlocked)
            }.onSuccess { (drills, exclusiveUnlocked) ->
                _state.value = _state.value.copy(
                    loading = false,
                    drills = drills,
                    isExclusiveUnlocked = exclusiveUnlocked
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }
}
