package com.sportall.az.domain.usecases

import com.sportall.az.repositories.SearchRepository

class AddSearchQueryUseCase(private val search: SearchRepository) {
    operator fun invoke(query: String) = search.addQuery(query)
}

