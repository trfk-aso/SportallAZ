package com.sportall.az.models

import kotlinx.serialization.Serializable

@Serializable
data class Favorites(
    val drillIds: List<String> = emptyList()
)