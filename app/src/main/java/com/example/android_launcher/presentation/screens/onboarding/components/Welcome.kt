package com.example.android_launcher.presentation.screens.onboarding.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_launcher.R

@Composable
fun WelcomeScreen(navigateToNext: ()-> Unit){
    Column(
        modifier = Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Welcome to Planara Launcher",
            fontWeight = FontWeight.ExtraBold, fontSize = 25.sp,
            modifier = Modifier.padding(top = 0.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.planara_icon),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(300.dp),
            contentScale = ContentScale.FillBounds
        )
        Column(Modifier.fillMaxWidth()){
            Text(
                text = "Planara launcher is a productivity tool that helps declutter your phone, plan your day, " +
                        "and boost productivity. Manage tasks," +
                        "schedule events, and stay organized—all from a clean, distraction-free home screen.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                ),
            )
            TextButton(
                onClick = navigateToNext,
                modifier = Modifier.align(Alignment.End).padding(all = 20.dp),
                content = {
                    Text(
                        text="GET STARTED",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(10.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                    )
                }
            )
//            TextButton(
//                modifier = Modifier.align(Alignment.End).padding(all = 20.dp),
//                onClick = navigateToNext,
//                shape = RoundedCornerShape(size = 6.dp)
//            ) {
//                Text(
//                    text = "NEXT",
//                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
//                )
//            }
        }
    }
}