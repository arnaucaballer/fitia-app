package app.fitia.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.fitia.R
import app.fitia.design.fitiaPress

@Composable
fun ItemActionsDialog(
    title: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onRename: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    pinLabel: String? = null,
    onTogglePin: (() -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FitiaSpace.screen),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = FitiaCardShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(FitiaSpace.page()),
                    verticalArrangement = Arrangement.spacedBy(FitiaSpace.related),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(R.string.delete_dialog_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    )
                    if (onRename != null) {
                        ActionRow(
                            label = stringResource(R.string.action_rename),
                            onClick = onRename,
                        )
                    }
                    if (onEdit != null) {
                        ActionRow(
                            label = stringResource(R.string.action_edit),
                            onClick = onEdit,
                        )
                    }
                    if (onTogglePin != null && pinLabel != null) {
                        ActionRow(
                            label = pinLabel,
                            onClick = onTogglePin,
                            icon = Icons.Outlined.PushPin,
                        )
                    }
                    FitiaDangerButton(
                        text = stringResource(R.string.action_delete),
                        onClick = onDelete,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().fitiaPress()) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRow(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector = Icons.Outlined.Edit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FitiaPill)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = FitiaSpace.card, vertical = FitiaSpace.stack),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
        )
        Text(text = label, style = MaterialTheme.typography.titleMedium)
    }
}
