package com.sportall.az.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class BaseViewModel {
    protected val job = SupervisorJob()
    protected val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    val viewModelScope: CoroutineScope get() = scope

    open fun onCleared() { job.cancel() }
}
