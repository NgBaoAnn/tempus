package com.projectapp.tempus.ui.theme

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

// Tempus App Colors
private val TempusPrimary = Color(0xFF34C759)      // Green - matches FAB in Garden
private val TempusSecondary = Color(0xFF3CDAEF)    // Cyan - matches chart color
private val TempusAccent = Color(0xFF7C4DFF)       // Purple - matches task color
private val TempusBackground = Color(0xFFF8F8FA)   // Light gray background
private val TempusSurface = Color(0xFFFFFFFF)
private val TempusOnPrimary = Color(0xFFFFFFFF)
private val TempusOnBackground = Color(0xFF1C1C1E)

private val LightColorScheme = lightColorScheme(
    primary = TempusPrimary,
    secondary = TempusSecondary,
    tertiary = TempusAccent,
    background = TempusBackground,
    surface = TempusSurface,
    onPrimary = TempusOnPrimary,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TempusOnBackground,
    onSurface = TempusOnBackground,
    surfaceVariant = Color(0xFFF2F2F2),
    outline = Color(0xFFE5E5EA)
)

private val DarkColorScheme = darkColorScheme(
    primary = TempusPrimary,
    secondary = TempusSecondary,
    tertiary = TempusAccent,
    background = Color(0xFF1C1C1E),
    surface = Color(0xFF2C2C2E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3A3A3C),
    outline = Color(0xFF48484A)
)

@Composable
fun TempusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic color for consistent branding
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
        typography = TempusTypography,
        content = content
    )
}
