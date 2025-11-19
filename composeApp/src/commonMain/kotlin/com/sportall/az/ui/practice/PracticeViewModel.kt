package com.sportall.az.ui.practice

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.AddHistoryItemUseCase
import com.sportall.az.models.HistoryRecord
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

enum class TimerState {
    IDLE, RUNNING, PAUSED, COMPLETED
}

data class PracticeState(
    val drillId: Int? = null,
    val selectedDurationMinutes: Int = 5,
    val remainingSeconds: Int = 300, // 5 min default
    val timerState: TimerState = TimerState.IDLE,
    val showRatingDialog: Boolean = false
)

class PracticeViewModel(
    private val addHistoryItem: AddHistoryItemUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(PracticeState())
    val state: StateFlow<PracticeState> = _state

    private var timerJob: Job? = null

    fun initialize(drillId: Int) {
        _state.value = _state.value.copy(drillId = drillId)
    }

    fun selectDuration(minutes: Int) {
        if (_state.value.timerState == TimerState.IDLE) {
            _state.value = _state.value.copy(
                selectedDurationMinutes = minutes,
                remainingSeconds = minutes * 60
            )
        }
    }

    fun startTimer() {
        if (_state.value.timerState != TimerState.IDLE) return

        _state.value = _state.value.copy(timerState = TimerState.RUNNING)
        timerJob = viewModelScope.launch {
            while (_state.value.remainingSeconds > 0 && _state.value.timerState == TimerState.RUNNING) {
                delay(1000)
                val newRemaining = (_state.value.remainingSeconds - 1).coerceAtLeast(0)
                _state.value = _state.value.copy(remainingSeconds = newRemaining)

                if (newRemaining == 0) {
                    _state.value = _state.value.copy(timerState = TimerState.COMPLETED)
                }
            }
        }
    }

    fun pauseTimer() {
        _state.value = _state.value.copy(timerState = TimerState.PAUSED)
        timerJob?.cancel()
    }

    fun resumeTimer() {
        if (_state.value.timerState == TimerState.PAUSED && _state.value.remainingSeconds > 0) {
            _state.value = _state.value.copy(timerState = TimerState.RUNNING)
            startTimer()
        }
    }

    fun completePractice() {
        timerJob?.cancel()
        _state.value = _state.value.copy(showRatingDialog = true)
    }

    fun saveToHistory(rating: Int?) {
        val drillId = _state.value.drillId ?: return
        addHistoryItem(
            HistoryRecord(
                drillId = drillId,
                date = Clock.System.now().toEpochMilliseconds(),
                stars = rating
            )
        )
        _state.value = _state.value.copy(showRatingDialog = false)
    }

    fun cancelRating() {
        _state.value = _state.value.copy(showRatingDialog = false)
    }

    fun reset() {
        timerJob?.cancel()
        _state.value = PracticeState()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
