package com.sportall.az.domain.usecases

import com.sportall.az.repositories.FavoritesRepository

class ToggleFavoriteUseCase(private val favorites: FavoritesRepository) {
    operator fun invoke(id: Int) = favorites.toggleFavorite(id)
}

class IsFavoriteUseCase(private val favorites: FavoritesRepository) {
    operator fun invoke(id: Int): Boolean = favorites.getFavorites().contains(id)
}
