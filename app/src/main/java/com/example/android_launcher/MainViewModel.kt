package com.example.android_launcher

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(private val context: Context): ViewModel() {
    var splashCondition by mutableStateOf(true)

    private val _isSetupComplete = MutableStateFlow<Boolean?>(null)
    val isSetupComplete: StateFlow<Boolean?> = _isSetupComplete

//    var isSetupComplete  by mutableStateOf(false)

    var startDestination by mutableStateOf<Route>(Onboarding)
        private set
    init {
//        _isSetupComplete.value = true
        viewModelScope.launch {

            val isLoggedIn = context.dataStore.data.map{it.isLoggedIn}.first()
            Log.d("start_screen","is Logged is=$isLoggedIn")
            _isSetupComplete.value = true
            startDestination = if (isLoggedIn){
                HomeNavigation
            }else{
                Onboarding
            }
            delay(300)
            splashCondition=false
        }

    }
    private fun isLoggedInFc(){

    }
}