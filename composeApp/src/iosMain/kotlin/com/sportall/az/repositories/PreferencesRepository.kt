package com.sportall.az.repositories

import platform.Foundation.NSUserDefaults

actual class PreferencesRepository {

    private val prefs = NSUserDefaults.standardUserDefaults

    actual fun putString(key: String, value: String) {
        prefs.setObject(value, forKey = key)
    }

    actual fun getString(key: String): String? =
        prefs.stringForKey(key)

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.setBool(value, forKey = key)
    }

    actual fun getBoolean(key: String): Boolean =
        prefs.boolForKey(key)

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
