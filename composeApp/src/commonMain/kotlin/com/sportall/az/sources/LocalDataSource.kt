package com.sportall.az.sources

import com.sportall.az.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

interface LocalDataSource {
    suspend fun readText(path: String): String
}

class DefaultLocalDataSource : LocalDataSource {
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun readText(path: String): String {
        val bytes = Res.readBytes(path)
        return bytes.decodeToString()
    }
}

