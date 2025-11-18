package com.sportall.az.domain.usecases

import com.sportall.az.repositories.PreferencesRepository

class IsFirstLaunchUseCase(private val prefs: PreferencesRepository) {
    operator fun invoke(): Boolean = !prefs.getBoolean("onboarding_completed")
}
