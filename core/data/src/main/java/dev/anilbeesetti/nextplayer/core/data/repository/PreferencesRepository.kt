package dev.anilbeesetti.nextplayer.core.data.repository

import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.MediaViewMode
import dev.anilbeesetti.nextplayer.core.model.PlayerPreferences
import dev.anilbeesetti.nextplayer.core.model.Sort
import dev.anilbeesetti.nextplayer.core.model.SortOrder
import dev.anilbeesetti.nextplayer.core.model.StreamHistory
import dev.anilbeesetti.nextplayer.core.model.StreamHistoryItem
import dev.anilbeesetti.nextplayer.core.model.ThemeConfig
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    /**
     * Stream of [ApplicationPreferences].
     */
    val applicationPreferences: Flow<ApplicationPreferences>

    /**
     * Stream of [PlayerPreferences].
     */
    val playerPreferences: Flow<PlayerPreferences>
    
    /**
     * Stream of [StreamHistory].
     */
    val streamHistory: Flow<StreamHistory>
    
    /**
     * Stream of stream history items.
     */
    val streamHistoryItems: Flow<List<StreamHistoryItem>>

    suspend fun updateApplicationPreferences(
        transform: suspend (ApplicationPreferences) -> ApplicationPreferences,
    )

    suspend fun updatePlayerPreferences(transform: suspend (PlayerPreferences) -> PlayerPreferences)
    
    suspend fun updateStreamHistory(transform: suspend (StreamHistory) -> StreamHistory)
    
    suspend fun addStreamHistoryItem(item: StreamHistoryItem)
    
    suspend fun deleteStreamHistoryItem(url: String)
    
    suspend fun clearStreamHistory()

    fun getPreferences(): Flow<ApplicationPreferences>
    
    suspend fun updateShowFloatingPlayButton(show: Boolean)
    suspend fun updateMarkLastPlayedMedia(mark: Boolean)
    suspend fun updateHighQualityThumbnails(highQuality: Boolean)
    suspend fun updateAutoScanLibrary(autoScan: Boolean)
    suspend fun updateGroupByFolder(groupByFolder: Boolean)
    suspend fun updateShowHiddenFiles(show: Boolean)
    suspend fun updateDefaultSortOrder(sortOrder: SortOrder)
    suspend fun updateExcludeList(excludeList: List<String>)
    suspend fun updateSortBy(sortBy: Sort.By)
    suspend fun updateSortOrder(sortOrder: Sort.Order)
    suspend fun updateThemeConfig(themeConfig: ThemeConfig)
    suspend fun updateUseHighContrastDarkTheme(use: Boolean)
    suspend fun updateUseDynamicColors(use: Boolean)
    suspend fun updateExcludeFolders(folders: List<String>)
    suspend fun updateMediaViewMode(mode: MediaViewMode)
    suspend fun updateShowDurationField(show: Boolean)
    suspend fun updateShowExtensionField(show: Boolean)
    suspend fun updateShowPathField(show: Boolean)
    suspend fun updateShowResolutionField(show: Boolean)
    suspend fun updateShowSizeField(show: Boolean)
    suspend fun updateShowThumbnailField(show: Boolean)
    suspend fun updateShowPlayedProgress(show: Boolean)
}
