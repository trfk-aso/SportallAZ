package com.sportall.az.models

import kotlinx.serialization.Serializable

@Serializable
data class HistoryRecord(
    val drillId: Int,
    val date: Long,
    val stars: Int? = null
)

