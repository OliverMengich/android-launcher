package com.planara.android_launcher.domain.models


import kotlinx.serialization.Serializable

data class App(
    val name: String,
    val packageName: String,
    val isPinned: Boolean?=false,
    val blockReleaseDate: String?= "",
    val domains: List<String>,
    val isHidden: Boolean=false,
    val category: String?=null,
)
enum class BlockType{
    NORMAL,
    SCHEDULED
}
@Serializable
data class UsageTime(val startTime: String,val endTime: String)
@Serializable
data class BlockedApp(
    val packageName: String,
    val name: String,
    val blockType: Pair<BlockType,List<UsageTime>>,
    val domains: List<String>,
    val releaseDate: String?=null,
)