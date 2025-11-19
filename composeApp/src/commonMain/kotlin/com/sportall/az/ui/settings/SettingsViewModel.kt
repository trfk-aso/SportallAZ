package com.sportall.az.ui.settings

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.IsExportUnlockedUseCase
import com.sportall.az.domain.usecases.IsWipeUnlockedUseCase
import com.sportall.az.domain.usecases.IsExclusiveUnlockedUseCase
import com.sportall.az.domain.usecases.PurchaseUnlockUseCase
import com.sportall.az.domain.usecases.WipeDataUseCase
import com.sportall.az.domain.usecases.ExportDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SettingsState(
    val exportUnlocked: Boolean = false,
    val wipeUnlocked: Boolean = false,
    val exclusiveUnlocked: Boolean = false
)

class SettingsViewModel(
    private val isExportUnlocked: IsExportUnlockedUseCase,
    private val isWipeUnlocked: IsWipeUnlockedUseCase,
    private val isExclusiveUnlocked: IsExclusiveUnlockedUseCase,
    private val purchaseUnlock: PurchaseUnlockUseCase,
    private val wipeDataUseCase: WipeDataUseCase,
    private val exportDataUseCase: ExportDataUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    fun refresh() {
        _state.value = SettingsState(
            exportUnlocked = isExportUnlocked(),
            wipeUnlocked = isWipeUnlocked(),
            exclusiveUnlocked = isExclusiveUnlocked()
        )
    }

    fun unlockExport() {
        purchaseUnlock.unlockExport()
        refresh()
    }

    fun unlockWipe() {
        purchaseUnlock.unlockWipe()
        refresh()
    }

    fun unlockExclusive() {
        purchaseUnlock.unlockExclusive()
        refresh()
    }

    fun wipeData() {
        wipeDataUseCase()
    }

    fun exportData(): String {
        return exportDataUseCase()
    }
}
