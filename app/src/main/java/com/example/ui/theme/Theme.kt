package com.example.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StimulerDarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = TextPrimaryDark,
    primaryContainer = PurpleDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = TealAccent,
    onSecondary = TextPrimaryDark,
    secondaryContainer = SurfaceDarkVariant,
    onSecondaryContainer = TealLight,
    tertiary = RoseAccent,
    onTertiary = TextPrimaryDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = SurfaceBorder
)

private val StimulerLightColorScheme = lightColorScheme(
    primary = PurpleDark,
    onPrimary = TextPrimaryDark,
    primaryContainer = PurpleLight,
    onPrimaryContainer = BackgroundDark,
    secondary = TealAccent,
    onSecondary = TextPrimaryDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = SurfaceBorder
)

@Composable
fun StimulerTheme(
    darkTheme: Boolean = true, // Default to Stimuler's signature sleek dark palette
    content: @Composable () -> Unit
) {
    val colorScheme = StimulerDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            window.navigationBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
