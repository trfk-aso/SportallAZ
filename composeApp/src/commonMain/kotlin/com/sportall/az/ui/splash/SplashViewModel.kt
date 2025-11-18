package com.sportall.az.ui.splash

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.IsFirstLaunchUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SplashState(
    val shouldShowOnboarding: Boolean? = null
)

class SplashViewModel(
    private val isFirstLaunch: IsFirstLaunchUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state

    init {
        checkFirstLaunch()
    }

    private fun checkFirstLaunch() {
        viewModelScope.launch {
            delay(1500) // 1.5 seconds splash duration
            val isFirst = isFirstLaunch()
            _state.value = SplashState(shouldShowOnboarding = isFirst)
        }
    }
}
