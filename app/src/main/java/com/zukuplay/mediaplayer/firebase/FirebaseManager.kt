package com.zukuplay.mediaplayer.firebase

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private const val DEVICE_TOKENS_COLLECTION = "device_tokens"
    
    private lateinit var firestore: FirebaseFirestore
    private var isInitialized = false
    
    fun initialize(context: Context) {
        if (isInitialized) {
            Log.d(TAG, "🔥 Firebase already initialized")
            return
        }
        
        try {
            Log.d(TAG, "🔥 Starting Firebase initialization...")
            
            // Initialize Firebase if not already done
            if (FirebaseApp.getApps(context).isEmpty()) {
                Log.d(TAG, "🔥 Initializing Firebase app...")
                FirebaseApp.initializeApp(context)
            } else {
                Log.d(TAG, "🔥 Firebase app already initialized")
            }
            
            firestore = FirebaseFirestore.getInstance()
            isInitialized = true
            
            Log.d(TAG, "🔥 Firebase initialized successfully")
            
            // Get and save FCM token
            getAndSaveToken()
            
            // Subscribe to topics
            subscribeToTopics()
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error initializing Firebase", e)
        }
    }
    
    private fun getAndSaveToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "❌ Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.d(TAG, "🔑 FCM Token: $token")
                
                // Save token to Firestore
                saveDeviceToken(token)
            }
    }
    
    fun saveDeviceToken(token: String) {
        if (!isInitialized) {
            Log.w(TAG, "⚠️ Firebase not initialized, cannot save token")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceData = mapOf(
                    "token" to token,
                    "platform" to "android",
                    "deviceModel" to Build.MODEL,
                    "osVersion" to Build.VERSION.RELEASE,
                    "appVersion" to "1.0.7", // From build.gradle.kts
                    "lastUpdated" to Date(),
                    "isActive" to true
                )
                
                firestore.collection(DEVICE_TOKENS_COLLECTION)
                    .document(token)
                    .set(deviceData)
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ Device token saved successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Error saving device token", e)
                    }
                    
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in saveDeviceToken", e)
            }
        }
    }
    
    private fun subscribeToTopics() {
        val topics = listOf("all", "active_users", "android_users")
        
        topics.forEach { topic ->
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "✅ Subscribed to topic: $topic")
                    } else {
                        Log.e(TAG, "❌ Failed to subscribe to topic: $topic", task.exception)
                    }
                }
        }
    }
    
    fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Unsubscribed from topic: $topic")
                } else {
                    Log.e(TAG, "❌ Failed to unsubscribe from topic: $topic", task.exception)
                }
            }
    }
    
    fun markTokenInactive(token: String) {
        if (!isInitialized) return
        
        firestore.collection(DEVICE_TOKENS_COLLECTION)
            .document(token)
            .update("isActive", false, "lastUpdated", Date())
            .addOnSuccessListener {
                Log.d(TAG, "✅ Token marked as inactive")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error marking token inactive", e)
            }
    }
}


