package com.example.android_launcher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.android_launcher.R

// Set of Material typography styles to start with
val montserratFont = FontFamily(Font(R.font.montserrat_regular))
val spaceGroteskFont = FontFamily(Font(R.font.space_grotesk_regular))
val interFont = FontFamily(Font(R.font.inter_regular))
val sarinaFont = FontFamily(Font(R.font.sarina_regular))
val croissantFont = FontFamily(Font(R.font.croissant_regular))
val dancingScriptFont = FontFamily(Font(R.font.dancing_script_regular))

val MontserratTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = montserratFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = montserratFont,
        fontWeight = FontWeight.Normal,
    ),
    labelMedium = TextStyle(
        fontFamily = montserratFont,
        fontWeight = FontWeight.Normal,
    )
)
val SpaceGroteskTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = spaceGroteskFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = spaceGroteskFont,
        fontWeight = FontWeight.Normal,
    ),
    labelMedium = TextStyle(
        fontFamily = spaceGroteskFont,
        fontWeight = FontWeight.Normal,
    )
)
val InterTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = interFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = interFont,
        fontWeight = FontWeight.Normal,
    ),
    labelMedium = TextStyle(
        fontFamily = interFont,
        fontWeight = FontWeight.Normal,
    )

)
val SarinaTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = sarinaFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = sarinaFont,
        fontWeight = FontWeight.Normal,
    ),
    labelMedium = TextStyle(
        fontFamily = sarinaFont,
        fontWeight = FontWeight.Normal,
    )
)
val CroissantTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = croissantFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = croissantFont,
        fontWeight = FontWeight.Normal,
    ),
    labelMedium = TextStyle(
        fontFamily = croissantFont,
        fontWeight = FontWeight.Normal,
    )
)
val DancingTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = dancingScriptFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = dancingScriptFont,
        fontWeight = FontWeight.Normal,
    ),
    labelMedium = TextStyle(
        fontFamily = dancingScriptFont,
        fontWeight = FontWeight.Normal,
    )
)
val DefaultTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)