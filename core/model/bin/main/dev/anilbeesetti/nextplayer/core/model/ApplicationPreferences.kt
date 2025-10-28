package dev.anilbeesetti.nextplayer.core.model

import kotlinx.serialization.Serializable
import dev.anilbeesetti.nextplayer.core.model.SortOrder

@Serializable
data class ApplicationPreferences(
    val sortBy: Sort.By = Sort.By.TITLE,
    val sortOrder: Sort.Order = Sort.Order.ASCENDING,
    val themeConfig: ThemeConfig = ThemeConfig.SYSTEM,
    val useHighContrastDarkTheme: Boolean = false,
    val useDynamicColors: Boolean = false,
    val markLastPlayedMedia: Boolean = false,
    val showFloatingPlayButton: Boolean = false,
    val excludeFolders: List<String> = emptyList(),
    val mediaViewMode: MediaViewMode = MediaViewMode.FOLDERS,
    val highQualityThumbnails: Boolean = false,
    val autoScanLibrary: Boolean = false,
    val groupByFolder: Boolean = false,
    val showHiddenFiles: Boolean = false,
    val defaultSortOrder: SortOrder = SortOrder.NAME,

    // Fields
    val showDurationField: Boolean = true,
    val showExtensionField: Boolean = false,
    val showPathField: Boolean = true,
    val showResolutionField: Boolean = false,
    val showSizeField: Boolean = false,
    val showThumbnailField: Boolean = true,
    val showPlayedProgress: Boolean = true,
    val excludeList: List<String> = emptyList(),
)
