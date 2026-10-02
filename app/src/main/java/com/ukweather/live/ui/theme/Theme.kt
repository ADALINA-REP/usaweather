package com.ukweather.live.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimaryDark, // #3B82F6
    onPrimary = BackgroundDark,
    primaryContainer = SecondaryCardDark, // #172033
    onPrimaryContainer = TextPrimaryDark,
    secondary = BrandPrimaryDark,
    onSecondary = BackgroundDark,
    secondaryContainer = SecondaryCardDark,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = WeatherSunnyDark, // #FBBF24
    onTertiary = BackgroundDark,
    background = BackgroundDark, // #0B1220
    onBackground = TextPrimaryDark, // #F8FAFC
    surface = SurfaceDark, // #111827
    onSurface = TextPrimaryDark, // #F8FAFC
    surfaceVariant = SecondaryCardDark, // #172033
    onSurfaceVariant = TextSecondaryDark, // #94A3B8
    outline = BorderDark, // #1E293B
    outlineVariant = BorderDark,
    error = StatusDangerDark, // #F87171
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimaryLight, // #2563EB
    onPrimary = Color.White,
    primaryContainer = SecondaryBackgroundLight, // #EFF6FF
    onPrimaryContainer = BrandPrimaryDarkVariant, // #1D4ED8
    secondary = BrandPrimaryDarkVariant, // #1D4ED8
    onSecondary = Color.White,
    secondaryContainer = SecondaryBackgroundLight,
    onSecondaryContainer = BrandPrimaryLight,
    tertiary = WeatherSunnyLight, // #FBBF24
    onTertiary = TextPrimaryLight,
    background = BackgroundLight, // #F8FAFC
    onBackground = TextPrimaryLight, // #0F172A
    surface = SurfaceLight, // #FFFFFF
    onSurface = TextPrimaryLight, // #0F172A
    surfaceVariant = SecondaryBackgroundLight, // #EFF6FF
    onSurfaceVariant = TextSecondaryLight, // #64748B
    outline = BorderLight, // #E2E8F0
    outlineVariant = BorderLight,
    error = StatusDangerLight, // #EF4444
    onError = Color.White
)

@Composable
fun SkyCastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // We want full control over our premium colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}



