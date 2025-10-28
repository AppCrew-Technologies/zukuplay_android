package dev.anilbeesetti.nextplayer.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dev.anilbeesetti.nextplayer.settings.Setting
import dev.anilbeesetti.nextplayer.settings.navigation.*
import dev.anilbeesetti.nextplayer.core.ads.AdManager
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import dev.anilbeesetti.nextplayer.core.ads.TestAdScreen

@HiltViewModel
class AdManagerViewModel @Inject constructor(
    val adManager: AdManager
) : ViewModel()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    val viewModel = hiltViewModel<AdManagerViewModel>()
    val adManager = viewModel.adManager
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { /* Navigate to test ads */ }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🧪 Test AdMob Ads",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Test all AdMob ad types with official Google demo ads",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Show test ads directly in settings for now
            TestAdScreen(
                adManager = adManager,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestAdScreenWrapper(
    adManager: AdManager,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧪 Test AdMob Ads") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        TestAdScreen(
            adManager = adManager,
            modifier = Modifier.padding(padding)
        )
    }
}

fun NavGraphBuilder.settingsNavGraph(navController: NavController) {
    navigation(
        startDestination = settingsNavigationRoute,
        route = SETTINGS_ROUTE
    ) {
        settingsScreen(
            onNavigateUp = { navController.popBackStack() },
            onItemClick = { setting ->
                when (setting) {
                    Setting.APPEARANCE -> navController.navigateToAppearancePreferences()
                    Setting.PLAYER -> navController.navigateToPlayerPreferences()
                    Setting.AUDIO -> navController.navigateToAudioPreferences()
                    Setting.SUBTITLE -> navController.navigateToSubtitlePreferences()
                    Setting.MEDIA_LIBRARY -> navController.navigateToMediaLibraryPreferencesScreen()
                    Setting.DECODER -> navController.navigateToDecoderPreferences()
                    Setting.ABOUT -> navController.navigateToAboutPreferences()
                }
            }
        )
        
        // Test AdMob Ads Screen
        composable("test_ads") {
            val viewModel = hiltViewModel<AdManagerViewModel>()
            TestAdScreenWrapper(
                adManager = viewModel.adManager,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Preference screens
        appearancePreferencesScreen(onNavigateUp = { navController.popBackStack() })
        playerPreferencesScreen(onNavigateUp = { navController.popBackStack() })
        audioPreferencesScreen(onNavigateUp = { navController.popBackStack() })
        subtitlePreferencesScreen(onNavigateUp = { navController.popBackStack() })
        decoderPreferencesScreen(onNavigateUp = { navController.popBackStack() })
        aboutPreferencesScreen(onNavigateUp = { navController.popBackStack() })
        
        // Media library preferences screens
        mediaLibraryPreferencesScreen(
            onNavigateUp = { navController.popBackStack() },
            onFolderSettingClick = { navController.navigateToFolderPreferencesScreen() },
            onDisplayFieldsClick = { navController.navigateToDisplayFieldsPreferencesScreen() }
        )
        
        folderPreferencesScreen(onNavigateUp = { navController.popBackStack() })
        
        displayFieldsPreferencesScreen(onNavigateUp = { navController.popBackStack() })
    }
}
