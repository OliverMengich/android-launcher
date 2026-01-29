package com.example.android_launcher.domain.repository

import com.example.android_launcher.domain.models.App
import com.example.android_launcher.domain.models.BatteryInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

interface AppsRepository {
    val batteryInfo: MutableStateFlow<BatteryInfo?>
    val refetchAppsFlow: MutableStateFlow<Boolean?>
    fun getAllApps(): Flow<List<App>>
    fun insertApps(apps: List<App>)
    fun getPinnedApps(): Flow<List<App>>
    fun getHiddenApps(): List<App>
    fun getBlockedApps(): List<App>
    suspend fun blockUnblockApp(packageName: String, blocked: Int, blockReleaseDate: String?)
    suspend fun pinUnpinApp(packageName: String,pinned: Int)
    suspend fun hideUnhideApp(packageName: String,hidden: Int)
    fun newAppInstalled(app: App)
    suspend fun removeUninstalled(packageName: String)
    fun batteryInfoReceiver(batteryInfoValue: BatteryInfo)
}