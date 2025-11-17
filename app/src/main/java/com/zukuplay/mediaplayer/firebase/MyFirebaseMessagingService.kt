package com.zukuplay.mediaplayer.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.zukuplay.mediaplayer.R
import com.zukuplay.mediaplayer.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "nextplayer_notifications"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🔥 Firebase Messaging Service created")
        createNotificationChannel()
        
        // Subscribe to the "all" topic
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Successfully subscribed to 'all' topic")
                } else {
                    Log.e(TAG, "❌ Failed to subscribe to 'all' topic", task.exception)
                }
            }
        
        // Also subscribe to other common topics
        val topics = listOf("all", "active_users", "android_users")
        topics.forEach { topic ->
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "✅ Successfully subscribed to topic: $topic")
                    } else {
                        Log.e(TAG, "❌ Failed to subscribe to topic: $topic", task.exception)
                    }
                }
        }
        
        // Test notification to verify service is working
        Log.d(TAG, "🧪 Sending test notification to verify service is working")
//        showNotification(
//            title = "Firebase Service Test",
//            body = "Firebase Messaging Service is working!",
//            data = mapOf(
//                "test" to "true",
//                "from_notification" to "true",
//                "show_rich_popup" to "true"
//            )
//        )
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "🔑 New FCM token: $token")
        
        // Send token to your server or save to Firebase Firestore
        FirebaseManager.saveDeviceToken(token)
        
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "📨 Message received from: ${remoteMessage.from}")
        Log.d(TAG, "📨 Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "📨 Message Type: ${remoteMessage.messageType}")

        // Handle data payload if present
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "📊 Message data payload: ${remoteMessage.data}")
        }

//        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "📢 Message notification body: ${notification.body}")
            Log.d(TAG, "📢 Message notification title: ${notification.title}")
            showNotification(
                title = notification.title ?: "Notification",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }

//        // ALSO handle data-only messages (fallback)
//        if (remoteMessage.notification == null && remoteMessage.data.isNotEmpty()) {
//            Log.d(TAG, "📊 Data-only message received")
//            val title = remoteMessage.data["title"] ?: "Notification"
//            val body = remoteMessage.data["body"] ?: "New update available"
//            showNotification(title, body, remoteMessage.data)
//        }
//
//        // Handle both notification and data payload
//        if (remoteMessage.notification != null && remoteMessage.data.isNotEmpty()) {
//            Log.d(TAG, "📊 Message has both notification and data payload")
//            showNotification(
//                title = remoteMessage.notification?.title ?: "Notification",
//                body = remoteMessage.notification?.body ?: "",
//                data = remoteMessage.data
//            )
//        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NextPlayer Notifications",
                NotificationManager.IMPORTANCE_HIGH // Changed to HIGH for better visibility
            ).apply {
                description = "Notifications from NextPlayer"
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(true) // Allow notifications to bypass Do Not Disturb
                setSound(null, null) // Use default system sound
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "📱 Notification channel created with HIGH importance")
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        Log.d(TAG, "🔍 showNotification called with title: '$title', body: '$body'")
        Log.d(TAG, "🔍 showNotification data: $data")
        Log.d(TAG, "🔍 All data keys: ${data.keys.joinToString(", ")}")
        data.forEach { (key, value) ->
            Log.d(TAG, "   📦 $key = $value")
        }
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            // For popup content, prioritize data payload over notification payload
            val popupTitle = data["notification_title"] ?: title
            val popupBody = data["notification_body"] ?: body
            
            Log.d(TAG, "📝 System notification: title='$title', body='$body'")
            Log.d(TAG, "📝 Popup content: title='$popupTitle', body='$popupBody'")
            Log.d(TAG, "📝 Data contains notification_title: ${data.containsKey("notification_title")}")
            Log.d(TAG, "📝 Data notification_title value: '${data["notification_title"]}'")
            Log.d(TAG, "📝 Data contains notification_body: ${data.containsKey("notification_body")}")
            Log.d(TAG, "📝 Data notification_body value: '${data["notification_body"]}'")
            
            // Pass popup-specific data to the app
            putExtra("notification_title", popupTitle)
            putExtra("notification_body", popupBody)
            putExtra("from_notification", true)
            putExtra("show_rich_popup", true)
            putExtra("show_popup", true)
            
            Log.d(TAG, "📝 Setting intent extra notification_title = '$popupTitle'")
            Log.d(TAG, "📝 Setting intent extra notification_body = '$popupBody'")
            
            // Pass all other data fields (image, colors, CTA, etc.)
            data.forEach { (key, value) ->
                if (key != "notification_title" && key != "notification_body") {
                    putExtra(key, value)
                    Log.d(TAG, "📝 Setting intent extra $key = '$value'")
                }
            }
            
            // Explicitly log ctaLink for debugging
            data["ctaLink"]?.let { link ->
                Log.d(TAG, "🔗 CTA Link found: $link")
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), // Unique request code
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Simple notification that just opens the app - no action buttons needed
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setLights(0xFF0000FF.toInt(), 1000, 1000)
            .setDefaults(android.app.Notification.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
            )

        val notification = notificationBuilder.build()
        val notificationId = NOTIFICATION_ID + (System.currentTimeMillis() % 1000).toInt()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
        
        Log.d(TAG, "✅ Notification displayed with ID: $notificationId")
        Log.d(TAG, "✅ Simple notification displayed - will show rich popup in app")
        Log.d(TAG, "📊 Notification data will be passed to MainActivity for popup display")
        Log.d(TAG, "📊 Intent created with extras: ${intent.extras?.keySet()}")
    }
}


