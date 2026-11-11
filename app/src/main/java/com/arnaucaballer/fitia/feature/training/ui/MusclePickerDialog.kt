package com.arnaucaballer.fitia.feature.training.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaPill
import com.arnaucaballer.fitia.feature.training.domain.Muscle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusclePickerDialog(
    muscles: List<Muscle>,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    var front by rememberSaveable { mutableStateOf(true) }
    var zoneId by rememberSaveable { mutableStateOf<String?>(null) }
    val zone = MuscleMap.zones.firstOrNull { it.id == zoneId }
    val others = muscles.filter { it.name.lowercase() !in MuscleMap.knownNames }
    val fade = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    BackHandler(enabled = zone != null) {
        zoneId = null
    }

    Dialog(
        onDismissRequest = {
            if (zone != null) zoneId = null else onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = zone?.title ?: stringResource(R.string.training_muscle_map_title),
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (zone != null) zoneId = null else onDismiss()
                            },
                        ) {
                            Icon(
                                imageVector = if (zone != null) {
                                    Icons.AutoMirrored.Filled.ArrowBack
                                } else {
                                    Icons.Filled.Close
                                },
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    },
                )
            },
        ) { padding ->
            AnimatedContent(
                targetState = zone,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                transitionSpec = {
                    (fadeIn(fade) + scaleIn(initialScale = 0.96f, animationSpec = fade)) togetherWith
                        (fadeOut(fade) + scaleOut(targetScale = 0.98f, animationSpec = fade))
                },
                label = "muscleStep",
            ) { current ->
                if (current == null) {
                    BodyStep(
                        front = front,
                        others = others,
                        onFront = { front = true },
                        onBackSide = { front = false },
                        onOpenZone = { zoneId = it.id },
                        onPick = onPick,
                    )
                } else {
                    DetailStep(
                        zone = current,
                        onPick = onPick,
                    )
                }
            }
        }
    }
}

@Composable
private fun BodyStep(
    front: Boolean,
    others: List<Muscle>,
    onFront: () -> Unit,
    onBackSide: () -> Unit,
    onOpenZone: (MuscleZone) -> Unit,
    onPick: (String) -> Unit,
) {
    var expandedZoneId by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedHit by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(front) {
        expandedZoneId = null
    }
    val fade = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(
                if (expandedZoneId == null) {
                    R.string.training_muscle_pick_body
                } else {
                    R.string.training_muscle_pick_zoom
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(FitiaPill)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(4.dp),
        ) {
            SideChip(
                text = stringResource(R.string.training_body_front),
                selected = front,
                onClick = onFront,
            )
            SideChip(
                text = stringResource(R.string.training_body_back),
                selected = !front,
                onClick = onBackSide,
            )
        }
        AnimatedContent(
            targetState = front,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            transitionSpec = { fadeIn(fade) togetherWith fadeOut(fade) },
            label = "bodySide",
        ) { side ->
            BodyFigure(
                front = side,
                expandedZoneId = expandedZoneId,
                expandedHit = expandedHit,
                onZone = { zone, hit ->
                    expandedZoneId = zone.id
                    expandedHit = hit
                },
                onOpenZone = onOpenZone,
                modifier = Modifier.fillMaxSize(),
            )
        }
        val expanded = MuscleMap.zones.firstOrNull { it.id == expandedZoneId && it.front == front }
        if (expanded != null) {
            GeneralMuscleButton(
                name = expanded.generalName,
                onClick = { onPick(expanded.generalName) },
            )
        }
        if (others.isNotEmpty()) {
            Text(
                text = stringResource(R.string.training_muscle_other),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(others, key = { it.id }) { muscle ->
                    FilterChip(
                        selected = false,
                        onClick = { onPick(muscle.name) },
                        label = { Text(muscle.name) },
                    )
                }
            }
        }
        Box(Modifier.padding(bottom = 8.dp))
    }
}

@Composable
private fun DetailStep(
    zone: MuscleZone,
    onPick: (String) -> Unit,
) {
    var expandedPart by rememberSaveable { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.training_muscle_pick_part),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        MuscleDetailFigure(
            zone = zone,
            expandedPart = expandedPart,
            onPart = { expandedPart = it },
            onChoosePart = onPick,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        GeneralMuscleButton(
            name = zone.generalName,
            onClick = { onPick(zone.generalName) },
        )
        Box(Modifier.padding(bottom = 8.dp))
    }
}

@Composable
private fun GeneralMuscleButton(
    name: String,
    onClick: () -> Unit,
) {
    Text(
        text = stringResource(R.string.training_muscle_general, name),
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(FitiaPill)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    )
}

@Composable
private fun SideChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "sideChip",
    )
    val foreground by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "sideChipText",
    )
    Text(
        text = text,
        color = foreground,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clip(FitiaPill)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    )
}
