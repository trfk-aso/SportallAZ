package com.sportall.az.core

suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
    try { Result.success(block()) } catch (t: Throwable) { Result.failure(t) }

