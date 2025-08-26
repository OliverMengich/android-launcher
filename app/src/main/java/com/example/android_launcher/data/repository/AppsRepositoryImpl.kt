package com.example.android_launcher.data.repository

import com.example.android_launcher.data.local.AppsDao
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.domain.models.BatteryInfo
import com.example.android_launcher.domain.repository.AppsRepository
import kotlinx.coroutines.flow.MutableStateFlow

class AppsRepositoryImpl(private val appsDao: AppsDao): AppsRepository {

    override val batteryInfo = MutableStateFlow<BatteryInfo?>(null)
//    override val batteryInfo: StateFlow<BatteryInfo?> = _batteryInfo.asStateFlow()

    override fun getAllApps(): List<App> {
        return appsDao.getAllApps()
    }

    override fun batteryInfoReceiver(batteryInfoVal: BatteryInfo) {
        batteryInfo.value = batteryInfoVal
    }

    override fun insertApps(apps: List<App>) {
        appsDao.insertApps(
            apps = apps
        )
    }

    override fun getPinnedApps(): List<App> {
        return appsDao.getPinnedApps()
    }

    override fun getHiddenApps(): List<App> {
        return appsDao.getHiddenApps()
    }

    override fun getBlockedApps(): List<App> {
        return appsDao.getBlockedApps()
    }

    override suspend fun blockUnblockApp(
        packageName: String,
        blocked: Int,
        blockReleaseDate: String?
    ) {
        appsDao.blockUnblockApp(packageName,blocked, blockReleaseDate = blockReleaseDate)
    }

    override suspend fun pinUnpinApp(packageName: String,pinned: Int) {
        appsDao.pinApp(packageName,pinned)
    }

    override suspend fun hideUnhideApp(packageName: String, hidden: Int) {
        appsDao.hideUnhideApp(packageName,hidden)
    }

    override fun newAppInstalled(app: App) {
        appsDao.insertApp(app)
    }

    override suspend fun removeUninstalled(packageName: String) {
        appsDao.deleteApp(packageName)
    }
}