package dev.anilbeesetti.nextplayer.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import dev.anilbeesetti.nextplayer.core.ui.designsystem.animatedComposable
import dev.anilbeesetti.nextplayer.settings.screens.medialibrary.FolderPreferencesScreen
import dev.anilbeesetti.nextplayer.settings.screens.medialibrary.MediaLibraryPreferencesScreen
import dev.anilbeesetti.nextplayer.settings.screens.medialibrary.DisplayFieldsPreferencesScreen

const val mediaLibraryPreferencesNavigationRoute = "media_library_preferences_route"
const val folderPreferencesNavigationRoute = "folder_preferences_route"
const val displayFieldsPreferencesNavigationRoute = "display_fields_preferences_route"

fun NavController.navigateToMediaLibraryPreferencesScreen(navOptions: NavOptions? = navOptions { launchSingleTop = true }) {
    this.navigate(mediaLibraryPreferencesNavigationRoute, navOptions)
}

fun NavController.navigateToFolderPreferencesScreen(navOptions: NavOptions? = navOptions { launchSingleTop = true }) {
    this.navigate(folderPreferencesNavigationRoute, navOptions)
}

fun NavController.navigateToDisplayFieldsPreferencesScreen(navOptions: NavOptions? = navOptions { launchSingleTop = true }) {
    this.navigate(displayFieldsPreferencesNavigationRoute, navOptions)
}

fun NavGraphBuilder.mediaLibraryPreferencesScreen(
    onNavigateUp: () -> Unit,
    onFolderSettingClick: () -> Unit,
    onDisplayFieldsClick: () -> Unit,
) {
    animatedComposable(route = mediaLibraryPreferencesNavigationRoute) {
        MediaLibraryPreferencesScreen(
            onNavigateUp = onNavigateUp,
            onFolderSettingClick = onFolderSettingClick,
            onDisplayFieldsClick = onDisplayFieldsClick,
        )
    }
}

fun NavGraphBuilder.folderPreferencesScreen(onNavigateUp: () -> Unit) {
    animatedComposable(route = folderPreferencesNavigationRoute) {
        FolderPreferencesScreen(onNavigateUp = onNavigateUp)
    }
}

fun NavGraphBuilder.displayFieldsPreferencesScreen(onNavigateUp: () -> Unit) {
    animatedComposable(route = displayFieldsPreferencesNavigationRoute) {
        DisplayFieldsPreferencesScreen(onNavigateUp = onNavigateUp)
    }
}
