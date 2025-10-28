# App Build Fixes

This document outlines the issues that need to be fixed to make the app build properly.

## Current Issues:

1. **Firebase Dependency Issues:**
   - The app depends on Firebase services but is missing Firebase dependencies
   - Files in `com.vipulplayer.nextplayer.*` packages have numerous Firebase-related errors

2. **Navigation Route Conflicts:**
   - Multiple declarations of route constants like `MEDIA_ROUTE`, `MUSIC_ROUTE`, etc.
   - Need to consolidate these into a single source of truth

3. **Missing Material Icon Resources:**
   - Missing icons like `Icons.Filled.MusicNote`, `Icons.Filled.Stream`, etc.
   - Need to replace with available Material icons

4. **Incompatible MediaService API:**
   - The `mediaService.init()` method referenced in MainActivity doesn't exist

## Step-by-Step Solutions:

1. **Fix Firebase Dependencies:**
   - Add these to your app-level `build.gradle` file:
   ```groovy
   implementation platform('com.google.firebase:firebase-bom:32.4.0')
   implementation 'com.google.firebase:firebase-analytics-ktx'
   implementation 'com.google.firebase:firebase-messaging-ktx'
   implementation 'com.google.firebase:firebase-firestore-ktx'
   ```
   - Remove or fix the `com.vipulplayer.nextplayer.adapters.NotificationsAdapter` class
   - Remove or fix the `com.vipulplayer.nextplayer.fragments.NotificationsFragment` class
   - Remove or fix the `com.vipulplayer.nextplayer.models.BroadcastMessage` class (the Firebase version)
   - Remove or fix the `com.vipulplayer.nextplayer.services.FirebaseMessagingService` class
   - Remove or fix the `com.vipulplayer.nextplayer.utils.NotificationHelper` class

2. **Fix Navigation Routes:**
   - Keep only one declaration of each route constant in `app/src/main/java/dev/anilbeesetti/nextplayer/navigation/Routes.kt`
   - Delete or comment out all other declarations in:
     - `StreamNavigation.kt`
     - `MusicNavigation.kt`
     - `SettingsNavGraph.kt`
     - Other files with duplicate route declarations
   - Update all imports to use the constants from Routes.kt

3. **Fix Material Icons:**
   - In `BottomNavDestination.kt`, replace:
     - `Icons.Filled.MusicNote` with `Icons.Filled.VideoLibrary`
     - `Icons.Filled.Stream` with `Icons.Filled.Cloud`
   - In `NotificationsScreen.kt`, replace:
     - `Icons.Default.Update` with `Icons.Default.Refresh`

4. **Fix MediaService Initialization:**
   - In `MainActivity.kt`, replace:
     ```kotlin
     mediaService.init()
     ```
     with:
     ```kotlin
     mediaService.initialize(this)
     ```

5. **Fix NotificationIcon Ripple Issue:**
   - In `NotificationIcon.kt`, update the imports to use:
     ```kotlin
     import androidx.compose.material3.ripple.rememberRipple
     ```

## Complete Fix Approach:

For the quickest solution to get the app building:

1. **Remove all Firebase-dependent code** by renaming the packages to `.disabled` (e.g., `com.vipulplayer.nextplayer.disabled.utils`)
2. **Use a single Routes.kt file** for all navigation constants
3. **Replace missing icons** with supported icons that exist in the current Material icons
4. **Fix method signatures** like `mediaService.initialize()` to match the actual implementations
5. **Update imports** to fix ripple and other component references

After implementing these fixes, you should be able to successfully build the basic app. 