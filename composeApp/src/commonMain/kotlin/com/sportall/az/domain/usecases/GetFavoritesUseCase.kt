package com.sportall.az.domain.usecases

import com.sportall.az.repositories.FavoritesRepository

class GetFavoritesUseCase(private val favorites: FavoritesRepository) {
    operator fun invoke(): List<Int> = favorites.getFavorites()
}

