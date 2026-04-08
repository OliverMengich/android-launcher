package com.planara.android_launcher.di

import com.planara.android_launcher.MainViewModel
import com.planara.android_launcher.data.LocalManagerImpl
import com.planara.android_launcher.data.repository.AppsRepositoryImpl
import com.planara.android_launcher.data.repository.CalendarRepositoryImpl
import com.planara.android_launcher.domain.repository.AppsRepository
import com.planara.android_launcher.domain.repository.CalendarRepository
import com.planara.android_launcher.presentation.screens.home.SharedViewModel
import com.planara.android_launcher.presentation.screens.home.home.apps.BlockingAppViewModel
import com.planara.android_launcher.presentation.screens.home.home.calendar.CalendarViewModel
import com.planara.android_launcher.presentation.screens.home.settings.SettingsViewModel
import com.planara.android_launcher.presentation.screens.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
val appModule = module {
    single {
        LocalManagerImpl
    }

    single <AppsRepository> { AppsRepositoryImpl(context=get()) }
    single<CalendarRepository> { CalendarRepositoryImpl(get()) }

    viewModelOf(::CalendarViewModel)
    viewModelOf(::BlockingAppViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::SettingsViewModel)
    viewModel { SharedViewModel(get(),get()) }

}