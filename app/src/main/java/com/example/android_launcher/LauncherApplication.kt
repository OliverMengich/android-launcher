package com.example.android_launcher

import android.app.Application
import com.example.android_launcher.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LauncherApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@LauncherApplication)
            modules(appModule)
        }
    }
}