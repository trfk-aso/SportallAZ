package com.sportall.az.repositories

interface IapRepository {
    fun unlockPremium()
    fun unlockExclusive()
    fun isPremiumUnlocked(): Boolean
    fun isExclusiveUnlocked(): Boolean
}

class DefaultIapRepository(
    private val prefs: PreferencesRepository
) : IapRepository {

    private val keyPremium = "iap_premium"
    private val keyExclusive = "iap_exclusive"

    override fun unlockPremium() { prefs.putBoolean(keyPremium, true) }

    override fun unlockExclusive() { prefs.putBoolean(keyExclusive, true) }

    override fun isPremiumUnlocked(): Boolean = prefs.getBoolean(keyPremium)

    override fun isExclusiveUnlocked(): Boolean = prefs.getBoolean(keyExclusive)
}

