package dev.anilbeesetti.nextplayer.feature.videopicker.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.Sort
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.CancelButton
import dev.anilbeesetti.nextplayer.core.ui.components.DoneButton
import dev.anilbeesetti.nextplayer.core.ui.components.NextDialog
import dev.anilbeesetti.nextplayer.core.ui.components.NextSwitch
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSettingsDialog(
    applicationPreferences: ApplicationPreferences,
    onDismiss: () -> Unit,
    updatePreferences: (ApplicationPreferences) -> Unit,
) {
    var preferences by remember { mutableStateOf(applicationPreferences) }

    NextDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.quick_settings),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        content = {
            HorizontalDivider(color = MaterialTheme.colorScheme.primaryContainer)
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
            ) {
                DialogSectionTitle(text = stringResource(R.string.sort))

                // Enhanced Sort Options with cards
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Sort types section
                    Text(
                        text = "Sort By",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                    )

                    // Enhanced sort options with better visual design
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SortOptionCard(
                            text = stringResource(id = R.string.title),
                            icon = NextIcons.Title,
                            isSelected = preferences.sortBy == Sort.By.TITLE,
                            onClick = { preferences = preferences.copy(sortBy = Sort.By.TITLE) },
                        )

                        SortOptionCard(
                            text = stringResource(id = R.string.duration),
                            icon = NextIcons.Length,
                            isSelected = preferences.sortBy == Sort.By.LENGTH,
                            onClick = { preferences = preferences.copy(sortBy = Sort.By.LENGTH) },
                        )

                        SortOptionCard(
                            text = stringResource(id = R.string.date),
                            icon = NextIcons.Calendar,
                            isSelected = preferences.sortBy == Sort.By.DATE,
                            onClick = { preferences = preferences.copy(sortBy = Sort.By.DATE) },
                        )

                        SortOptionCard(
                            text = stringResource(id = R.string.size),
                            icon = NextIcons.Size,
                            isSelected = preferences.sortBy == Sort.By.SIZE,
                            onClick = { preferences = preferences.copy(sortBy = Sort.By.SIZE) },
                        )

                        SortOptionCard(
                            text = stringResource(id = R.string.location),
                            icon = NextIcons.Location,
                            isSelected = preferences.sortBy == Sort.By.PATH,
                            onClick = { preferences = preferences.copy(sortBy = Sort.By.PATH) },
                        )
                    }

                    // Sort direction section
                    Text(
                        text = "Sort Order",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
                    )

                    // Enhanced sort order selection
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            SortDirectionButton(
                                text = stringResource(id = R.string.ascending),
                                icon = NextIcons.ArrowUpward,
                                isSelected = preferences.sortOrder == Sort.Order.ASCENDING,
                                onClick = { preferences = preferences.copy(sortOrder = Sort.Order.ASCENDING) },
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            SortDirectionButton(
                                text = stringResource(id = R.string.descending),
                                icon = NextIcons.ArrowDownward,
                                isSelected = preferences.sortOrder == Sort.Order.DESCENDING,
                                onClick = { preferences = preferences.copy(sortOrder = Sort.Order.DESCENDING) },
                            )
                        }
                    }
                }

                // Fields section removed - now in a separate FieldsDialog
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortOptionCard(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    ElevatedCard(
        modifier = modifier
            .height(88.dp)
            .widthIn(min = 72.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp,
        ),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else contentColor,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )

            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
}

@Composable
private fun SortDirectionButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else contentColor,
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun DialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
fun DialogPreferenceSwitch(
    text: String,
    isChecked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = isChecked,
                enabled = enabled,
                onValueChange = { onClick() },
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        NextSwitch(
            checked = isChecked,
            onCheckedChange = null,
            modifier = Modifier.padding(start = 20.dp),
            enabled = enabled,
        )
    }
}

@Preview
@Composable
fun QuickSettingsPreview() {
    Surface {
        QuickSettingsDialog(applicationPreferences = ApplicationPreferences(), onDismiss = { }, updatePreferences = {})
    }
}
