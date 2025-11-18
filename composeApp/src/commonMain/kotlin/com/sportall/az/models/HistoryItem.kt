package com.sportall.az.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class HistoryItem(
    val drillId: String,
    val rating: Int,
    val timestamp: Instant
)
