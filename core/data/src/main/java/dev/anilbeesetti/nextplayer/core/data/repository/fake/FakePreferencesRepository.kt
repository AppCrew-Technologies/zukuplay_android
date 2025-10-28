package dev.anilbeesetti.nextplayer.core.data.repository.fake

import dev.anilbeesetti.nextplayer.core.data.repository.PreferencesRepository
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.MediaViewMode
import dev.anilbeesetti.nextplayer.core.model.PlayerPreferences
import dev.anilbeesetti.nextplayer.core.model.Sort
import dev.anilbeesetti.nextplayer.core.model.SortOrder
import dev.anilbeesetti.nextplayer.core.model.StreamHistory
import dev.anilbeesetti.nextplayer.core.model.StreamHistoryItem
import dev.anilbeesetti.nextplayer.core.model.ThemeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakePreferencesRepository : PreferencesRepository {

    private val applicationPreferencesStateFlow = MutableStateFlow(ApplicationPreferences())
    private val playerPreferencesStateFlow = MutableStateFlow(PlayerPreferences())
    private val streamHistoryStateFlow = MutableStateFlow(StreamHistory())

    override val applicationPreferences: Flow<ApplicationPreferences>
        get() = applicationPreferencesStateFlow
    override val playerPreferences: Flow<PlayerPreferences>
        get() = playerPreferencesStateFlow
    override val streamHistory: Flow<StreamHistory>
        get() = streamHistoryStateFlow
    override val streamHistoryItems: Flow<List<StreamHistoryItem>>
        get() = streamHistoryStateFlow.map { it.items }

    override fun getPreferences(): Flow<ApplicationPreferences> = applicationPreferences

    override suspend fun updateApplicationPreferences(
        transform: suspend (ApplicationPreferences) -> ApplicationPreferences,
    ) {
        applicationPreferencesStateFlow.update { transform.invoke(it) }
    }

    override suspend fun updatePlayerPreferences(
        transform: suspend (PlayerPreferences) -> PlayerPreferences,
    ) {
        playerPreferencesStateFlow.update { transform.invoke(it) }
    }
    
    override suspend fun updateStreamHistory(
        transform: suspend (StreamHistory) -> StreamHistory
    ) {
        streamHistoryStateFlow.update { transform.invoke(it) }
    }
    
    override suspend fun addStreamHistoryItem(item: StreamHistoryItem) {
        updateStreamHistory { streamHistory ->
            val newItems = listOf(item) + streamHistory.items.filter { it.url != item.url }
            streamHistory.copy(items = newItems)
        }
    }
    
    override suspend fun deleteStreamHistoryItem(url: String) {
        updateStreamHistory { streamHistory ->
            val newItems = streamHistory.items.filter { it.url != url }
            streamHistory.copy(items = newItems)
        }
    }
    
    override suspend fun clearStreamHistory() {
        updateStreamHistory { it.copy(items = emptyList()) }
    }

    override suspend fun updateShowFloatingPlayButton(show: Boolean) {
        updateApplicationPreferences { it.copy(showFloatingPlayButton = show) }
    }

    override suspend fun updateMarkLastPlayedMedia(mark: Boolean) {
        updateApplicationPreferences { it.copy(markLastPlayedMedia = mark) }
    }

    override suspend fun updateHighQualityThumbnails(highQuality: Boolean) {
        updateApplicationPreferences { it.copy(highQualityThumbnails = highQuality) }
    }

    override suspend fun updateAutoScanLibrary(autoScan: Boolean) {
        updateApplicationPreferences { it.copy(autoScanLibrary = autoScan) }
    }

    override suspend fun updateGroupByFolder(groupByFolder: Boolean) {
        updateApplicationPreferences { it.copy(groupByFolder = groupByFolder) }
    }

    override suspend fun updateShowHiddenFiles(show: Boolean) {
        updateApplicationPreferences { it.copy(showHiddenFiles = show) }
    }

    override suspend fun updateDefaultSortOrder(sortOrder: SortOrder) {
        updateApplicationPreferences { it.copy(defaultSortOrder = sortOrder) }
    }

    override suspend fun updateExcludeList(excludeList: List<String>) {
        updateApplicationPreferences { it.copy(excludeList = excludeList) }
    }

    override suspend fun updateSortBy(sortBy: Sort.By) {
        updateApplicationPreferences { it.copy(sortBy = sortBy) }
    }

    override suspend fun updateSortOrder(sortOrder: Sort.Order) {
        updateApplicationPreferences { it.copy(sortOrder = sortOrder) }
    }

    override suspend fun updateThemeConfig(themeConfig: ThemeConfig) {
        updateApplicationPreferences { it.copy(themeConfig = themeConfig) }
    }

    override suspend fun updateUseHighContrastDarkTheme(use: Boolean) {
        updateApplicationPreferences { it.copy(useHighContrastDarkTheme = use) }
    }

    override suspend fun updateUseDynamicColors(use: Boolean) {
        updateApplicationPreferences { it.copy(useDynamicColors = use) }
    }

    override suspend fun updateExcludeFolders(folders: List<String>) {
        updateApplicationPreferences { it.copy(excludeFolders = folders) }
    }

    override suspend fun updateMediaViewMode(mode: MediaViewMode) {
        updateApplicationPreferences { it.copy(mediaViewMode = mode) }
    }

    override suspend fun updateShowDurationField(show: Boolean) {
        updateApplicationPreferences { it.copy(showDurationField = show) }
    }

    override suspend fun updateShowExtensionField(show: Boolean) {
        updateApplicationPreferences { it.copy(showExtensionField = show) }
    }

    override suspend fun updateShowPathField(show: Boolean) {
        updateApplicationPreferences { it.copy(showPathField = show) }
    }

    override suspend fun updateShowResolutionField(show: Boolean) {
        updateApplicationPreferences { it.copy(showResolutionField = show) }
    }

    override suspend fun updateShowSizeField(show: Boolean) {
        updateApplicationPreferences { it.copy(showSizeField = show) }
    }

    override suspend fun updateShowThumbnailField(show: Boolean) {
        updateApplicationPreferences { it.copy(showThumbnailField = show) }
    }

    override suspend fun updateShowPlayedProgress(show: Boolean) {
        updateApplicationPreferences { it.copy(showPlayedProgress = show) }
    }
}
