package dev.anilbeesetti.nextplayer.feature.videopicker.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import dev.anilbeesetti.nextplayer.core.common.Utils
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.ListItemComponent
import dev.anilbeesetti.nextplayer.core.ui.theme.VipulPlayerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderItem(
    folder: Folder,
    isRecentlyPlayedFolder: Boolean,
    preferences: ApplicationPreferences,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isRecentlyPlayedFolder && preferences.markLastPlayedMedia) {
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        } else {
                            listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
) {
    ListItemComponent(
        colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
            headlineColor = if (isRecentlyPlayedFolder && preferences.markLastPlayedMedia) {
                MaterialTheme.colorScheme.primary
            } else {
                        MaterialTheme.colorScheme.onSurface
            },
            supportingColor = if (isRecentlyPlayedFolder && preferences.markLastPlayedMedia) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
            },
        ),
        leadingContent = {
                    Box(
                        modifier = Modifier
                            .width(min(90.dp, LocalConfiguration.current.screenWidthDp.dp * 0.3f))
                            .aspectRatio(20 / 17f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (isRecentlyPlayedFolder && preferences.markLastPlayedMedia) {
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                        )
                                    } else {
                                        listOf(
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                                        )
                                    }
                                )
                            )
                    ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.folder_thumb),
                contentDescription = "",
                tint = if (isRecentlyPlayedFolder && preferences.markLastPlayedMedia) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
                modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(20 / 17f)
            )
                    }
        },
        headlineContent = {
            Text(
                text = folder.name,
                maxLines = 2,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isRecentlyPlayedFolder && preferences.markLastPlayedMedia) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Medium
                            }
                        ),
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            if (preferences.showPathField) {
                Text(
                    text = folder.path.substringBeforeLast("/"),
                    maxLines = 2,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                if (folder.mediaList.isNotEmpty()) {
                    InfoChip(
                        text = "${folder.mediaList.size} " +
                            stringResource(id = R.string.video.takeIf { folder.mediaList.size == 1 } ?: R.string.videos),
                    )
                }
                if (folder.folderList.isNotEmpty()) {
                    InfoChip(
                        text = "${folder.folderList.size} " +
                            stringResource(id = R.string.folder.takeIf { folder.folderList.size == 1 } ?: R.string.folders),
                    )
                }
                if (preferences.showSizeField) {
                    InfoChip(text = Utils.formatFileSize(folder.mediaSize))
                }
            }
        },
    )
        }
    }
}

@PreviewLightDark
@Composable
fun FolderItemRecentlyPlayedPreview() {
    VipulPlayerTheme {
        FolderItem(
            folder = Folder.sample,
            preferences = ApplicationPreferences(),
            isRecentlyPlayedFolder = true,
        )
    }
}

@PreviewLightDark
@Composable
fun FolderItemPreview() {
    VipulPlayerTheme {
        FolderItem(
            folder = Folder.sample,
            preferences = ApplicationPreferences(),
            isRecentlyPlayedFolder = false,
        )
    }
}
