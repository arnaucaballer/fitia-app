package app.fitia.feature.training.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.fitia.R
import app.fitia.design.FitiaCard
import app.fitia.design.FitiaCardShape
import app.fitia.design.FitiaMotion
import app.fitia.design.FitiaPrimaryButton
import app.fitia.design.FitiaSectionHeader
import app.fitia.design.FitiaSpace
import app.fitia.design.fitiaClickable
import app.fitia.design.fitiaPress
import app.fitia.di.fitiaContainer
import app.fitia.di.fitiaViewModelFactory
import app.fitia.feature.training.domain.Workout
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun LogSessionScreen(
    onOpenSession: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: LogSessionViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            LogSessionViewModel(
                observeWorkoutsUseCase = c.observeWorkoutsUseCase,
                observePlansUseCase = c.observePlansUseCase,
                observeActiveSessionUseCase = c.observeActiveSessionUseCase,
                startSessionFromWorkoutUseCase = c.startSessionFromWorkoutUseCase,
                startBlankSessionUseCase = c.startBlankSessionUseCase,
            )
        },
    )
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    var showBlank by rememberSaveable { mutableStateOf(false) }
    var libraryOpen by rememberSaveable { mutableStateOf(true) }
    var openPlanIds by rememberSaveable { mutableStateOf("") }
    val planFallback = stringResource(R.string.training_log_plan_fallback)
    val library = remember(workouts) { workouts.filter { it.isGeneric } }
    val planSections = remember(workouts, plans, planFallback) {
        val names = plans.associate { it.id to it.name }
        workouts.filterNot { it.isGeneric }
            .groupBy { it.planId }
            .map { (planId, items) ->
                LogPlanSection(
                    id = planId ?: 0L,
                    title = planId?.let { names[it] } ?: planFallback,
                    workouts = items.sortedBy { it.name.lowercase() },
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    LaunchedEffect(Unit) {
        viewModel.sessionStarted.collect(onOpenSession)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = FitiaSpace.page(),
        verticalArrangement = Arrangement.spacedBy(FitiaSpace.stack),
    ) {
        val session = activeSession
        if (session != null) {
            item(key = "active") {
                FitiaCard(onClick = { onOpenSession(session.id) }) {
                    Text(
                        text = stringResource(R.string.training_log_continue),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = session.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = FitiaSpace.related),
                    )
                }
            }
        }

        item(key = "blank") {
            FitiaPrimaryButton(
                text = stringResource(R.string.training_log_blank),
                onClick = { showBlank = true },
            )
        }

        item(key = "from-header") {
            FitiaSectionHeader(title = stringResource(R.string.training_log_from_workout))
        }

        if (library.isEmpty() && planSections.isEmpty()) {
            item(key = "empty") {
                FitiaCard {
                    Text(
                        text = stringResource(R.string.training_log_empty_workouts),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (library.isNotEmpty()) {
            item(key = "library") {
                LogWorkoutSection(
                    title = stringResource(R.string.training_log_library),
                    workouts = library,
                    expanded = libraryOpen,
                    onToggle = { libraryOpen = !libraryOpen },
                    onStart = viewModel::startFromWorkout,
                )
            }
        }
        planSections.forEach { section ->
            item(key = "plan-${section.id}-${section.title}") {
                val expanded = openPlanIds.split(',').any { it == section.id.toString() }
                LogWorkoutSection(
                    title = section.title,
                    workouts = section.workouts,
                    expanded = expanded,
                    onToggle = { openPlanIds = togglePlanId(openPlanIds, section.id) },
                    onStart = viewModel::startFromWorkout,
                )
            }
        }
    }

    if (showBlank) {
        NameInputDialog(
            title = stringResource(R.string.training_log_blank),
            label = stringResource(R.string.field_name),
            onDismiss = { showBlank = false },
            onConfirm = { name ->
                showBlank = false
                viewModel.startBlank(name)
            },
        )
    }
}

private data class LogPlanSection(
    val id: Long,
    val title: String,
    val workouts: List<Workout>,
)

private fun togglePlanId(openPlanIds: String, planId: Long): String {
    val current = openPlanIds.split(',').filter { it.isNotBlank() }.toMutableSet()
    val key = planId.toString()
    if (!current.add(key)) current.remove(key)
    return current.joinToString(",")
}

@Composable
private fun LogWorkoutSection(
    title: String,
    workouts: List<Workout>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onStart: (Long) -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = FitiaMotion.press,
        label = "logSectionChevron",
    )
    val sizeSpec = spring<IntSize>(
        dampingRatio = 0.72f,
        stiffness = Spring.StiffnessMedium,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(FitiaCardShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .fitiaClickable(onClick = onToggle)
                .padding(horizontal = FitiaSpace.card, vertical = FitiaSpace.stack),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.training_log_section_count, workouts.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = stringResource(
                    if (expanded) R.string.training_log_collapse else R.string.training_log_expand,
                ),
                modifier = Modifier.rotate(rotation),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = sizeSpec) + fadeIn(animationSpec = FitiaMotion.effects()),
            exit = shrinkVertically(animationSpec = sizeSpec) + fadeOut(animationSpec = FitiaMotion.effects()),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(FitiaSpace.related)) {
                workouts.forEach { workout ->
                    FitiaCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = workout.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                modifier = Modifier.fitiaPress(),
                                onClick = { onStart(workout.id) },
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PlayArrow,
                                    contentDescription = stringResource(R.string.training_start_session),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
