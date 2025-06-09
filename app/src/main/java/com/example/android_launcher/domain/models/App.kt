package com.example.android_launcher.domain.models

import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "apps")
data class App(
    @PrimaryKey val id: Int,
    val name: String,
    val packageName: String,
    val isBlocked: Boolean?=false,
    val isPinned: Boolean?=false,
    val isHidden: Boolean?=false
)