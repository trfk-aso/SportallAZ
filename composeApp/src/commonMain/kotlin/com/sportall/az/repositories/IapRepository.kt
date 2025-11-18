package com.sportall.az.repositories

interface IapRepository {
    fun unlockExport()
    fun unlockWipe()
    fun unlockExclusive()
    fun isExportUnlocked(): Boolean
    fun isWipeUnlocked(): Boolean
    fun isExclusiveUnlocked(): Boolean
}

class DefaultIapRepository(
    private val prefs: PreferencesRepository
) : IapRepository {

    private val keyExport = "iap_export"
    private val keyWipe = "iap_wipe"
    private val keyExclusive = "iap_exclusive"

    override fun unlockExport() { prefs.putBoolean(keyExport, true) }

    override fun unlockWipe() { prefs.putBoolean(keyWipe, true) }

    override fun unlockExclusive() { prefs.putBoolean(keyExclusive, true) }

    override fun isExportUnlocked(): Boolean = prefs.getBoolean(keyExport)

    override fun isWipeUnlocked(): Boolean = prefs.getBoolean(keyWipe)

    override fun isExclusiveUnlocked(): Boolean = prefs.getBoolean(keyExclusive)
}

