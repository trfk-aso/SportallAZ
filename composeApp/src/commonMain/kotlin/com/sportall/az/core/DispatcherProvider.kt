package com.sportall.az.core

import kotlinx.coroutines.CoroutineDispatcher

expect object DispatcherProvider {
    val Main: CoroutineDispatcher
    val IO: CoroutineDispatcher
}

