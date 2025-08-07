package com.example.android_launcher.di

import androidx.room.Room
import com.example.android_launcher.MainViewModel
import com.example.android_launcher.data.local.AppsDao
import com.example.android_launcher.data.local.CalendarDao
import com.example.android_launcher.data.local.LauncherDatabase
import com.example.android_launcher.data.local.ListTypeConverter
import com.example.android_launcher.data.repository.AppsRepositoryImpl
import com.example.android_launcher.data.repository.CalendarRepositoryImpl
import com.example.android_launcher.domain.repository.AppsRepository
import com.example.android_launcher.domain.repository.CalendarRepository
import com.example.android_launcher.presentation.screens.home.SharedViewModel
import com.example.android_launcher.presentation.screens.home.home.apps.BlockingAppViewModel
import com.example.android_launcher.presentation.screens.home.home.calendar.CalendarViewModel
import com.example.android_launcher.presentation.screens.home.settings.SettingsViewModel
import com.example.android_launcher.presentation.screens.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
//
val appModule = module {
    single {
        Room.databaseBuilder(context = get(), klass = LauncherDatabase::class.java, name = "apps_db")
//            .addTypeConverter(ListTypeConverter())
            .fallbackToDestructiveMigration(false)
            .build()
    }

    single <AppsDao> {
        get< LauncherDatabase>().appsDao()
    }

    single <CalendarDao> {
        get<LauncherDatabase>().calendarDao()
    }

    single <AppsRepository> { AppsRepositoryImpl(get()) }
    single<CalendarRepository> { CalendarRepositoryImpl(get()) }

    viewModelOf(::CalendarViewModel)
    viewModelOf(::BlockingAppViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::SettingsViewModel)
    viewModel { SharedViewModel(get(),get()) }

}