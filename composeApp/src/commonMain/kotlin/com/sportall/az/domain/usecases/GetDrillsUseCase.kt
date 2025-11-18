package com.sportall.az.domain.usecases

import com.sportall.az.models.Drill
import com.sportall.az.repositories.DrillRepository

class GetDrillsUseCase(private val drills: DrillRepository) {
    suspend operator fun invoke(): List<Drill> = drills.getAll()
}

