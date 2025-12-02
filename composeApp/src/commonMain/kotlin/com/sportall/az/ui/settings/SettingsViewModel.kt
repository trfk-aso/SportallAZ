package com.sportall.az.ui.settings

import com.sportall.az.core.BaseViewModel
import com.sportall.az.domain.usecases.IsExportUnlockedUseCase
import com.sportall.az.domain.usecases.IsWipeUnlockedUseCase
import com.sportall.az.domain.usecases.IsExclusiveUnlockedUseCase
import com.sportall.az.domain.usecases.PurchaseUnlockUseCase
import com.sportall.az.domain.usecases.WipeDataUseCase
import com.sportall.az.domain.usecases.ExportDataUseCase
import com.sportall.az.export.BuildExportPayloadUseCase
import com.sportall.az.export.ExportResult
import com.sportall.az.export.ExportViewer
import com.sportall.az.export.PdfExporter
import com.sportall.az.iap.PurchaseResult
import com.sportall.az.iap.createIAPManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
    private val buildExportPayloadUseCase: BuildExportPayloadUseCase,
    private val pdfExporter: PdfExporter,
    private val exportViewer: ExportViewer
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

    fun restorePurchases() {
        viewModelScope.launch {
            val iap = createIAPManager()
            val result = iap.restorePurchases()

            when (result) {
                is PurchaseResult.Success -> {
                    println("Purchases restored!")
                }
                is PurchaseResult.Error -> println("Restore error: ${result.message}")
                is PurchaseResult.Cancelled -> println("Restore cancelled")
            }
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            val payload = buildExportPayloadUseCase()
            when (val result = pdfExporter.export(payload)) {
                is ExportResult.Ok -> exportViewer.view(result.location)
                is ExportResult.Error -> println("Export error: ${result.message}")
            }
        }
    }
}
