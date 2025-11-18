package com.sportall.az.di

import com.sportall.az.repositories.DefaultDrillRepository
import com.sportall.az.repositories.DefaultFavoritesRepository
import com.sportall.az.repositories.DefaultHistoryRepository
import com.sportall.az.repositories.DefaultIapRepository
import com.sportall.az.repositories.DefaultSearchRepository
import com.sportall.az.repositories.DrillRepository
import com.sportall.az.repositories.FavoritesRepository
import com.sportall.az.repositories.HistoryRepository
import com.sportall.az.repositories.IapRepository
import com.sportall.az.repositories.PreferencesRepository
import com.sportall.az.repositories.SearchRepository
import com.sportall.az.sources.DefaultLocalDataSource
import com.sportall.az.sources.LocalDataSource
import com.sportall.az.domain.usecases.*
import org.koin.dsl.module

val appModule = module {
    single { PreferencesRepository() }
    single<LocalDataSource> { DefaultLocalDataSource() }
    single<DrillRepository> { DefaultDrillRepository(get()) }
    single<FavoritesRepository> { DefaultFavoritesRepository(get()) }
    single<HistoryRepository> { DefaultHistoryRepository(get()) }
    single<SearchRepository> { DefaultSearchRepository(get()) }
    single<IapRepository> { DefaultIapRepository(get()) }

    factory { GetDrillsUseCase(get()) }
    factory { GetDrillByIdUseCase(get()) }
    factory { SearchDrillsUseCase(get()) }
    factory { FilterDrillsByCategoryUseCase(get()) }
    factory { FilterDrillsByDifficultyUseCase(get()) }
    factory { LoadCategoriesUseCase(get()) }

    factory { ToggleFavoriteUseCase(get()) }
    factory { GetFavoritesUseCase(get()) }

    factory { AddHistoryItemUseCase(get()) }
    factory { GetHistoryUseCase(get()) }
    factory { ClearHistoryUseCase(get()) }

    factory { AddSearchQueryUseCase(get()) }
    factory { GetSearchHistoryUseCase(get()) }

    factory { GetStatisticsUseCase(get(), get()) }

    factory { PurchaseUnlockUseCase(get()) }
    factory { IsExportUnlockedUseCase(get()) }
    factory { IsWipeUnlockedUseCase(get()) }
    factory { IsExclusiveUnlockedUseCase(get()) }

    factory { IsFirstLaunchUseCase(get()) }
    factory { CompleteOnboardingUseCase(get()) }

    factory { com.sportall.az.ui.splash.SplashViewModel(get()) }
    factory { com.sportall.az.ui.onboarding.OnboardingViewModel(get()) }
    factory { com.sportall.az.ui.home.HomeViewModel(get(), get(), get(), get(), get(), get()) }
    factory { com.sportall.az.ui.search.SearchViewModel(get(), get(), get(), get()) }
    factory { com.sportall.az.ui.catalog.DrillDetailsViewModel(get(), get(), get()) }
    factory { com.sportall.az.ui.favorites.FavoritesViewModel(get(), get()) }
    factory { com.sportall.az.ui.history.HistoryViewModel(get(), get()) }
    factory { com.sportall.az.ui.settings.SettingsViewModel(get(), get(), get(), get()) }
    factory { com.sportall.az.ui.practice.PracticeViewModel() }
}

