package com.sportall.az.domain.usecases

import com.sportall.az.models.Drill
import com.sportall.az.repositories.DrillRepository

class SearchDrillsUseCase(private val drills: DrillRepository) {
    suspend operator fun invoke(query: String): List<Drill> = drills.search(query)
}

