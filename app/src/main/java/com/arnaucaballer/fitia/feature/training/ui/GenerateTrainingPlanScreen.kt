package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.di.fitiaContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateTrainingPlanScreen(
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    var goal by remember { mutableStateOf("Hipertrofia") }
    var days by remember { mutableStateOf("4") }
    var level by remember { mutableStateOf("Intermedio") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.training_generate_plan_ai)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.training_generate_plan_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = goal,
                onValueChange = { goal = it },
                label = { Text(stringResource(R.string.training_goal_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = days,
                onValueChange = { days = it.filter { c -> c.isDigit() }.take(1) },
                label = { Text(stringResource(R.string.training_days_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = level,
                onValueChange = { level = it },
                label = { Text(stringResource(R.string.training_level_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                FitiaPrimaryButton(
                    text = stringResource(R.string.generate),
                    onClick = {
                        loading = true
                        error = null
                        scope.launch {
                            runCatching {
                                container.trainingPlanGenerator.generate(
                                    goal = goal,
                                    daysPerWeek = days.toIntOrNull() ?: 4,
                                    level = level,
                                )
                            }.onSuccess { id ->
                                loading = false
                                onCreated(id)
                            }.onFailure { t ->
                                loading = false
                                error = t.message ?: "Error"
                            }
                        }
                    },
                )
            }
        }
    }
}
