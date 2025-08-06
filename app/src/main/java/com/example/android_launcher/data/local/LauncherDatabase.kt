package com.example.android_launcher.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.domain.models.Event


@Database(entities = [App::class, Event::class], version = 1)
abstract class AppsDatabase: RoomDatabase() {
    abstract fun appsDao(): AppsDao
    abstract fun calendarDao(): CalendarDao
}