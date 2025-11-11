package com.zukuplay.mediaplayer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.Constants
import dagger.hilt.android.AndroidEntryPoint
import dev.anilbeesetti.nextplayer.core.ads.AdConstants
import dev.anilbeesetti.nextplayer.core.ads.AdManager
import dev.anilbeesetti.nextplayer.core.ads.components.BannerAdView
import dev.anilbeesetti.nextplayer.core.ads.models.AdPlacement
import dev.anilbeesetti.nextplayer.core.data.repository.PreferencesRepository
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.ThemeConfig
import dev.anilbeesetti.nextplayer.core.ui.components.NotificationPopup
import dev.anilbeesetti.nextplayer.core.ui.components.NotificationPopupData
import dev.anilbeesetti.nextplayer.core.ui.theme.VipulPlayerTheme
import dev.anilbeesetti.nextplayer.navigation.MEDIA_ROUTE
import dev.anilbeesetti.nextplayer.navigation.MUSIC_ROUTE
import dev.anilbeesetti.nextplayer.navigation.SETTINGS_ROUTE
import dev.anilbeesetti.nextplayer.navigation.STREAM_ROUTE
import dev.anilbeesetti.nextplayer.navigation.NextPlayerBottomBar
import dev.anilbeesetti.nextplayer.navigation.mediaNavGraph
import dev.anilbeesetti.nextplayer.navigation.musicNavGraph
import dev.anilbeesetti.nextplayer.navigation.settingsNavGraph
import dev.anilbeesetti.nextplayer.navigation.streamNavGraph
import dev.anilbeesetti.nextplayer.navigation.startAudioPlayerActivity
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var adManager: AdManager
    
    @Inject
    lateinit var preferencesRepository: PreferencesRepository
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    // Permission state to track what's been requested
    private var allPermissionsRequested = false
    
    // Function to update popup state from outside Compose
    private var _popupStateUpdater: ((NotificationPopupData?) -> Unit)? = null
    
    // Comprehensive permission launcher for all permissions at once
    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        allPermissionsRequested = true
        
        permissions.entries.forEach { (permission, isGranted) ->
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    if (isGranted) {
                        Log.d(TAG, "✅ Notification permission granted")
                    } else {
                        Log.w(TAG, "❌ Notification permission denied")
                    }
                }
                Manifest.permission.READ_MEDIA_VIDEO -> {
                    if (isGranted) {
                        Log.d(TAG, "✅ Video permission granted")
                    } else {
                        Log.w(TAG, "❌ Video permission denied")
                    }
                }
                Manifest.permission.READ_MEDIA_AUDIO -> {
                    if (isGranted) {
                        Log.d(TAG, "✅ Audio permission granted")
                    } else {
                        Log.w(TAG, "❌ Audio permission denied")
                    }
                }
                Manifest.permission.READ_EXTERNAL_STORAGE -> {
                    if (isGranted) {
                        Log.d(TAG, "✅ External storage permission granted")
                    } else {
                        Log.w(TAG, "❌ External storage permission denied")
                    }
                }
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    if (isGranted) {
                        Log.d(TAG, "✅ Write external storage permission granted")
                    } else {
                        Log.w(TAG, "❌ Write external storage permission denied")
                    }
                }
            }
        }
        
        val grantedCount = permissions.values.count { it }
        val totalCount = permissions.size
        Log.i(TAG, "🎯 Permission Summary: $grantedCount/$totalCount permissions granted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display for proper handling of different screen sizes
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Ensure screenshots are allowed by clearing FLAG_SECURE if it's set
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        
        // ⭐ REQUEST ALL PERMISSIONS UPFRONT - This is the key change!
        requestAllPermissionsUpfront()
        
        // Initialize AdMob - use runBlocking for now
        kotlinx.coroutines.runBlocking {
            adManager.initialize(this@MainActivity)
        }
        
        setContent {
            // Create properly observed Compose state
            var shouldShowNotificationPopup by remember { mutableStateOf(false) }
            var notificationPopupData by remember { mutableStateOf<NotificationPopupData?>(null) }
            
            // Create a function to update state from outside Compose
            val updatePopupState: (NotificationPopupData?) -> Unit = { popupData ->
                Log.d(TAG, "📝 Updating popup state from intent: ${popupData?.title}")
                notificationPopupData = popupData
                shouldShowNotificationPopup = popupData != null
                Log.d(TAG, "📝 Popup state updated: shouldShow=$shouldShowNotificationPopup")
            }
            
            // Store the update function globally so onNewIntent can use it
            _popupStateUpdater = updatePopupState
            
            // Handle initial intent
            LaunchedEffect(Unit) {
                Log.d(TAG, "🔍 Handling initial intent on app start")
                handleNotificationIntent(intent, updatePopupState)
            }
            
            NextPlayerApp(
                shouldShowNotificationPopup = shouldShowNotificationPopup,
                notificationPopupData = notificationPopupData,
                onDismissPopup = { 
                    shouldShowNotificationPopup = false
                    notificationPopupData = null
                    Log.d(TAG, "🎯 Popup dismissed by user")
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        // Show App Open ad on app launch/start. SDK provides the close (X) control.
//        adManager.showAppOpenAd(
//            context = this
//        )
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the activity's intent
        Log.d(TAG, "🔄 New intent received, handling notification data")
        
        _popupStateUpdater?.let { updater ->
            Log.d(TAG, "📝 Popup state updater is available, calling handleNotificationIntent")
            handleNotificationIntent(intent, updater)
        } ?: run {
            Log.w(TAG, "⚠️ Popup state updater is null! Cannot update popup state from intent.")
        }
    }
    
    private fun handleNotificationIntent(intent: Intent, updatePopupState: (NotificationPopupData?) -> Unit) {
        val extras = intent.extras
        if (extras != null) {
            Log.d(TAG, "=== NOTIFICATION INTENT DATA ===")
            for (key in extras.keySet()) {
                val value = extras.get(key)
                Log.d(TAG, "Key: $key, Value: $value, Type: ${value?.javaClass?.simpleName}")
            }
            Log.d(TAG, "===============================")
            
            // Check if this is from a notification and should show rich popup
            // Handle both boolean and string values for compatibility
            val fromNotification = extras.getBoolean("from_notification", false) || 
                                 extras.getString("from_notification") == "true"
            val showRichPopup = extras.getBoolean("show_rich_popup", false) || 
                              extras.getString("show_rich_popup") == "true" ||
                              extras.getBoolean("show_popup", false) || 
                              extras.getString("show_popup") == "true"
            
            Log.d(TAG, "🔍 fromNotification: $fromNotification, showRichPopup: $showRichPopup")
            
            if (fromNotification && showRichPopup) {
                Log.d(TAG, "🎨 Preparing to show rich notification popup")
                
                // Extract notification data for rich popup
                val title = extras.getString("notification_title") ?: "Notification"
                val body = extras.getString("notification_body") ?: "New update available"
                val imageUrl = extras.getString("image_url")
                val ctaText = extras.getString("cta_text")
                val ctaLink = extras.getString("cta_link")
                val backgroundColor = extras.getString("background_color")
                val textColor = extras.getString("text_color")
                
                Log.d(TAG, "🎯 Popup data extracted: title='$title', body='$body', cta='$ctaText', bg='$backgroundColor'")
                
                // Parse additional buttons
                val buttons = mutableListOf<dev.anilbeesetti.nextplayer.core.ui.components.PopupButton>()
                val buttonsCount = extras.getString("buttons_count")?.toIntOrNull() ?: 0
                
                for (i in 0 until buttonsCount) {
                    val buttonText = extras.getString("button_${i}_text")
                    val buttonLink = extras.getString("button_${i}_link")
                    val buttonStyle = extras.getString("button_${i}_style") ?: "PRIMARY"
                    
                    if (!buttonText.isNullOrBlank() && !buttonLink.isNullOrBlank()) {
                        val style = when (buttonStyle) {
                            "SECONDARY" -> dev.anilbeesetti.nextplayer.core.ui.components.ButtonStyle.SECONDARY
                            "OUTLINE" -> dev.anilbeesetti.nextplayer.core.ui.components.ButtonStyle.OUTLINE
                            else -> dev.anilbeesetti.nextplayer.core.ui.components.ButtonStyle.PRIMARY
                        }
                        buttons.add(dev.anilbeesetti.nextplayer.core.ui.components.PopupButton(buttonText, buttonLink, style))
                    }
                }
                
                // Create notification data and set state directly
                val notificationData = NotificationPopupData(
                    title = title,
                    body = body,
                    imageUrl = imageUrl,
                    ctaText = ctaText,
                    ctaLink = ctaLink,
                    buttons = buttons,
                    backgroundColor = backgroundColor,
                    textColor = textColor
                )
                
                Log.d(TAG, "🚀 Calling updatePopupState with notification data")
                
                // Set the state directly
                updatePopupState(notificationData)
                
                Log.d(TAG, "🎯 Rich popup data set: title='$title', hasImage=${imageUrl != null}, hasCTA=${ctaText != null}")
                Log.d(TAG, "🎯 Popup state should be visible now!")
                return
            } else {
                Log.d(TAG, "❌ Not showing popup - fromNotification: $fromNotification, showRichPopup: $showRichPopup")
            }
            
            // Handle other notification types
            val notificationType = extras.getString("type")
            val broadcastId = extras.getString("broadcast_id")
            
            when (notificationType) {
                "broadcast" -> {
                    Log.d(TAG, "Handling broadcast notification: $broadcastId")
                    // Handle broadcast notification - maybe navigate to a specific screen
                }
                "update" -> {
                    Log.d(TAG, "Handling update notification")
                    // Handle app update notification
                }
                else -> {
                    Log.d(TAG, "Regular notification opened")
                }
            }
        } else {
            Log.d(TAG, "❌ No extras found in intent")
        }
    }
    
    /**
     * 🎯 COMPREHENSIVE PERMISSION MANAGER
     * This function requests ALL required permissions at once when the app starts,
     * preventing the need to request them individually later.
     */
    private fun requestAllPermissionsUpfront() {
        if (allPermissionsRequested) {
            Log.d(TAG, "All permissions already requested this session")
            return
        }
        
        try {
            val permissionsToRequest = mutableListOf<String>()
            
            // 1. Notification Permission (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            
            // 2. Media Permissions (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Video permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) 
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
                }
                // Audio permission  
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) 
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
                }
            } else {
                // 3. Legacy Storage Permissions (Android 12 and below)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Android 11-12: READ_EXTERNAL_STORAGE
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                        != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {
                    // Android 10 and below: WRITE_EXTERNAL_STORAGE
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                        != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            }
            
            // Request all permissions at once if any are missing
            if (permissionsToRequest.isNotEmpty()) {
                Log.i(TAG, "🔐 Requesting ${permissionsToRequest.size} permissions upfront:")
                permissionsToRequest.forEach { permission ->
                    Log.i(TAG, "   - $permission")
                }
                
                requestMultiplePermissions.launch(permissionsToRequest.toTypedArray())
            } else {
                Log.i(TAG, "✅ All permissions already granted!")
                allPermissionsRequested = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting permissions", e)
        }
    }

}

