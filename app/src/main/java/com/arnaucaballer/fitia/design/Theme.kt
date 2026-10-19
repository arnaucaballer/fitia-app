package com.arnaucaballer.fitia.design

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = FitiaGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F0E3),
    onPrimaryContainer = FitiaGreenDark,
    secondary = FitiaGreenLight,
    onSecondary = Color.White,
    background = FitiaLightBackground,
    onBackground = FitiaLightOnSurface,
    surface = FitiaLightSurface,
    onSurface = FitiaLightOnSurface,
    surfaceVariant = Color(0xFFEEF2EF),
    onSurfaceVariant = FitiaLightOnSurfaceVariant,
    outline = FitiaLightOutline,
)

private val DarkColorScheme = darkColorScheme(
    primary = FitiaGreenLight,
    onPrimary = Color.White,
    primaryContainer = FitiaGreenDark,
    onPrimaryContainer = Color(0xFFD6F0E3),
    secondary = FitiaGreen,
    onSecondary = Color.White,
    background = FitiaDarkBackground,
    onBackground = FitiaDarkOnSurface,
    surface = FitiaDarkSurface,
    onSurface = FitiaDarkOnSurface,
    surfaceVariant = Color(0xFF222722),
    onSurfaceVariant = FitiaDarkOnSurfaceVariant,
    outline = FitiaDarkOutline,
)

@Composable
fun FitiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FitiaTypography,
        content = content,
    )
}
