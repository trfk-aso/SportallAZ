package com.sportall.az.repositories

interface FavoritesRepository {
    fun toggleFavorite(id: Int)
    fun getFavorites(): List<Int>
}

class DefaultFavoritesRepository(
    private val prefs: PreferencesRepository
) : FavoritesRepository {

    private val KEY = "favorites"

    override fun toggleFavorite(id: Int) {
        val list = getFavorites().toMutableList()
        if (list.contains(id)) list.remove(id) else list.add(id)
        prefs.putIntList(KEY, list)
    }

    override fun getFavorites(): List<Int> =
        prefs.getIntList(KEY)
}
