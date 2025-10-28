package dev.anilbeesetti.nextplayer.core.model

import kotlinx.serialization.Serializable

/**
 * Data class representing stream history preferences with default values
 */
@Serializable
data class StreamHistory(
    val items: List<StreamHistoryItem> = emptyList()
)

/**
 * Data class representing a single stream history item
 */
@Serializable
data class StreamHistoryItem(
    val url: String,
    val fileName: String,
    val timestamp: Long = System.currentTimeMillis()
) 