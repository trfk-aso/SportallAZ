package com.sportall.az.repositories

interface SearchRepository {
    fun addQuery(query: String)
    fun getHistory(): List<String>
}

class DefaultSearchRepository(
    private val prefs: PreferencesRepository
) : SearchRepository {

    private val KEY = "search_history"
    private val MAX_ITEMS = 20

    override fun addQuery(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return

        val list = getHistory().toMutableList()
        list.remove(q)
        list.add(0, q)

        prefs.putStringList(KEY, list.take(MAX_ITEMS))
    }

    override fun getHistory(): List<String> =
        prefs.getStringList(KEY)
}
