package com.planara.android_launcher.presentation.screens.home.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.planara.android_launcher.R
import com.planara.android_launcher.domain.models.App
import com.planara.android_launcher.presentation.screens.home.home.apps.AppsPage
import com.planara.android_launcher.presentation.screens.home.home.calendar.CalendarPage
import kotlinx.coroutines.launch

@Composable
fun LauncherHome(navigateToSettingsPage:()->Unit, navigateToBlockedApp:(App)->Unit,navigateToNewEvent:()->Unit, navigateToBlockingAppPage:(App)-> Unit) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    label = { Text(text="Apps") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                        selectedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(page=0)
                        }
                    },
                    icon = {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(id = R.drawable.ic_dashboard),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    selected = pagerState.currentPage==0,
                )
                NavigationBarItem(
                    label = { Text("Home") },
                    onClick = {
//                        if (isFocusModeActive){
//                            Toast.makeText(context, "Focus mode is active", Toast.LENGTH_SHORT).show()
//                            return@NavigationBarItem
//                        }
                        scope.launch {
                            pagerState.animateScrollToPage(page = 1)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                        selectedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    selected = pagerState.currentPage==1,
                )
                NavigationBarItem(
                    label = { Text(text="Calendar") },
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(page = 2)
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                        selectedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    icon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    selected = pagerState.currentPage==2,
                )
            }
        }
    ) { innerP->
        HorizontalPager(state = pagerState,  modifier = Modifier.fillMaxSize().padding(paddingValues = innerP)) {index->
            when(index){
                0->{
                    AppsPage(
                        navigateToBlockedApp=navigateToBlockedApp,
                        navigateToBlockingAppPage=navigateToBlockingAppPage,
                        isFocused = pagerState.currentPage==0,
                        navigateToHome = {
                            scope.launch {
                                pagerState.animateScrollToPage(page = 1)
                            }
                        }
                    )
                }
                1->{
                    HomePage(navigateToSettingsPage = navigateToSettingsPage, )
                }
                2->{
                    CalendarPage(
                        navigateToNewEvent=navigateToNewEvent,
                        navigateToHome = {
                            scope.launch {
                                pagerState.animateScrollToPage(page = 1)
                            }
                        }
                    )
                }
            }
        }
    }
}