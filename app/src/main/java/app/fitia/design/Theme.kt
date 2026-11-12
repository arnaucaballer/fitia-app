package app.fitia.design

import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

private val FitiaColorScheme = darkColorScheme(
    primary = FitiaOrange,
    onPrimary = FitiaBlack,
    primaryContainer = FitiaOrangeMuted,
    onPrimaryContainer = FitiaOrangeSoft,
    secondary = FitiaSand,
    onSecondary = FitiaBlack,
    secondaryContainer = FitiaCardElevated,
    onSecondaryContainer = FitiaWhite,
    tertiary = FitiaSand,
    onTertiary = FitiaBlack,
    tertiaryContainer = FitiaSandMuted,
    onTertiaryContainer = FitiaSand,
    background = FitiaBlack,
    onBackground = FitiaWhite,
    surface = FitiaNearBlack,
    onSurface = FitiaWhite,
    surfaceVariant = FitiaCard,
    onSurfaceVariant = FitiaMuted,
    surfaceContainerLowest = FitiaBlack,
    surfaceContainerLow = FitiaNearBlack,
    surfaceContainer = FitiaCard,
    surfaceContainerHigh = FitiaCardElevated,
    surfaceContainerHighest = Color(0xFF2E2A26),
    outline = FitiaOutline,
    outlineVariant = FitiaOutline,
    error = Color(0xFFFF6B6B),
    onError = FitiaBlack,
    errorContainer = FitiaErrorContainer,
    onErrorContainer = Color(0xFFFFD0D0),
)

@Composable
fun FitiaTheme(
    content: @Composable () -> Unit,
) {
    val rippleIndication = ripple()
    val indication = remember(rippleIndication) {
        FitiaIndication(rippleIndication)
    }
    MaterialTheme(
        colorScheme = FitiaColorScheme,
        typography = FitiaTypography,
        shapes = FitiaShapes,
    ) {
        CompositionLocalProvider(LocalIndication provides indication, content = content)
    }
}
