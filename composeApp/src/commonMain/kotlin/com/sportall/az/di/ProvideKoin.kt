package com.sportall.az.di

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication

@Composable
fun ProvideKoin(content: @Composable () -> Unit) {
    KoinApplication(
        application = {
            modules(appModule)
        }
    ) {
        content()
    }
}
