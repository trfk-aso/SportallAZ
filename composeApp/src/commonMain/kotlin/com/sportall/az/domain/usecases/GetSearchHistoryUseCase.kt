package com.sportall.az.domain.usecases

import com.sportall.az.repositories.SearchRepository

class GetSearchHistoryUseCase(private val search: SearchRepository) {
    operator fun invoke(): List<String> = search.getHistory()
}

