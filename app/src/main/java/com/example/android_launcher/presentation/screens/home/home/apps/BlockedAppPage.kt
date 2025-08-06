package com.example.android_launcher.presentation.screens.home.apps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel

@Composable
fun BlockedAppPage(viewModel: BlockingAppViewModel = koinViewModel(), name: String, blockReleaseDate: String?= "", packageName: String){

    Box(modifier = Modifier.fillMaxSize()){
        Text("App: $name")
        OutlinedButton(onClick = { }) {
            Text("Close")
        }
    }
}