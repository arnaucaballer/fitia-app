package com.arnaucaballer.fitia.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.clearAndSetSemantics

/**
 * Keeps pages that were already opened in composition, and measures only the
 * selected one. Coming back does not rebuild the screen.
 */
@Composable
fun <T> RetainedPages(
    pages: List<T>,
    selected: T,
    modifier: Modifier = Modifier,
    pageKey: (T) -> Any,
    content: @Composable (T) -> Unit,
) {
    Layout(
        modifier = modifier,
        content = {
            pages.forEach { page ->
                key(pageKey(page)) {
                    val active = page == selected
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .focusProperties { canFocus = active }
                            .then(if (active) Modifier else Modifier.clearAndSetSemantics {}),
                    ) {
                        content(page)
                    }
                }
            }
        },
    ) { measurables, constraints ->
        val index = pages.indexOf(selected).coerceAtLeast(0)
        val placeable = measurables.getOrNull(index)?.measure(constraints)
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable?.place(0, 0)
        }
    }
}
