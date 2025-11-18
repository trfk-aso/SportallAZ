package com.sportall.az.ui.onboarding

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.CompleteOnboardingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class OnboardingState(
    val currentPage: Int = 0,
    val isCompleted: Boolean = false
)

class OnboardingViewModel(
    private val completeOnboarding: CompleteOnboardingUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun nextPage() {
        val nextPage = _state.value.currentPage + 1
        if (nextPage < 3) {
            _state.value = _state.value.copy(currentPage = nextPage)
        } else {
            finishOnboarding()
        }
    }

    fun skipOnboarding() {
        finishOnboarding()
    }

    private fun finishOnboarding() {
        completeOnboarding()
        _state.value = _state.value.copy(isCompleted = true)
    }
}
