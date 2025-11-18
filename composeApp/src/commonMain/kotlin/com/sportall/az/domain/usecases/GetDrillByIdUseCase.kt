package com.sportall.az.domain.usecases

import com.sportall.az.models.Drill
import com.sportall.az.repositories.DrillRepository

class GetDrillByIdUseCase(private val drills: DrillRepository) {
    suspend operator fun invoke(id: Int): Drill? = drills.getById(id)
}

