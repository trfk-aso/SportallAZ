package com.sportall.az.repositories

import android.content.Context
import com.sportall.az.androidContext

actual class PreferencesRepository {

    private val prefs = androidContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getString(key: String): String? =
        prefs.getString(key, null)

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    actual fun getBoolean(key: String): Boolean =
        prefs.getBoolean(key, false)

    actual fun putStringList(key: String, list: List<String>) {
        putString(key, list.joinToString("||"))
    }

    actual fun getStringList(key: String): List<String> =
        getString(key)?.split("||") ?: emptyList()

    actual fun putIntList(key: String, list: List<Int>) {
        putString(key, list.joinToString(","))
    }

    actual fun getIntList(key: String): List<Int> =
        getString(key)?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
}

