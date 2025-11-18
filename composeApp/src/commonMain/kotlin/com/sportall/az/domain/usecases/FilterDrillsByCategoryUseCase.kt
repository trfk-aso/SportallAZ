package com.sportall.az.domain.usecases

import com.sportall.az.models.Category
import com.sportall.az.models.Drill
import com.sportall.az.repositories.DrillRepository

class FilterDrillsByCategoryUseCase(private val drills: DrillRepository) {
    suspend operator fun invoke(category: Category): List<Drill> = drills.filterByCategory(category)
}

