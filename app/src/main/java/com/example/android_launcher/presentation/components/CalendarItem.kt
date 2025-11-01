package com.example.android_launcher.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_launcher.domain.models.Event
import com.example.android_launcher.utils.formatTimeToRequiredFormat
import java.time.LocalTime
import java.util.Locale

@Composable
fun CalendarItem(title: String,timeFormat: String?="HH:mm",events: List<Event>){
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text=title, modifier = Modifier.padding(end = 10.dp), fontSize = 25.sp, fontWeight = FontWeight.Bold, )
            HorizontalDivider(modifier = Modifier.fillMaxWidth(.85f),1.dp)
        }
        if (events.isEmpty()){
            Text("No events for this hour", modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp), textAlign = TextAlign.Center)
        }else{
            LazyColumn {
                items(items=events){ev->
                    Text(
                        text=" - ${ev.startTime.formatTimeToRequiredFormat(timeFormat)} - ${ev.endTime.formatTimeToRequiredFormat(timeFormat)} ${ev.title}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
