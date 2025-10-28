package dev.anilbeesetti.nextplayer.notifications

/**
 * Basic notification types for NextPlayer
 */
enum class NotificationType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    UPDATE,
    SECURITY
}

enum class NotificationPriority(val value: Int) {
    MIN(-2),
    LOW(-1),
    NORMAL(0),
    HIGH(1),
    MAX(2)
}

enum class NotificationSound(val soundName: String) {
    SILENT("silent"),
    DEFAULT("default"),
    SUCCESS("success"),
    WARNING("warning"), 
    ERROR("error"),
    UPDATE("update"),
    SECURITY("security")
}

/**
 * Notification behavior configuration
 */
data class NotificationBehavior(
    val collapsible: Boolean = true,
    val timeToLive: Long? = null, // in seconds, null means use default (28 days)
    val restrictedPackageName: String? = null,
    val dryRun: Boolean = false,
    val directBootAware: Boolean = false,
    val collapseKey: String? = null,
    val threadId: String? = null, // for grouping related notifications
    val tag: String? = null
)

/**
 * Rich notification content types
 */
enum class NotificationContentType {
    TEXT_ONLY,
    BIG_TEXT,
    BIG_PICTURE,
    INBOX_STYLE,
    MESSAGING_STYLE,
    MEDIA_STYLE,
    CUSTOM_VIEW
}

/**
 * Notification interaction types
 */
enum class NotificationAction(val actionId: String, val title: String) {
    REPLY("reply", "Reply"),
    MARK_READ("mark_read", "Mark as Read"),
    DELETE("delete", "Delete"),
    SHARE("share", "Share"),
    DISMISS("dismiss", "Dismiss"),
    OPEN_APP("open_app", "Open App")
}

/**
 * Delivery options for FCM messages
 */
data class DeliveryOptions(
    val priority: String = "normal", // "normal" or "high"
    val timeToLive: Long? = null, // TTL in seconds
    val collapseKey: String? = null,
    val restrictedPackageName: String? = null,
    val dryRun: Boolean = false,
    val directBootAware: Boolean = false
)

/**
 * Topic configuration for topic messaging
 */
data class TopicConfig(
    val topicName: String,
    val condition: String? = null, // For conditional topic messaging
    val subscriberCount: Int = 0
)

/**
 * Analytics and tracking configuration
 */
data class NotificationAnalytics(
    val trackOpens: Boolean = true,
    val trackClicks: Boolean = true,
    val trackDismissals: Boolean = true,
    val customParameters: Map<String, String> = emptyMap()
) 