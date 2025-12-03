package com.sportall.az.ui.catalog

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.AddHistoryItemUseCase
import com.sportall.az.domain.usecases.GetDrillByIdUseCase
import com.sportall.az.domain.usecases.IsExclusiveUnlockedUseCase
import com.sportall.az.domain.usecases.IsFavoriteUseCase
import com.sportall.az.domain.usecases.ToggleFavoriteUseCase
import com.sportall.az.models.Drill
import com.sportall.az.models.HistoryRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class DrillDetailsState(
    val loading: Boolean = true,
    val drill: Drill? = null,
    val isFavorite: Boolean = false,
    val isExclusiveUnlocked: Boolean = false,
    val error: String? = null
)

class DrillDetailsViewModel(
    private val getDrillById: GetDrillByIdUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val isFavorite: IsFavoriteUseCase,
    private val addHistoryItem: AddHistoryItemUseCase,
    private val isExclusiveUnlocked: IsExclusiveUnlockedUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(DrillDetailsState())
    val state: StateFlow<DrillDetailsState> = _state

    private var currentId: Int? = null

    fun load(drillId: Int) {
        currentId = drillId
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { getDrillById(drillId) }
                .onSuccess { d ->
                    val fav = isFavorite(drillId)

                    _state.value = _state.value.copy(
                        loading = false,
                        drill = d,
                        isFavorite = fav,
                        isExclusiveUnlocked = isExclusiveUnlocked()
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(loading = false, error = e.message)
                }
        }
    }

    fun toggleFavorite() {
        val id = currentId ?: state.value.drill?.id ?: return
        toggleFavorite(id)

        val fav = isFavorite(id)
        _state.value = _state.value.copy(isFavorite = fav)
    }

    fun completeWithRating(stars: Int?) {
        val id = currentId ?: state.value.drill?.id ?: return
        addHistoryItem(HistoryRecord(drillId = id, date = Clock.System.now().toEpochMilliseconds(), stars = stars))
    }
}
