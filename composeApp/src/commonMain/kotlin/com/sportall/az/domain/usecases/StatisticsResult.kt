package com.sportall.az.domain.usecases

import com.sportall.az.models.Category

data class StatisticsResult(
    val total: Int,
    val avgStars: Double?,
    val byCategory: Map<Category, Int>,
    val mostUsed: List<Int>
)

