package com.sportall.az.repositories

expect class PreferencesRepository() {
    fun putString(key: String, value: String)
    fun getString(key: String): String?

    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String): Boolean

    fun putStringList(key: String, list: List<String>)
    fun getStringList(key: String): List<String>

    fun putIntList(key: String, list: List<Int>)
    fun getIntList(key: String): List<Int>
}
