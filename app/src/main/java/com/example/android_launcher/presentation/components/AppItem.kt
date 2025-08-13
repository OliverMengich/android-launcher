package com.example.android_launcher.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_launcher.R
import com.example.android_launcher.domain.models.App

@Composable
fun AppItem(ap: App, onClick: ()->Unit, onHideApp: ()->Unit, onUninstallApp: ()-> Unit, onBlockApp: ()->Unit, onPinApp: ()->Unit,isInHomeScreen:Boolean?=false,) {

    var isActive by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 4.dp else 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) if (isSystemInDarkTheme()) Color(
                0xFF6A2C00
            ) else Color(0xffFFD4B6) else Color.Transparent
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    if (isActive) {
                        isActive = false
                    } else {
                        isActive = false
                        onClick()
                    }
                },
                onLongClick = {
                    isActive = true
                }
            )
            .background(color = Color.Transparent, shape = RoundedCornerShape(10.dp))
            .fillMaxWidth(.92f)) {
            if (ap.isPinned==true && isInHomeScreen==true){
                Spacer(Modifier.height(5.dp).padding(top=10.dp))
                Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        ap.name,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp)
                    )
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            modifier = Modifier.clickable { onPinApp(); isActive=false },
                            contentDescription = null
                        )
                    }
                }
                Spacer(Modifier.height(5.dp))

            }else {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text(
                        text=ap.name,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp)
                    )
                    if (isActive) {
                        Column {
                            HorizontalDivider(modifier = Modifier.fillMaxWidth(), 1.dp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        isActive = false
                                        onPinApp()
                                    },
                                    content = {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onBackground,
                                        )
                                        Text(
                                            text = if (ap.isPinned == true) "Remove" else "Favourite",
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }
                                )
                                TextButton(
                                    onClick = {
                                        isActive = false;
                                        onHideApp()
                                    },
                                    content = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_block),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = if (ap.isHidden == true) {
                                                "Unhide"
                                            } else "Hide",
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }
                                )
                                if (ap.isBlocked == false) {
                                    TextButton(
                                        onClick = {
                                            isActive = false;
                                            onBlockApp()
                                        },
                                        content = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_block),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = if (ap.isBlocked) {
                                                    "Unblock"
                                                } else "Block",
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        isActive = false;
                                        onUninstallApp()
                                    },
                                    content = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Uninstall",
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
    }
}