package com.sportall.az.models

import kotlinx.serialization.Serializable

@Serializable
data class Drill(
    val id: Int,
    val name: String,
    val category: Category,
    val difficulty: Difficulty,
    val estimatedTime: Int,
    val setup: List<String>,
    val execution: List<String>,
    val coachingPoints: List<String>,
    val visualDescription: String,
    val steps: List<String>,
    val recommendation: String? = null,
    val isExclusive: Boolean = false
)