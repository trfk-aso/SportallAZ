package com.sportall.az.ui.settings

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.IsExclusiveUnlockedUseCase
import com.sportall.az.domain.usecases.IsPremiumUnlockedUseCase
import com.sportall.az.domain.usecases.PurchaseUnlockUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SettingsState(
    val premiumUnlocked: Boolean = false,
    val exclusiveUnlocked: Boolean = false
)

class SettingsViewModel(
    private val isPremiumUnlocked: IsPremiumUnlockedUseCase,
    private val isExclusiveUnlocked: IsExclusiveUnlockedUseCase,
    private val purchaseUnlock: PurchaseUnlockUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    fun refresh() {
        _state.value = SettingsState(
            premiumUnlocked = isPremiumUnlocked(),
            exclusiveUnlocked = isExclusiveUnlocked()
        )
    }

    fun unlockPremium() {
        purchaseUnlock.unlockPremium()
        refresh()
    }

    fun unlockExclusive() {
        purchaseUnlock.unlockExclusive()
        refresh()
    }
}
