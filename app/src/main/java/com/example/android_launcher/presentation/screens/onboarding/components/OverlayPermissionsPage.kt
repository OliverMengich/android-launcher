package com.example.android_launcher.presentation.screens.onboarding.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_launcher.R
import com.example.android_launcher.presentation.components.OnboardAppPreview

@Composable
fun OverlayPermissionsPage(requestOverlayPermission: ()-> Unit,isActive: Boolean, navigateToNextPage:()-> Unit,navigateToPrev: ()->Unit){
    Box(modifier = Modifier.fillMaxSize().padding(top=50.dp)) {
        Column(modifier = Modifier.fillMaxSize(),) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { idx->
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onBackground,
                            thickness = 3.dp
                        )
                        Box(
                            modifier = Modifier.size(40.dp)
                                .background(
                                    color=if (idx==0 || idx==1) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                    shape = RoundedCornerShape(40.dp)
                                )
                                .border(
                                    shape = CircleShape,
                                    border = BorderStroke(width = 2.dp,  color = MaterialTheme.colorScheme.onBackground)
                                ),
                            contentAlignment = Alignment.Center
                        ){
                            if (idx==0 || idx==1) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = if (isSystemInDarkTheme()) Color.Black else Color.White)
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onBackground,
                            thickness = 3.dp
                        )
                    }
                }
            }


            Column(modifier = Modifier.fillMaxWidth().padding(top=16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                OnboardAppPreview(
                    title="Display over other apps",
                    activeSubTitle = "Allowed",
                    activeTitle = "Planara Launcher",
                    isActive=isActive,
                )
                Text(" Planara launcher needs permission to display content over other apps to allow in app time reminder to be functional",modifier = Modifier.fillMaxWidth(.9f), textAlign = TextAlign.Center)
                Row(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp).fillMaxWidth(0.9f).background(color = if (isSystemInDarkTheme()) Color(0xff252525) else Color.White, shape = RoundedCornerShape(20.dp)).border(color = Color.Transparent, width = 1.dp, shape = RoundedCornerShape(10.dp)), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.padding(10.dp)
                            .size(40.dp)
                            .border(
                                shape = CircleShape,
                                border = BorderStroke(width = 2.dp, Color.Black)
                            ).background(Color.Black, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ){ Text("1",color=Color.White) }
                    Text(" Press button below and find My launcher",modifier = Modifier.padding(end = 10.dp))
                }
                Row(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp).fillMaxWidth(0.9f).background(color = if (isSystemInDarkTheme()) Color(0xff252525) else Color.White, shape = RoundedCornerShape(20.dp)).border(color = Color.Transparent, width = 1.dp, shape = RoundedCornerShape(10.dp)), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.padding(10.dp)
                            .size(40.dp).border(
                                shape = CircleShape,
                                border = BorderStroke(
                                    2.dp,
                                    Color.Black
                                )
                            ).background(
                                Color.Black,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ){ Text("2",color=Color.White) }
                    Text("  Activate switch next to my launcher",modifier = Modifier.padding(end = 10.dp))
                }
                Button(
                    onClick = requestOverlayPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    content = {
                        Text("Grant Overlay Permission",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.background,
                        )
                    }
                )
            }
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)){
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = navigateToPrev,
                    content = {
                        Text("PREVIOUS",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                )
                TextButton(onClick = navigateToNextPage,
                    content = {
                        Text("NEXT",
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                    }
                )
            }
        }
    }
}