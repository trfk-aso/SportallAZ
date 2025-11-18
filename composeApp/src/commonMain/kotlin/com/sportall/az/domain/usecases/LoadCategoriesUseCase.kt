package com.sportall.az.domain.usecases

import com.sportall.az.models.Category
import com.sportall.az.repositories.DrillRepository

class LoadCategoriesUseCase(private val drills: DrillRepository) {
    suspend operator fun invoke(): List<Category> = drills.getCategories()
}

