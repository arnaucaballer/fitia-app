package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory
import com.arnaucaballer.fitia.feature.training.domain.SessionStatus
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onOpenSession: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: HistoryViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            HistoryViewModel(observeSessionsUseCase = c.observeSessionsUseCase)
        },
    )
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.training_history_title)) },
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
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (sessions.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.training_empty_history),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(sessions, key = { it.id }) { session ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSession(session.id) }
                        .padding(vertical = 8.dp),
                ) {
                    Text(session.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = dateFormat.format(Date(session.startedAtEpochMs)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (session.status == SessionStatus.Completed) {
                            stringResource(R.string.training_status_completed)
                        } else {
                            stringResource(R.string.training_status_in_progress)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
