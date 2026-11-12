package app.fitia.feature.chat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.fitia.R
import app.fitia.design.FitiaCard
import app.fitia.design.FitiaSpace
import app.fitia.design.FitiaDeleteDialog
import app.fitia.design.FitiaPrimaryButton
import app.fitia.design.ItemActionsDialog
import app.fitia.design.fitiaPress
import app.fitia.feature.chat.domain.ChatThread
import app.fitia.feature.training.ui.NameInputDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ChatHistoryList(
    threads: List<ChatThread>,
    interactionEnabled: Boolean,
    onOpen: (ChatThread) -> Unit,
    onNewChat: () -> Unit,
    onRename: (ChatThread, String) -> Unit,
    onTogglePin: (ChatThread) -> Unit,
    onDelete: (ChatThread) -> Unit,
    modifier: Modifier = Modifier,
) {
    var actionsFor by remember { mutableStateOf<ChatThread?>(null) }
    var renameFor by remember { mutableStateOf<ChatThread?>(null) }
    var deleteFor by remember { mutableStateOf<ChatThread?>(null) }
    val pinned = threads.filter { it.pinned }
    val recent = threads.filter { !it.pinned }
    val showSections = pinned.isNotEmpty() && recent.isNotEmpty()
    val longPressLabel = stringResource(R.string.chat_history_actions)

    if (threads.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.chat_history_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            FitiaPrimaryButton(
                text = stringResource(R.string.chat_new),
                onClick = onNewChat,
                enabled = interactionEnabled,
                modifier = Modifier.padding(top = FitiaSpace.card),
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = FitiaSpace.screen),
            verticalArrangement = Arrangement.spacedBy(FitiaSpace.stack),
        ) {
            if (showSections) {
                item(key = "pinned-header") {
                    SectionLabel(stringResource(R.string.chat_pinned_section))
                }
            }
            items(pinned, key = { it.id }) { thread ->
                ChatThreadRow(
                    thread = thread,
                    longPressLabel = longPressLabel,
                    onOpen = { if (interactionEnabled) onOpen(thread) },
                    onLongClick = { actionsFor = thread },
                )
            }
            if (showSections) {
                item(key = "recent-header") {
                    SectionLabel(stringResource(R.string.chat_recent_section))
                }
            }
            items(recent, key = { it.id }) { thread ->
                ChatThreadRow(
                    thread = thread,
                    longPressLabel = longPressLabel,
                    onOpen = { if (interactionEnabled) onOpen(thread) },
                    onLongClick = { actionsFor = thread },
                )
            }
        }
    }

    actionsFor?.let { thread ->
        ItemActionsDialog(
            title = thread.title,
            onDismiss = { actionsFor = null },
            onRename = {
                actionsFor = null
                renameFor = thread
            },
            pinLabel = stringResource(
                if (thread.pinned) R.string.chat_unpin else R.string.chat_pin,
            ),
            onTogglePin = {
                actionsFor = null
                onTogglePin(thread)
            },
            onDelete = {
                actionsFor = null
                deleteFor = thread
            },
        )
    }
    renameFor?.let { thread ->
        NameInputDialog(
            title = stringResource(R.string.chat_rename),
            initialValue = thread.title,
            label = stringResource(R.string.chat_name),
            onDismiss = { renameFor = null },
            onConfirm = { name ->
                renameFor = null
                if (name.isNotBlank()) onRename(thread, name)
            },
        )
    }
    deleteFor?.let { thread ->
        FitiaDeleteDialog(
            title = stringResource(R.string.chat_delete_title),
            message = stringResource(R.string.chat_delete_body),
            onDismiss = { deleteFor = null },
            onConfirm = {
                deleteFor = null
                onDelete(thread)
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = FitiaSpace.related),
    )
}

@Composable
private fun ChatThreadRow(
    thread: ChatThread,
    longPressLabel: String,
    onOpen: () -> Unit,
    onLongClick: () -> Unit,
) {
    FitiaCard(
        onClick = onOpen,
        onLongClick = onLongClick,
        onLongClickLabel = longPressLabel,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = thread.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (thread.pinned) {
                Icon(
                    imageVector = Icons.Outlined.PushPin,
                    contentDescription = stringResource(R.string.chat_pinned),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(
                onClick = onLongClick,
                modifier = Modifier.fitiaPress(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = longPressLabel,
                )
            }
        }
        Text(
            text = chatTimeLabel(thread.updatedAtEpochMs),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun chatTimeLabel(epochMs: Long): String {
    val zone = ZoneId.systemDefault()
    val dateTime = Instant.ofEpochMilli(epochMs).atZone(zone)
    val day = dateTime.toLocalDate()
    val time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    return when (day) {
        LocalDate.now(zone) -> stringResource(R.string.chat_time_today, time)
        LocalDate.now(zone).minusDays(1) -> stringResource(R.string.chat_time_yesterday, time)
        else -> dateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES")))
    }
}
