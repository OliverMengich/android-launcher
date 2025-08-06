package com.example.android_launcher.presentation.screens.home.apps

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.presentation.components.AppItem
import com.example.android_launcher.presentation.screens.home.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun AppsPage(viewModel: SharedViewModel = koinViewModel(),navigateToBlockingAppPage:(App)->Unit, navigateToBlockedApp:(App)->Unit){
    val focusRequester = remember { FocusRequester() }
    val apps = viewModel.apps.collectAsState().value
    var textField by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val filteredApps = apps.filter { ap->
        ap.name.contains(textField, ignoreCase = true)
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    LaunchedEffect(key1 = viewModel.navigateToBlockedAppPage, key2 = viewModel.navigateToBlockingAppPage) {
        viewModel.navigateToBlockedAppPage.collectLatest { app->
            if(app!=null){
                navigateToBlockedApp(app)
            }
        }
        viewModel.navigateToBlockingAppPage.collectLatest { app->
            if(app!=null){
                navigateToBlockingAppPage(app)
            }
        }
    }

    Column(modifier = Modifier.padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.fillMaxWidth(.9f).background(Color.Transparent, shape = RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.CenterStart,
        ) {
            OutlinedTextField(
                value = textField,
                onValueChange = { textField = it },
                placeholder = { Text("Search Apps", textAlign = TextAlign.Center) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Green,
                    unfocusedBorderColor = Color.Gray
                ),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (filteredApps.isNotEmpty()) {
                            viewModel.launchApp(app=filteredApps[0])
                        }
                    }
                ),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).padding(vertical= 5.dp),
                shape = RoundedCornerShape(40.dp),
                trailingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
            )
        }
        LazyColumn(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            items(filteredApps) { ap->
                AppItem(
                    onClick = {
                        viewModel.launchApp(app=ap)
                    },
                    ap = ap,
                    onHideApp = {
                        scope.launch {
                            viewModel.hideUnhideAppFc(ap,0)
                        }
                    },
                    onUninstallApp = {
                        val intent = Intent(Intent.ACTION_DELETE)
                        intent.data = "package:${ap.packageName}".toUri()
                        context.startActivity(intent)
                    },
                    onBlockApp = {
                        navigateToBlockingAppPage(ap)
//                        scope.launch {
//                            viewModel.blockUnblockAppFc(ap, if(ap.isBlocked == true) 0 else 1)
//                        }
                    },
                    onPinApp = {
                        scope.launch {
                            viewModel.pinUnpinApp(ap, if(ap.isPinned == true) 0 else 1)
                        }
                    },
                )
            }
        }
    }
}