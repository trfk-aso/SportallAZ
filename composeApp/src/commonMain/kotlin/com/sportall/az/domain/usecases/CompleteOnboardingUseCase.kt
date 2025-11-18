package com.sportall.az.domain.usecases

import com.sportall.az.repositories.PreferencesRepository

class CompleteOnboardingUseCase(private val prefs: PreferencesRepository) {
    operator fun invoke() {
        prefs.putBoolean("onboarding_completed", true)
    }
}
