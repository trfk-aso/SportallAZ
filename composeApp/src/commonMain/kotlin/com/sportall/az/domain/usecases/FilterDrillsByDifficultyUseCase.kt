package com.sportall.az.domain.usecases

import com.sportall.az.models.Difficulty
import com.sportall.az.models.Drill
import com.sportall.az.repositories.DrillRepository

class FilterDrillsByDifficultyUseCase(private val drills: DrillRepository) {
    suspend operator fun invoke(level: Difficulty): List<Drill> = drills.filterByDifficulty(level)
}

