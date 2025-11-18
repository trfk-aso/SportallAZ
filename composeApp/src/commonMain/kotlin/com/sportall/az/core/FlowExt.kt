package com.sportall.az.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn

fun <T> Flow<T>.stateInViewModel(viewModel: BaseViewModel, initial: T): StateFlow<T> =
    this.stateIn(viewModel.viewModelScope, SharingStarted.Eagerly, initial)

fun <T> Flow<T>.launchInViewModel(viewModel: BaseViewModel) =
    this.launchIn(viewModel.viewModelScope)
