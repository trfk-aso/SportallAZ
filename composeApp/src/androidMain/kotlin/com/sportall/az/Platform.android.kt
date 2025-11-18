package com.sportall.az

import android.app.Application
import android.os.Build

lateinit var androidContext: Application
    private set

fun initAndroid(app: Application) {
    androidContext = app
}

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.RELEASE}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
