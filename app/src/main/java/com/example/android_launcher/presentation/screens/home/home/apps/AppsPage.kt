package com.example.android_launcher.presentation.screens.home.home.apps

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.presentation.components.AppItem
import com.example.android_launcher.presentation.screens.home.SharedViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsPage(viewModel: SharedViewModel = koinViewModel(), navigateToHome:()->Unit, navigateToBlockingAppPage:(App)->Unit,isFocused: Boolean, navigateToBlockedApp:(App)->Unit){
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val localManagerData by viewModel.localManagerData.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    var textField by remember { mutableStateOf("") }

    val filteredApps by remember(apps, textField) {
        derivedStateOf {
            val query = textField.lowercase()
            apps
                .filter { it.name.lowercase().contains(other=query) }
                .sortedWith(
                    comparator = compareByDescending<App> { it.name.lowercase().startsWith(prefix=query) }
                        .thenByDescending { it.name.lowercase().contains(other=query) }
                        .thenBy { it.name.lowercase() }
                )
        }
    }
    val currentTime = Calendar.getInstance()

    val context = LocalContext.current

    LaunchedEffect(key1=isFocused) {
        val isOpenKeyboard = localManagerData.displaySettings.autoOpenKeyboard
        if (isFocused && isOpenKeyboard) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }else{
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }
    BackHandler {
        navigateToHome()
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

    Column(Modifier.fillMaxSize().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.fillMaxWidth(.9f).background(Color.Transparent, shape = RoundedCornerShape(40.dp))) {
            Text(text="My Apps", fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
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
                    capitalization = KeyboardCapitalization.Unspecified,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Unspecified,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (filteredApps.isNotEmpty()) {
                            viewModel.launchApp(
                                app=filteredApps[0],
                                callBackFunction = {
                                    textField = ""
                                    navigateToHome()
                                }
                            )
                        }
                    }
                ),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).padding(vertical= 5.dp),
                shape = RoundedCornerShape(40.dp),
                trailingIcon = {
                    Icon(imageVector = Icons.Default.Clear,modifier= Modifier.clickable{textField=""}, contentDescription = null)
                },
            )
        }
        LazyColumn(Modifier.fillMaxWidth().padding(top = if(textField=="") 0.dp else 30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            items(items=filteredApps) { ap->
                AppItem(
                    onClick = {
                        viewModel.launchApp(
                            app=ap,
                            callBackFunction = {
                                textField = ""
                                navigateToHome()
                            }
                        )
                    },
                    ap = ap,
                    onHideApp = {
                        textField=""
                        scope.launch {
                            viewModel.hideUnhideAppFc(app=ap, hidden=1)
                        }
                    },
                    onUninstallApp = {
                        val intent = Intent(Intent.ACTION_DELETE)
                        intent.data = "package:${ap.packageName}".toUri()
                        context.startActivity(intent)
                    },
                    onBlockApp = {
                        navigateToBlockingAppPage(ap)
                    },
                    onPinApp = {
                        scope.launch {
                            viewModel.pinUnpinApp(app=ap, pinned=if(ap.isPinned == true) 0 else 1)
                        }
                    },
                )
            }
        }
    }
}