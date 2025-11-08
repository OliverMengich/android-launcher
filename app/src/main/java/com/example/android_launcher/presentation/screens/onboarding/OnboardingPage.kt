package com.example.android_launcher.presentation.screens.onboarding

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android_launcher.IS_LOGGED_IN_KEY
import com.example.android_launcher.MainActivity
import com.example.android_launcher.OnboardingActivity
import com.example.android_launcher.dataStore
import com.example.android_launcher.domain.manager.LocalManager
import com.example.android_launcher.presentation.screens.onboarding.components.DefaultLauncherPermissionPage
import com.example.android_launcher.presentation.screens.onboarding.components.FinishPage
import com.example.android_launcher.presentation.screens.onboarding.components.OverlayPermissionsPage
import com.example.android_launcher.presentation.screens.onboarding.components.SignInSignUp
import com.example.android_launcher.presentation.screens.onboarding.components.UsagePermissionsPage
import com.example.android_launcher.presentation.screens.onboarding.components.WelcomeScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingPage(viewModel: OnboardingViewModel = koinViewModel(),finishNavigate:()->Unit,activePage: Int, padding: PaddingValues){
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val user = Firebase.auth.currentUser
//    val sharedPref = context.getSharedPreferences("settings_value", Context.MODE_PRIVATE)
    val isLoggedInUser = context.dataStore.data.collectAsStateWithLifecycle(initialValue = LocalManager()).value.isLoggedIn
//    val isLoggedInUser = sharedPref.getBoolean("IS_AUTHENTICATED",false)

    val pagerState = rememberPagerState(initialPage = activePage) {
        6
    }
    val overlayPermissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        if (Settings.canDrawOverlays(context)){
            Toast.makeText(context,"Overlay permission granted",Toast.LENGTH_SHORT).show()
            val launchIntent= Intent(context, OnboardingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(launchIntent)
        }
    }
    LaunchedEffect(viewModel.finishNav) {
        viewModel.finishNav.collectLatest {
            if (it){
                scope.launch {
                    pagerState.animateScrollToPage(page = pagerState.currentPage+1)
                }
            }
        }
    }
    Column(modifier = Modifier.padding(paddingValues = padding)) {
        HorizontalPager(state = pagerState, modifier = Modifier, userScrollEnabled = false) { idx->
            when(idx){
                0->{
                    WelcomeScreen(
                        navigateToNext = {
                            scope.launch {
                                pagerState.animateScrollToPage(page = pagerState.currentPage + 1)
                            }
                        }
                    )
                }
                1->{
                    SignInSignUp(
                        navigateToPrev = {
                            scope.launch {
                                pagerState.animateScrollToPage(page = pagerState.currentPage-1)
                            }
                        },
                        loginWithEmailAndPassword = { email,password->
                            viewModel.loginWithEmailAndPassword(email,password)
                        },
                        navigateToNextPage = {
                            if (isLoggedInUser){
                                val startIntent = Intent(context, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                }
                                context.startActivity(startIntent)
                            }else{
                                scope.launch {
                                    pagerState.animateScrollToPage(page = pagerState.currentPage+1)
                                }
                            }
                        }
                    )
                }
                2->{
                    UsagePermissionsPage(
                        isActive=pagerState.currentPage==2,
                        navigateToPrev = {
                            scope.launch {
                                pagerState.animateScrollToPage(page = pagerState.currentPage-1)
                            }
                        },
                        navigateToNextPage = {
                            if (viewModel.hasUsageAccess(context)){
                                scope.launch {
                                    pagerState.animateScrollToPage(page = pagerState.currentPage+1)
                                }
                            }else {
                                Toast.makeText(context,"Please grant usage permission to proceed.", Toast.LENGTH_LONG).show()
                            }
                        },
                        requestUsagePermission = {
                            if (viewModel.hasUsageAccess(context)){
                                Toast.makeText(context,"Permission already granted.", Toast.LENGTH_LONG).show()
                                scope.launch {
                                    pagerState.animateScrollToPage(page = pagerState.currentPage+1)
                                }
                            }else {
                                viewModel.requestUsageStatsPermission()
                            }
                        },
                        hasUsageAccess= viewModel.hasUsageAccess(context)
                    )
                }
                3->{
                    OverlayPermissionsPage(
                        isActive=pagerState.currentPage==3,
                        navigateToPrev = {
                            scope.launch {
                                pagerState.animateScrollToPage(page = pagerState.currentPage-1)
                            }
                        },
                        navigateToNextPage = {

                            if (Settings.canDrawOverlays(context)) {
                                scope.launch {
                                    pagerState.animateScrollToPage(page = pagerState.currentPage + 1)
                                }
                            }else{
                                Toast.makeText(context,"Please grant overlay permission to proceed.", Toast.LENGTH_LONG).show()
                            }
                        },
                        requestOverlayPermission = {
                            if (Settings.canDrawOverlays(context)){
                                Toast.makeText(context,"Permission already granted.", Toast.LENGTH_LONG).show()
                                scope.launch {
                                    pagerState.animateScrollToPage(page = pagerState.currentPage+1)
                                }
                            }else {
                                viewModel.requestOverlayPermission()
                            }
                        },
                    )
                }
                4->{
                    DefaultLauncherPermissionPage(
                        isActive=pagerState.currentPage==4,
                        navigateToPrev = {
                            scope.launch {
                                pagerState.animateScrollToPage(page = pagerState.currentPage-1)
                            }
                        },
                        navigateToNextPage = {
                            scope.launch {
                                pagerState.animateScrollToPage(page = pagerState.currentPage+1)
                            }
                        },
                        requestSetDefaultLauncher = {
                            viewModel.requestSetDefaultLauncher()
                        },
                    )
                }
                5->{
                    FinishPage(
                        navigateToNextPage = {
                            scope.launch {
                                viewModel.loginUser(isLoggedIn = true)
                                val startIntent = Intent(context, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                }
                                context.startActivity(startIntent)
                            }
//                            scope.launch {
//                                context.dataStore.edit { st ->
//                                    st[IS_LOGGED_IN_KEY] = true
//                                }
//                            }
//                            finishNavigate()
                        },
                        navigateToPrev = {
                            scope.launch {
                                pagerState.animateScrollToPage(page = pagerState.currentPage-1)
                            }
                        }
                    )
                }
            }
        }
    }
}