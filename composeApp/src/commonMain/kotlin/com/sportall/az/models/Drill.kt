package com.sportall.az.models

import kotlinx.serialization.Serializable

@Serializable
data class Drill(
    val id: String,
    val name: String,
    val category: Category,
    val difficulty: Int,
    val steps: List<String>,
    val recommendation: String? = null,
    val isPremium: Boolean = false,
    val image: String? = null
)