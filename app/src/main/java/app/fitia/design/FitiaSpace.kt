package app.fitia.design

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object FitiaSpace {
    val screen: Dp = 20.dp
    val stack: Dp = 12.dp
    val card: Dp = 16.dp
    val related: Dp = 8.dp

    fun page(
        top: Dp = screen,
        bottom: Dp = screen,
    ): PaddingValues = PaddingValues(
        start = screen,
        end = screen,
        top = top,
        bottom = bottom,
    )
}
