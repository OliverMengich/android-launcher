package com.example.android_launcher.data.repository

import com.example.android_launcher.data.local.AppsDao
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.domain.repository.AppsRepository

class AppsRepositoryImpl(private val appsDao: AppsDao): AppsRepository {
    override fun getAllApps(): List<App> {
        return appsDao.getAllApps()
    }

    override fun insertApps(apps: List<App>) {
        appsDao.insertApps(
            apps = apps
        )
    }

    override fun getPinnedApps(): List<App> {
        return appsDao.getPinnedApps()
    }

    override suspend fun blockApp(appId: Int) {
        appsDao.blockApp(appId)
    }

    override suspend fun pinApp(appId: Int) {
        appsDao.pinApp(appId)
    }

    override suspend fun hideApp(appId: Int) {
        appsDao.hideApp(appId)
    }

    override fun newAppInstalled(app: App) {
        appsDao.insertApp(app)
    }

    override suspend fun removeUninstalled(packageName: String) {
        appsDao.deleteApp(packageName)
    }
}