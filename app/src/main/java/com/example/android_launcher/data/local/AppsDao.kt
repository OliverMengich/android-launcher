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

    @Query("SELECT * FROM apps WHERE isHidden=1;")
    fun getHiddenApps(): List<App>

    @Query("SELECT * FROM apps WHERE isBlocked=1;")
    fun getBlockedApps(): List<App>

    @Query("UPDATE apps SET isPinned=:pinned WHERE packageName =:packageName;")
    fun pinApp(packageName: String,pinned: Int)

    @Query("UPDATE apps SET isBlocked=:blocked, blockReleaseDate=:blockReleaseDate WHERE packageName =:packageName;")
    fun blockUnblockApp(packageName: String, blocked: Int,blockReleaseDate: String?)

    @Query("UPDATE apps SET isHidden=:hidden WHERE packageName=:packageName;")
    fun hideUnhideApp(packageName: String,hidden: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertApps(apps: List<App>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertApp(app: App)

    @Query("DELETE FROM apps WHERE packageName=:packageName")
    fun deleteApp(packageName: String)
}