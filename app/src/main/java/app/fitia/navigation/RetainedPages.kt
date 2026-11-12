package app.fitia.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import app.fitia.design.FitiaMotion

/**
 * Keeps pages that were already opened in composition, and measures only the
 * selected one. Coming back does not rebuild the screen. The entrance spring
 * is read from the draw phase, so the page itself does not recompose each frame.
 */
@Composable
fun <T> RetainedPages(
    pages: List<T>,
    selected: T,
    modifier: Modifier = Modifier,
    pageKey: (T) -> Any,
    content: @Composable (T) -> Unit,
) {
    val selectedIndex = pages.indexOf(selected).coerceAtLeast(0)
    val previousIndex = remember { intArrayOf(selectedIndex) }
    val forward = selectedIndex >= previousIndex[0]
    SideEffect { previousIndex[0] = selectedIndex }
    var motionsEnabled by remember { mutableStateOf(false) }
    SideEffect { motionsEnabled = true }

    Layout(
        modifier = modifier.clipToBounds(),
        content = {
            pages.forEach { page ->
                key(pageKey(page)) {
                    val active = page == selected
                    val motion = remember(active) {
                        Animatable(if (active && motionsEnabled) 0f else 1f)
                    }
                    val direction = remember(active) { if (forward) 1f else -1f }
                    LaunchedEffect(motion) {
                        if (motion.value < 1f) {
                            motion.animateTo(1f, FitiaMotion.effects())
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                if (!active) return@graphicsLayer
                                val amount = motion.value.coerceIn(0f, 1f)
                                alpha = 0.72f + (0.28f * amount)
                                translationX = (1f - amount) * direction * 24.dp.toPx()
                            }
                            .focusProperties { canFocus = active }
                            .then(if (active) Modifier else Modifier.clearAndSetSemantics {}),
                    ) {
                        content(page)
                    }
                }
            }
        },
    ) { measurables, constraints ->
        val placeable = measurables.getOrNull(selectedIndex)?.measure(constraints)
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable?.place(0, 0)
        }
    }
}
