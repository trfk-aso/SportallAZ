package com.sportall.az.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual object DispatcherProvider {
    actual val Main: CoroutineDispatcher = Dispatchers.Main
    actual val IO: CoroutineDispatcher = Dispatchers.IO
}

