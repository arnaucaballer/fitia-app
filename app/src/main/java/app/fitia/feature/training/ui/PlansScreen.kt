package app.fitia.feature.training.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.fitia.R
import app.fitia.design.FitiaPrimaryButton
import app.fitia.design.FitiaSecondaryButton
import app.fitia.di.fitiaContainer
import app.fitia.di.fitiaViewModelFactory
import app.fitia.design.fitiaPress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    onBack: (() -> Unit)? = null,
    onOpenPlan: (Long) -> Unit,
    onGeneratePlan: (() -> Unit)? = null,
    onFollowPlan: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: PlansViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            PlansViewModel(
                observePlansUseCase = c.observePlansUseCase,
                observeActiveTrainingPlanUseCase = c.observeActiveTrainingPlanUseCase,
                savePlanUseCase = c.savePlanUseCase,
                deletePlanUseCase = c.deletePlanUseCase,
                setActiveTrainingPlanUseCase = c.setActiveTrainingPlanUseCase,
            )
        },
    )
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val activePlanId by viewModel.activePlanId.collectAsStateWithLifecycle()
    var showCreate by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text(stringResource(R.string.training_plans_title)) },
                    navigationIcon = {
                        IconButton(modifier = Modifier.fitiaPress(), onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    },
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "create") {
                FitiaPrimaryButton(
                    text = stringResource(R.string.training_create_plan),
                    onClick = { showCreate = true },
                )
            }
            if (onGeneratePlan != null) {
                item {
                    FitiaSecondaryButton(
                        text = stringResource(R.string.training_generate_plan_ai),
                        onClick = onGeneratePlan,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (plans.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.training_empty_plans),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(plans, key = { it.id }) { plan ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPlan(plan.id) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.training_plan_badge),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = plan.name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            text = if (plan.workouts.isEmpty()) {
                                stringResource(R.string.training_plan_no_workouts)
                            } else {
                                stringResource(
                                    R.string.training_plan_workouts_count,
                                    plan.workouts.size,
                                ) + " · " + plan.workouts.joinToString(
                                    separator = ", ",
                                    limit = 3,
                                    truncated = "…",
                                ) { it.name }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        if (onFollowPlan != null && plan.workouts.isNotEmpty()) {
                            if (activePlanId == plan.id) {
                                Text(
                                    text = stringResource(R.string.training_follow_plan_active_short),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                                FitiaSecondaryButton(
                                    text = stringResource(R.string.training_unfollow_plan),
                                    onClick = viewModel::stopFollowingPlan,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                )
                            } else {
                                FitiaSecondaryButton(
                                    text = stringResource(R.string.training_follow_plan),
                                    onClick = { viewModel.followPlan(plan.id, onFollowPlan) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                )
                            }
                        }
                    }
                    IconButton(modifier = Modifier.fitiaPress(), onClick = { viewModel.deletePlan(plan.id) }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                        )
                    }
                }
            }
        }
    }

    if (showCreate) {
        NameInputDialog(
            title = stringResource(R.string.training_create_plan),
            label = stringResource(R.string.field_name),
            onDismiss = { showCreate = false },
            onConfirm = { name ->
                viewModel.createPlan(name)
                showCreate = false
            },
        )
    }
}
