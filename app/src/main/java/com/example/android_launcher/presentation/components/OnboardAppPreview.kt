package com.example.android_launcher.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_launcher.R
import kotlinx.coroutines.delay

@Composable
fun OnboardAppPreview(title: String,activeTitle: String,isActive: Boolean, activeSubTitle: String) {
    var visible by remember{
        mutableStateOf(false)
    }
    LaunchedEffect(isActive) {
        if (isActive) {
            delay(500)
            visible = true
        }
    }
    val alpha: Float by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "OpacityAnimation"
    )
    val brush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color.Transparent,
            0.5f to Color.Transparent,
            0.8f to MaterialTheme.colorScheme.background,
            1.0f to MaterialTheme.colorScheme.background,
        )
    )

    Box(modifier = Modifier.fillMaxHeight(fraction=0.55f).padding(top=16.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(10.dp)
                .fillMaxWidth(0.85f)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.onBackground,
                    RoundedCornerShape(40.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.padding(10.dp)
                        .align(Alignment.CenterHorizontally)
                        .size(15.dp)
                        .border(
                            shape = CircleShape,
                            border = BorderStroke(width=2.dp, color=MaterialTheme.colorScheme.onBackground)
                        )
                        .background(color=MaterialTheme.colorScheme.onBackground, shape = CircleShape)
                )
                Row(modifier = Modifier.padding(15.dp).alpha(alpha)) {
                    Icon(
                        imageVector=Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                    Text(text=title)
                }
                AnimatedVisibility(visible = visible,label="animate") {
                    Column {
                        Row(
                            modifier = Modifier.height(70.dp).padding(top = 10.dp)
                                .fillMaxWidth()
                                .background(
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(5.dp)
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(50.dp)
                                    .padding(start = 15.dp, top = 5.dp).background(
                                        shape = RoundedCornerShape(5.dp),
                                        color = Color(0xffe5e5e5),
                                    )
                            )
                            Column {
                                Box(
                                    modifier = Modifier.padding(start = 5.dp)
                                        .fillMaxWidth(0.6f).height(15.dp).background(
                                            shape = RoundedCornerShape(3.dp),
                                            color = Color(0xffe5e5e5),
                                        )
                                )
                                Box(
                                    modifier = Modifier.padding(
                                        start = 5.dp,
                                        top = 5.dp
                                    )
                                        .fillMaxWidth(0.3f).height(8.dp).background(
                                            shape = RoundedCornerShape(3.dp),
                                            color = Color(0xffe5e5e5),
                                        )
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.height(70.dp).padding(top = 10.dp)
                                .fillMaxWidth().background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(5.dp)
                                ).padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                modifier = Modifier.size(50.dp),
                                painter = painterResource(id = R.drawable.planara_icon),
                                contentDescription = "app icon",
                            )
                            Column {
                                Text(text=activeTitle, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                Text(text=activeSubTitle, fontSize = 10.sp)
                            }
                        }
                        Row(
                            modifier = Modifier.height(70.dp).padding(top = 10.dp)
                                .fillMaxWidth().background(
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(5.dp)
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(50.dp)
                                    .padding(start = 15.dp, top = 5.dp).background(
                                        shape = RoundedCornerShape(5.dp),
                                        color = Color(0xffe5e5e5),
                                    )
                            )
                            Column {
                                Box(
                                    modifier = Modifier.padding(start = 5.dp)
                                        .fillMaxWidth(0.6f).height(15.dp).background(
                                            shape = RoundedCornerShape(3.dp),
                                            color = Color(0xffe5e5e5),
                                        )
                                )
                                Box(
                                    modifier = Modifier.padding(
                                        start = 5.dp,
                                        top = 5.dp
                                    )
                                        .fillMaxWidth(0.3f).height(8.dp).background(
                                            shape = RoundedCornerShape(3.dp),
                                            color = Color(0xffe5e5e5),
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize().background(brush = brush)) {
        }
    }
}