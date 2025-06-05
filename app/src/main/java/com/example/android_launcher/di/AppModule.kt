package com.example.android_launcher.di

import com.example.android_launcher.presentation.screens.home.apps.AppsViewModel
import com.example.android_launcher.presentation.screens.home.calendar.CalendarViewModel
import com.example.android_launcher.presentation.screens.home.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
//
val appModule = module {
    viewModelOf(::CalendarViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::AppsViewModel)
}