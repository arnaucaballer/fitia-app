package app.fitia.feature.training.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.fitia.design.FitiaPill

@Composable
fun MuscleNameChip(
    text: String,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    maxWidth: Dp,
    maxHeight: Dp,
    onClick: () -> Unit,
    style: TextStyle = MaterialTheme.typography.labelLarge,
) {
    val view = LocalView.current
    val above = top >= 0.20f
    val align = when {
        above && left < 0.15f -> Alignment.BottomStart
        above && right > 0.85f -> Alignment.BottomEnd
        above -> Alignment.BottomCenter
        left < 0.15f -> Alignment.TopStart
        right > 0.85f -> Alignment.TopEnd
        else -> Alignment.TopCenter
    }
    val hitLeft = maxWidth * left
    val hitWidth = maxWidth * (right - left)
    val anchorWidth = hitWidth.coerceAtLeast(168.dp)
    val anchorX = when {
        left < 0.15f -> hitLeft
        right > 0.85f -> hitLeft + hitWidth - anchorWidth
        else -> hitLeft - (anchorWidth - hitWidth) / 2
    }
    val fade = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    val grow = spring<IntSize>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    Box(
        modifier = Modifier
            .offset(
                x = anchorX,
                y = if (above) maxHeight * top - 52.dp else maxHeight * bottom,
            )
            .size(width = anchorWidth, height = 52.dp)
            .wrapContentSize(align = align, unbounded = true)
            .zIndex(2f),
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(fade) + expandHorizontally(animationSpec = grow),
            exit = fadeOut(fade) + shrinkHorizontally(animationSpec = grow),
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                style = style,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .shadow(8.dp, FitiaPill)
                    .clip(FitiaPill)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        onClick()
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}
