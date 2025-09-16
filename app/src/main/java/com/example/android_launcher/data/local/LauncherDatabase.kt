package com.example.android_launcher.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.domain.models.Event

@Database(entities = [Event::class, App::class], version = 1)
@TypeConverters(ListTypeConverter::class)
abstract class LauncherDatabase: RoomDatabase() {
    abstract fun appsDao(): AppsDao
    abstract fun calendarDao(): CalendarDao
}