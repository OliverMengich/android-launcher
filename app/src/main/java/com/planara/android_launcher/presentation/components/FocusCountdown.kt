package com.planara.android_launcher.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planara.android_launcher.utils.formatCountdown
import com.planara.android_launcher.utils.formatTimeToRequiredFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Countdown ring for a running focus session: the remaining time (bold, like the home clock)
 * inside a circular indicator that depletes as the session approaches its end, with the
 * session's start and end hours underneath.
 */
@Composable
fun FocusCountdown(
    remainingMillis: Long,
    totalMillis: Long,
    startTime: LocalTime?,
    endTime: LocalTime?,
    timeFormat: String,
    modifier: Modifier = Modifier,
    ringSize: Dp = 200.dp,
    strokeWidth: Dp = 8.dp,
    valueFontSize: Int = 32
) {
    val targetProgress = if (totalMillis <= 0L) 0f else (remainingMillis.toFloat() / totalMillis).coerceIn(0f, 1f)
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 500),
        label = "focus-countdown-progress"
    )
    val onBackground = MaterialTheme.colorScheme.onBackground
    val track = onBackground.copy(alpha = 0.15f)
    val accent = MaterialTheme.colorScheme.primary

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(ringSize)) {
                val strokePx = strokeWidth.toPx()
                val diameter = size.minDimension - strokePx
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)

                // Track
                drawArc(
                    color = track,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                // Remaining time
                if (progress > 0f) {
                    drawArc(
                        color = accent,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }

            // Remaining time as HH:mm:ss, bold like the home clock.
            Text(
                text = formatCountdown(remainingMillis),
                fontSize = valueFontSize.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = onBackground
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = "remaining",
            fontSize = 12.sp,
            color = onBackground.copy(alpha = 0.7f)
        )

        if (startTime != null && endTime != null) {
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = startTime.formatTimeToRequiredFormat(timeFormat),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = "  –  ", fontSize = 14.sp, color = onBackground.copy(alpha = 0.7f))
                Text(
                    text = endTime.formatTimeToRequiredFormat(timeFormat),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** Convenience: duration between now and [end]. */
fun remainingMillisUntil(end: LocalDateTime?, now: LocalDateTime = LocalDateTime.now()): Long =
    if (end == null) 0L else Duration.between(now, end).toMillis().coerceAtLeast(0L)
