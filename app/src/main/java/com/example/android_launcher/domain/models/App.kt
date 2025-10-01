package com.example.android_launcher.domain.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.android_launcher.data.local.CommaListConverter


@Entity(tableName = "apps", indices = [Index(value = ["packageName"], unique = true)])
@TypeConverters(CommaListConverter::class)
data class App(
    val name: String,
    @PrimaryKey val packageName: String,
    val isBlocked: Boolean?=false,
    val isPinned: Boolean?=false,
    val blockReleaseDate: String?= "",
    val domains: List<String>,
    val isHidden: Boolean?=false,
    val category: String?=null,
)