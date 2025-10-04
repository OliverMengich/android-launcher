package com.example.android_launcher.presentation.screens.home.settings

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.Uri
import coil3.toUri
import com.example.android_launcher.BlockedAppActivity
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.domain.repository.AppsRepository
import com.example.android_launcher.services.FocusModeService
import com.example.android_launcher.utils.isDatePassed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class SettingsViewModel(private val appsRepository: AppsRepository, private val context: Context): ViewModel() {
    val pm: PackageManager? = context.packageManager

    private val _hiddenApps = MutableStateFlow<List<App>>(emptyList())
    val hiddenApps: StateFlow<List<App>> = _hiddenApps.asStateFlow()

    fun enableFocusModeHandler(isoDateTime: String?){
        if (isoDateTime==null){
            Toast.makeText(context,"Invalid date",Toast.LENGTH_SHORT).show()
            return
        }
        val sharedRef = context.getSharedPreferences("settings_value",MODE_PRIVATE)
        val isFocusModeOn = sharedRef.getBoolean("focus_mode",false)
        val focusModeEndTime = sharedRef.getString("focus_mode_end_time","").let {
            if (it.isNullOrEmpty()) null else LocalDateTime.parse(it)
        }
        if (isFocusModeOn && LocalDateTime.now().isAfter(focusModeEndTime)){
            Toast.makeText(context,"Focus mode is already expired",Toast.LENGTH_SHORT).show()
            sharedRef.edit{
                putBoolean("focus_mode",false)
                putString("focus_mode_end_time","")
                apply()
            }
            return
        }
        if (isFocusModeOn && focusModeEndTime!=null){
            Toast.makeText(context,"Focus mode is already on",Toast.LENGTH_SHORT).show()
            return
        }
        val focusService = Intent(context, FocusModeService::class.java).apply {
            putExtra("END_TIME",isoDateTime)
        }
        sharedRef.edit {
            putBoolean("focus_mode", true)
            putString("focus_mode_end_time", isoDateTime)
            apply()
        }
        context.startForegroundService(focusService)
    }
    fun handleUninstall(){
        val packageName = context.packageName
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = "package:${packageName}".toUri()
        context.startActivity(intent)
    }
    fun handleNavigateToSettings(){
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}