package com.planara.android_launcher.domain.repository

import com.planara.android_launcher.domain.models.App
import com.planara.android_launcher.domain.models.BatteryInfo
import com.planara.android_launcher.domain.models.BlockType
import com.planara.android_launcher.domain.models.BlockedApp
import com.planara.android_launcher.domain.models.UsageTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

interface AppsRepository {
    val batteryInfo: MutableStateFlow<BatteryInfo?>
    val refetchAppsFlow: MutableStateFlow<Boolean?>
    fun getAllApps(): Flow<List<App>>
    fun insertApps(apps: List<App>)
    fun getPinnedApps(): Flow<List<App>>
    suspend fun getHiddenApps(): List<App>
    suspend fun getBlockedApps(): List<BlockedApp>
    suspend fun blockUnblockApp(packageName: String, blockType: Pair<BlockType, List<UsageTime>>?, blockReleaseDate: String?)
    suspend fun pinUnpinApp(packageName: String,pin: Boolean)
    suspend fun hideUnhideApp(packageName: String,hidden: Boolean)
    fun newAppInstalled(app: App)
    suspend fun removeUninstalled(packageName: String)
    fun batteryInfoReceiver(batteryInfoValue: BatteryInfo)
}