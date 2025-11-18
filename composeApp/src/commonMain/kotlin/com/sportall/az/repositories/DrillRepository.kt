package com.sportall.az.repositories

import com.sportall.az.models.Category
import com.sportall.az.models.Drill
import com.sportall.az.models.Difficulty
import com.sportall.az.sources.LocalDataSource
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.concurrent.Volatile

interface DrillRepository {
    suspend fun getAll(): List<Drill>
    suspend fun getById(id: Int): Drill?
    suspend fun search(query: String): List<Drill>
    suspend fun filterByCategory(category: Category): List<Drill>
    suspend fun filterByDifficulty(level: Difficulty): List<Drill>
    suspend fun getCategories(): List<Category>
    fun isExclusive(drill: Drill): Boolean
}

class DefaultDrillRepository(
    private val dataSource: LocalDataSource,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : DrillRepository {

    @Volatile
    private var cache: List<Drill>? = null

    private suspend fun ensureCache(): List<Drill> {
        cache?.let { return it }
        val text = dataSource.readText("files/drills/drills.json")
        val list = json.decodeFromString(ListSerializer(Drill.serializer()), text)
        cache = list
        return list
    }

    override suspend fun getAll(): List<Drill> = ensureCache()

    override suspend fun getById(id: Int): Drill? = ensureCache().firstOrNull { it.id == id }

    override suspend fun search(query: String): List<Drill> {
        if (query.isBlank()) return ensureCache()
        val q = query.trim()
        return ensureCache().filter { d ->
            d.name.contains(q, ignoreCase = true) ||
                d.steps.any { it.contains(q, ignoreCase = true) }
        }
    }

    override suspend fun filterByCategory(category: Category): List<Drill> =
        ensureCache().filter { it.category == category }

    override suspend fun filterByDifficulty(level: Difficulty): List<Drill> =
        ensureCache().filter { it.difficulty == level }

    override suspend fun getCategories(): List<Category> = ensureCache()
        .map { it.category }
        .distinct()

    override fun isExclusive(drill: Drill): Boolean = drill.isExclusive
}

