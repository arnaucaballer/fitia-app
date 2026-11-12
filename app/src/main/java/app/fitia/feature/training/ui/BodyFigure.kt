package app.fitia.feature.training.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
fun BodyFigure(
    front: Boolean,
    expandedZoneId: String?,
    expandedHit: Int,
    onZone: (MuscleZone, hitIndex: Int) -> Unit,
    onOpenZone: (MuscleZone) -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val zones = MuscleMap.zones.filter { it.front == front }

    AnatomyPlate(front = front, modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            zones.forEach { zone ->
                zone.bodyHits.forEachIndexed { index, hit ->
                    val selected = expandedZoneId == zone.id && expandedHit == index
                    Box(
                        modifier = Modifier
                            .offset(x = maxWidth * hit.left, y = maxHeight * hit.top)
                            .size(
                                width = maxWidth * (hit.right - hit.left),
                                height = maxHeight * (hit.bottom - hit.top),
                            )
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                if (selected) onOpenZone(zone) else onZone(zone, index)
                            }
                            .semantics { contentDescription = zone.title },
                    ) {
                        if (selected) {
                            MuscleTint(
                                front = front,
                                left = hit.left,
                                top = hit.top,
                                right = hit.right,
                                bottom = hit.bottom,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
            val expanded = zones.firstOrNull { it.id == expandedZoneId }
            val hit = expanded?.bodyHits?.getOrNull(expandedHit)
            if (expanded != null && hit != null) {
                MuscleNameChip(
                    text = expanded.title,
                    left = hit.left,
                    top = hit.top,
                    right = hit.right,
                    bottom = hit.bottom,
                    maxWidth = maxWidth,
                    maxHeight = maxHeight,
                    onClick = { onOpenZone(expanded) },
                )
            }
        }
    }
}
