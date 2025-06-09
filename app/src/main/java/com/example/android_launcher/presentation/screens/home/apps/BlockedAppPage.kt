package com.example.android_launcher.presentation.screens.home.apps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel

@Composable
fun BlockedAppPage(viewModel: BlockedAppViewModel = koinViewModel(),packageName: String){
    val appStats = viewModel.appStats.collectAsState().value
    LaunchedEffect(packageName) {
        viewModel.getAppUsageStats(packageName)
    }
    Box(modifier = Modifier.fillMaxSize()){
        Text(appStats?.packageName.toString())
    }
}