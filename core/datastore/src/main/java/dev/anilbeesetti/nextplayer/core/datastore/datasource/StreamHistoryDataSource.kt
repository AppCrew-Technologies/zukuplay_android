package dev.anilbeesetti.nextplayer.core.datastore.datasource

import androidx.datastore.core.DataStore
import dev.anilbeesetti.nextplayer.core.model.StreamHistory
import dev.anilbeesetti.nextplayer.core.model.StreamHistoryItem
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class StreamHistoryDataSource @Inject constructor(
    private val streamHistoryDataStore: DataStore<StreamHistory>
) : PreferencesDataSource<StreamHistory> {

    override val preferences: Flow<StreamHistory> = streamHistoryDataStore.data
    
    // Convenience accessor for stream history items
    val streamHistory: Flow<List<StreamHistoryItem>> = streamHistoryDataStore.data.map { it.items }

    override suspend fun update(transform: suspend (StreamHistory) -> StreamHistory) {
        try {
            streamHistoryDataStore.updateData(transform)
        } catch (exception: Exception) {
            Timber.tag("NextPlayerStreamHistory").e("Failed to update stream history: $exception")
        }
    }
    
    /**
     * Add a new item to the stream history list
     */
    suspend fun addHistoryItem(item: StreamHistoryItem) {
        update { streamHistory ->
            // Add item to top, removing any existing entries with same URL
            val newItems = listOf(item) + streamHistory.items.filter { it.url != item.url }
            streamHistory.copy(items = newItems)
        }
    }
    
    /**
     * Delete a history item with the specified URL
     */
    suspend fun deleteHistoryItem(url: String) {
        update { streamHistory ->
            val newItems = streamHistory.items.filter { it.url != url }
            streamHistory.copy(items = newItems)
        }
    }
    
    /**
     * Clear all history items
     */
    suspend fun clearHistory() {
        update { it.copy(items = emptyList()) }
    }
} 