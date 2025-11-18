package com.sportall.az.models

import kotlinx.serialization.Serializable

@Serializable
data class LapState(
    val index: Int,
    val isCompleted: Boolean
)
