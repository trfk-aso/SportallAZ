package com.sportall.az.repositories

import com.sportall.az.iap.IAPProductIds

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

    private val keyExport = IAPProductIds.EXPORT
    private val keyWipe = IAPProductIds.WIPE
    private val keyExclusive = IAPProductIds.EXCLUSIVE

    override fun unlockExport() { prefs.putBoolean(keyExport, true) }

    override fun unlockWipe() { prefs.putBoolean(keyWipe, true) }

    override fun unlockExclusive() {
        println("SAVE_EXCLUSIVE: saving true")
        prefs.putBoolean(keyExclusive, true)
        println("CHECK_EXCLUSIVE_AFTER_SAVE: " + prefs.getBoolean(keyExclusive))
    }

    override fun isExportUnlocked(): Boolean = prefs.getBoolean(keyExport)

    override fun isWipeUnlocked(): Boolean = prefs.getBoolean(keyWipe)

    override fun isExclusiveUnlocked(): Boolean {
        val value = prefs.getBoolean(keyExclusive)
        println("READ_EXCLUSIVE: $value")
        return value
    }
}


