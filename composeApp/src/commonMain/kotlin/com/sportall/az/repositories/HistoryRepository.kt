package com.sportall.az.repositories

import com.sportall.az.models.HistoryRecord
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

interface HistoryRepository {
    fun add(record: HistoryRecord)
    fun getAll(): List<HistoryRecord>
    fun clear()
}

class DefaultHistoryRepository(
    private val prefs: PreferencesRepository,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : HistoryRepository {

    private val key = "history"

    override fun add(record: HistoryRecord) {
        val current = getAll().toMutableList()
        current.add(record)
        save(current)
    }

    override fun getAll(): List<HistoryRecord> =
        prefs.getString(key)?.let {
            runCatching { json.decodeFromString(ListSerializer(HistoryRecord.serializer()), it) }
                .getOrDefault(emptyList())
        } ?: emptyList()

    override fun clear() {
        save(emptyList())
    }

    private fun save(list: List<HistoryRecord>) {
        prefs.putString(key, json.encodeToString(ListSerializer(HistoryRecord.serializer()), list))
    }
}

