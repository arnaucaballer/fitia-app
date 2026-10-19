package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    onBack: () -> Unit,
    onOpenPlan: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: PlansViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            PlansViewModel(
                observePlansUseCase = c.observePlansUseCase,
                savePlanUseCase = c.savePlanUseCase,
                deletePlanUseCase = c.deletePlanUseCase,
            )
        },
    )
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    var showCreate by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.training_plans_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_add))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(plan.name, style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = { viewModel.deletePlan(plan.id) }) {
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
