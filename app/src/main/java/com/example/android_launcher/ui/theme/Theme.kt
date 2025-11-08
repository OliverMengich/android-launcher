package com.example.android_launcher.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.android_launcher.domain.models.AppFonts

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    background = LightBackground,

    onBackground = LightOnBackground,
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun AndroidlauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+,
    dynamicColor: Boolean = true,
    defaultFont: AppFonts,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) DarkColorScheme else LightColorScheme
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val typography = when(defaultFont){
        AppFonts.Default -> DefaultTypography
        AppFonts.Inter -> InterTypography
        AppFonts.SpaceGrotesk -> SpaceGroteskTypography
        AppFonts.Montserrat -> MontserratTypography
        AppFonts.Sarina -> SarinaTypography
        AppFonts.CrosiantMono -> CroissantTypography
        AppFonts.DancingScript -> DancingTypography
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}