package dev.anilbeesetti.nextplayer.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anilbeesetti.nextplayer.core.data.repository.PreferencesRepository
import dev.anilbeesetti.nextplayer.core.model.StreamHistoryItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StreamViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val streamHistory: StateFlow<List<StreamHistoryItem>> = preferencesRepository.streamHistoryItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Add a new item to stream history
     */
    fun addToHistory(url: String, fileName: String) {
        viewModelScope.launch {
            preferencesRepository.addStreamHistoryItem(
                StreamHistoryItem(
                    url = url,
                    fileName = fileName
                )
            )
        }
    }

    /**
     * Delete an item from stream history
     */
    fun deleteHistoryItem(url: String) {
        viewModelScope.launch {
            preferencesRepository.deleteStreamHistoryItem(url)
        }
    }

    /**
     * Clear all stream history
     */
    fun clearHistory() {
        viewModelScope.launch {
            preferencesRepository.clearStreamHistory()
        }
    }
    
    // Helper function to extract file name from URL
    fun extractFileName(url: String): String {
        return try {
            // Try to extract filename from URL
            val uri = android.net.Uri.parse(url)
            val path = uri.path ?: ""
            val lastSlashIndex = path.lastIndexOf('/')
            if (lastSlashIndex != -1 && lastSlashIndex < path.length - 1) {
                val fileName = path.substring(lastSlashIndex + 1)
                // If file has an extension, use it
                if (fileName.isNotEmpty()) return fileName
            }
            // If no filename found, use "Stream Video"
            "Stream Video"
        } catch (e: Exception) {
            "Stream Video"
        }
    }
} 