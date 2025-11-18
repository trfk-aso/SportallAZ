package com.sportall.az

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sportall.az.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Android context for platform-specific implementations
        initAndroid(application)
        setContent {
            App()
        }
    }
}