@Composable
fun NextPlayerApp(
    shouldShowNotificationPopup: Boolean = false,
    notificationPopupData: NotificationPopupData? = null,
    onDismissPopup: () -> Unit = {}
) {
    val viewModel: MainActivityViewModel = hiltViewModel()
    val preferences by viewModel.preferencesRepository.applicationPreferences.collectAsStateWithLifecycle(
        initialValue = ApplicationPreferences()
    )

    val isSystemInDarkTheme = isSystemInDarkTheme()

    val darkTheme = when (preferences.themeConfig) {
        ThemeConfig.SYSTEM -> isSystemInDarkTheme
        ThemeConfig.ON -> true
        ThemeConfig.OFF -> false
    }

    VipulPlayerTheme(
        darkTheme = darkTheme,
        highContrastDarkTheme = preferences.useHighContrastDarkTheme,
        dynamicColor = preferences.useDynamicColors
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainScreen()
            }

            // Notification popup overlay
            if (shouldShowNotificationPopup && notificationPopupData != null) {
                NotificationPopup(
                    data = notificationPopupData,
                    onDismiss = onDismissPopup
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val shouldShowBottomBar = true // Always show for root destinations
    val context = LocalContext.current
    val adManager = (context as MainActivity).adManager // access injected adManager
    // 🧩 Track first load
    var isFirstLoad by remember { mutableStateOf(true) }
// 🔹 State for Exit Dialog
    var showExitDialog by remember { mutableStateOf(false) }

    // 🧩 Handle back press
    BackHandler {
        showExitDialog = true
    }
    // ✅ Show Exit Confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit App") },
            text = { Text("Are you sure you want to exit?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        //(context as? Activity)?.finishAffinity() // Exit app completely
                        if (AdConstants.ON_EXIT_POP_UP_AD){
                            adManager.showRewardedAdForComposeActivity(context)
                        }else{
                            (context as? Activity)?.finishAffinity() // Exit app completely
                        }



                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    // ✅ Only preload ads when user switches tabs (not on initial load)
    LaunchedEffect(currentDestination?.route) {
        currentDestination?.route?.let { route ->
            if (isFirstLoad) {
                Log.d("MainScreen", "🚀 First tab load detected ($route) — skipping ad preload")
                isFirstLoad = false
            } else {
                Log.d("MainScreen", "🔄 Tab changed to: $route — preloading ads now")
                if (AdConstants.SHOW_AD_ON_TAB_CHANGE){
                    adManager.showInterstitialAdForTabsChangeComposeActivity(context)
                }

            }
        }
    }
    Scaffold(
        bottomBar = {
            NextPlayerBottomBar(
                navController = navController,
                currentDestination = currentDestination,
                shouldShowBottomBar = shouldShowBottomBar,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = MEDIA_ROUTE,
            modifier = Modifier.padding(paddingValues)
        ) {
            mediaNavGraph(
                navGraphBuilder = this,
                navController = navController,
                adBannerContent = {
                    BannerAdView(
                        placement = AdPlacement.HOME_SCREEN,
                        adManager = adManager,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

            )
            musicNavGraph(
                onPlayAudio = { audioFile ->
                    // Launch the dedicated audio player activity for audio files with metadata
                    context.startAudioPlayerActivity(
                        Uri.parse(audioFile.uriString),
                        audioFile.title,
                        audioFile.artist,
                        audioFile.album
                    )
                }
            )
            streamNavGraph(
                onPlayVideo = { uri ->
                    try {
                        android.util.Log.d("MainActivity", "Starting video player for URI: $uri")

                        // Create intent for video player using correct format
                        val intent = android.content.Intent().apply {
                            setClassName(context, "dev.anilbeesetti.nextplayer.feature.player.PlayerActivity")
                            action = android.content.Intent.ACTION_VIEW
                            data = uri
                            putExtra("player_title", "Stream Video")
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        }

                        // Verify the intent can be handled
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                            android.util.Log.d("MainActivity", "Video player started successfully")
                        } else {
                            android.util.Log.e("MainActivity", "No activity found to handle video intent")
                            android.widget.Toast.makeText(context, "Error: Cannot play this video format", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Error starting video player", e)
                        android.widget.Toast.makeText(context, "Error: Failed to start video player", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            )
            settingsNavGraph(navController)
        }
    }
}

fun getCurrentTitle(currentDestination: NavDestination?): String {
    return when (currentDestination?.route) {
        MEDIA_ROUTE -> "Videos"
        MUSIC_ROUTE -> "Music"
        STREAM_ROUTE -> "Stream"
        SETTINGS_ROUTE -> "Settings"
        else -> ""
    }
}