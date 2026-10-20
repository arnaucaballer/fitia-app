package com.arnaucaballer.fitia.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FitiaColorScheme = darkColorScheme(
    primary = FitiaOrange,
    onPrimary = FitiaWhite,
    primaryContainer = FitiaOrangeMuted,
    onPrimaryContainer = FitiaOrangeSoft,
    secondary = FitiaWhite,
    onSecondary = FitiaBlack,
    secondaryContainer = FitiaCardElevated,
    onSecondaryContainer = FitiaWhite,
    background = FitiaBlack,
    onBackground = FitiaWhite,
    surface = FitiaNearBlack,
    onSurface = FitiaWhite,
    surfaceVariant = FitiaCard,
    onSurfaceVariant = FitiaMuted,
    outline = FitiaOutline,
    outlineVariant = FitiaOutline,
    error = Color(0xFFFF5C5C),
    onError = FitiaWhite,
)

@Composable
fun FitiaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = FitiaColorScheme,
        typography = FitiaTypography,
        shapes = FitiaShapes,
        content = content,
    )
}
