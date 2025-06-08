package com.example.android_launcher.domain.repository

import com.example.android_launcher.domain.models.App

interface AppsRepository {
    fun getAllApps(): List<App>
    fun insertApps(apps: List<App>)
    fun getPinnedApps(): List<App>
    suspend fun blockApp(appId: Int)
    suspend fun pinApp(appId: Int)
    suspend fun hideApp(appId: Int)
    fun newAppInstalled(app: App)
    suspend fun removeUninstalled(packageName: String)
}