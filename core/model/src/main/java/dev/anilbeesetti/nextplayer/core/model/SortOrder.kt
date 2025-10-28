package dev.anilbeesetti.nextplayer.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class SortOrder {
    NAME,
    DATE_ADDED,
    DATE_MODIFIED,
    SIZE,
    DURATION
} 