package com.arnaucaballer.fitia.feature.training.ui

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
fun MuscleDetailFigure(
    zone: MuscleZone,
    expandedPart: String?,
    onPart: (String) -> Unit,
    onChoosePart: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current

    AnatomyPlate(
        front = zone.front,
        cropLeft = zone.cropLeft,
        cropTop = zone.cropTop,
        cropRight = zone.cropRight,
        cropBottom = zone.cropBottom,
        modifier = modifier,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            zone.parts.forEach { part ->
                val selected = expandedPart == part.name
                Box(
                    modifier = Modifier
                        .offset(x = maxWidth * part.left, y = maxHeight * part.top)
                        .size(
                            width = maxWidth * (part.right - part.left),
                            height = maxHeight * (part.bottom - part.top),
                        )
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            if (selected) onChoosePart(part.name) else onPart(part.name)
                        }
                        .semantics { contentDescription = part.name },
                ) {
                    if (selected) {
                        MuscleTint(
                            front = zone.front,
                            left = part.left,
                            top = part.top,
                            right = part.right,
                            bottom = part.bottom,
                            cropLeft = zone.cropLeft,
                            cropTop = zone.cropTop,
                            cropRight = zone.cropRight,
                            cropBottom = zone.cropBottom,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
            val part = zone.parts.firstOrNull { it.name == expandedPart }
            if (part != null) {
                MuscleNameChip(
                    text = part.label,
                    left = part.left,
                    top = part.top,
                    right = part.right,
                    bottom = part.bottom,
                    maxWidth = maxWidth,
                    maxHeight = maxHeight,
                    onClick = { onChoosePart(part.name) },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
