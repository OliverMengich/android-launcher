package com.example.android_launcher.di

import androidx.room.Room
import com.example.android_launcher.data.local.AppsDao
import com.example.android_launcher.data.local.AppsDatabase
import com.example.android_launcher.data.repository.AppsRepositoryImpl
import com.example.android_launcher.domain.repository.AppsRepository
import com.example.android_launcher.presentation.screens.home.SharedViewModel
import com.example.android_launcher.presentation.screens.home.apps.BlockedAppViewModel
import com.example.android_launcher.presentation.screens.home.calendar.CalendarViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
//
val appModule = module {
    single {
        Room.databaseBuilder(context = get(), klass = AppsDatabase::class.java, name = "apps_db")
            .build()
    }

    single <AppsDao> {
        get<AppsDatabase>().appsDao()
    }

    single <AppsRepository> { AppsRepositoryImpl(get()) }

    viewModelOf(::CalendarViewModel)
    viewModelOf(::BlockedAppViewModel)
    viewModel { SharedViewModel(get(),get()) }

}