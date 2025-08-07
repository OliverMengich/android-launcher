package com.example.android_launcher.domain.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "apps", indices = [Index(value = ["packageName"], unique = true)])
data class App(
    val name: String,
    @PrimaryKey val packageName: String,
    val isBlocked: Boolean?=false,
    val isPinned: Boolean?=false,
    val blockReleaseDate: String?= "",
    val isHidden: Boolean?=false,
    val category: String?=null,
)