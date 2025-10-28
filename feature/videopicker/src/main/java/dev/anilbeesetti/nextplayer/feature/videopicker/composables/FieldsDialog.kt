package dev.anilbeesetti.nextplayer.feature.videopicker.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.CancelButton
import dev.anilbeesetti.nextplayer.core.ui.components.DoneButton
import dev.anilbeesetti.nextplayer.core.ui.components.NextDialog
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FieldsDialog(
    applicationPreferences: ApplicationPreferences,
    onDismiss: () -> Unit,
    updatePreferences: (ApplicationPreferences) -> Unit,
) {
    var preferences by remember { mutableStateOf(applicationPreferences) }

    NextDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                // Main title with icon
                Text(
                    text = stringResource(R.string.fields),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                // Optional subtitle explanation
                Text(
                    text = "Choose which fields to display",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        content = {
            HorizontalDivider(color = MaterialTheme.colorScheme.primaryContainer)
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
            ) {
                // Improved UI for Fields section
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(align = Alignment.Top),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FieldChip(
                        label = stringResource(id = R.string.duration),
                        icon = NextIcons.Length,
                        selected = preferences.showDurationField,
                        onClick = { preferences = preferences.copy(showDurationField = !preferences.showDurationField) },
                    )
                    FieldChip(
                        label = stringResource(id = R.string.extension),
                        icon = NextIcons.Style,
                        selected = preferences.showExtensionField,
                        onClick = { preferences = preferences.copy(showExtensionField = !preferences.showExtensionField) },
                    )
                    FieldChip(
                        label = stringResource(id = R.string.path),
                        icon = NextIcons.Location,
                        selected = preferences.showPathField,
                        onClick = { preferences = preferences.copy(showPathField = !preferences.showPathField) },
                    )
                    FieldChip(
                        label = stringResource(id = R.string.played_progress),
                        icon = NextIcons.Timer,
                        selected = preferences.showPlayedProgress,
                        onClick = { preferences = preferences.copy(showPlayedProgress = !preferences.showPlayedProgress) },
                    )
                    FieldChip(
                        label = stringResource(id = R.string.resolution),
                        icon = NextIcons.FontSize,
                        selected = preferences.showResolutionField,
                        onClick = { preferences = preferences.copy(showResolutionField = !preferences.showResolutionField) },
                    )
                    FieldChip(
                        label = stringResource(id = R.string.size),
                        icon = NextIcons.Size,
                        selected = preferences.showSizeField,
                        onClick = { preferences = preferences.copy(showSizeField = !preferences.showSizeField) },
                    )
                    FieldChip(
                        label = stringResource(id = R.string.thumbnail),
                        icon = NextIcons.Movie,
                        selected = preferences.showThumbnailField,
                        onClick = { preferences = preferences.copy(showThumbnailField = !preferences.showThumbnailField) },
                    )
                }
            }
        },
        confirmButton = {
            DoneButton(
                onClick = {
                    updatePreferences(preferences)
                    onDismiss()
                },
            )
        },
        dismissButton = {
            CancelButton(onClick = onDismiss)
        },
    )
}

@Composable
fun FieldChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Enhanced Field Chip with icon, label, and selection state
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            labelColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
        ),
        border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            selectedBorderWidth = 1.dp,
        ),
        modifier = modifier,
    )
}

@Preview
@Composable
fun FieldsDialogPreview() {
    Surface {
        FieldsDialog(applicationPreferences = ApplicationPreferences(), onDismiss = { }, updatePreferences = {})
    }
}
