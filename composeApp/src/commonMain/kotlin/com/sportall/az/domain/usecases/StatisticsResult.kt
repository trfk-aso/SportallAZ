package com.sportall.az.domain.usecases

import com.sportall.az.models.Category

data class StatisticsResult(
    val total: Int,
    val avgStars: Double?,
    val byCategory: Map<Category, Int>,
    val mostUsed: List<DrillUsageStats>
)

data class DrillUsageStats(
    val drillId: Int,
    val drillName: String,
    val usageCount: Int,
    val avgRating: Double?
)

data class CategoryStats(
    val category: Category,
    val count: Int
)

