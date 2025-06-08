package com.example.android_launcher.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.android_launcher.domain.models.App

@Dao
interface AppsDao {
    @Query("SELECT * FROM apps WHERE isHidden=0;")
    fun getAllApps(): List<App>

    @Query("SELECT * FROM apps WHERE isPinned=1;")
    fun getPinnedApps(): List<App>

    @Query("UPDATE apps SET isPinned=1 WHERE id=:appId;")
    fun pinApp(appId: Int,)

    @Query("UPDATE apps SET isBlocked=1 WHERE id=:appId;")
    fun blockApp(appId: Int,)

    @Query("UPDATE apps SET isHidden=1 WHERE id=:appId;")
    fun hideApp(appId: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertApps(apps: List<App>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertApp(app: App)

    @Query("DELETE FROM apps WHERE packageName=:packageName")
    fun deleteApp(packageName: String)
}