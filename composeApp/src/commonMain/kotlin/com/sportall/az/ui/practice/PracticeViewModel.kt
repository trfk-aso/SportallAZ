package com.sportall.az.ui.practice

import com.sportall.az.core.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PracticeState(
    val stepsCount: Int = 0,
    val completedSteps: Set<Int> = emptySet()
)

class PracticeViewModel : BaseViewModel() {

    private val _state = MutableStateFlow(PracticeState())
    val state: StateFlow<PracticeState> = _state

    fun start(stepsCount: Int) { _state.value = PracticeState(stepsCount = stepsCount, completedSteps = emptySet()) }

    fun completeStep(index: Int) { _state.value = _state.value.copy(completedSteps = _state.value.completedSteps + index) }

    fun reset() { _state.value = PracticeState() }
}
