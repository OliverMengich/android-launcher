package com.example.android_launcher.presentation.screens.home.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_launcher.BlockedAppActivity
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.domain.repository.AppsRepository
import com.example.android_launcher.utils.isDatePassed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val appsRepository: AppsRepository, private val context: Context): ViewModel() {
    val pm: PackageManager? = context.packageManager

    private val _hiddenApps = MutableStateFlow<List<App>>(emptyList())
    val hiddenApps: StateFlow<List<App>> = _hiddenApps.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getHiddenApps()
        }
    }

    fun getHiddenApps(){
        val hdApps = appsRepository.getHiddenApps()
        _hiddenApps.value = hdApps
    }

}