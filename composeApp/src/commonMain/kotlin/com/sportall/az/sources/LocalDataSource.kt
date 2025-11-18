package com.sportall.az.sources

import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

interface LocalDataSource {
    suspend fun readText(path: String): String
}

class DefaultLocalDataSource : LocalDataSource {
    @OptIn(ExperimentalResourceApi::class, InternalResourceApi::class)
    override suspend fun readText(path: String): String {
        val bytes = readResourceBytes(path)
        return bytes.decodeToString()
    }
}

