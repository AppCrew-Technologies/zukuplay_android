@file:OptIn(ExperimentalPermissionsApi::class)

package dev.anilbeesetti.nextplayer.feature.videopicker.screens.media

import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dev.anilbeesetti.nextplayer.core.ads.AdConstants
import dev.anilbeesetti.nextplayer.core.ads.components.BannerAdViewNew
import dev.anilbeesetti.nextplayer.core.ads.components.NativeAdCornerOverlay
import dev.anilbeesetti.nextplayer.core.common.storagePermission
import dev.anilbeesetti.nextplayer.core.common.isStoragePermissionGranted
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.NextCenterAlignedTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.composables.PermissionMissingView
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.FieldsDialog
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.MediaView
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.QuickSettingsDialog
import dev.anilbeesetti.nextplayer.feature.videopicker.screens.MediaState

@Composable
fun MediaPickerRoute(
    onSettingsClick: () -> Unit,
    onPlayVideo: (uri: Uri) -> Unit,
    onFolderClick: (folderPath: String) -> Unit,
    viewModel: MediaPickerViewModel = hiltViewModel(),
    adBannerContent: @Composable (() -> Unit)? = null
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mediaState by viewModel.mediaState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // ⭐ Use centralized permission system - no more individual requests!
    val isPermissionGranted = context.isStoragePermissionGranted()
    
    // Start media sync when permission is granted (permissions are handled in MainActivity)
    LaunchedEffect(isPermissionGranted) {
        if (isPermissionGranted) {
            viewModel.startMediaSync()
        }
    }

    MediaPickerScreen(
        mediaState = mediaState,
        preferences = preferences,
        isRefreshing = uiState.refreshing,
        isPermissionGranted = isPermissionGranted,
        onPlayVideo = onPlayVideo,
        onFolderClick = onFolderClick,
        onSettingsClick = onSettingsClick,
        updatePreferences = viewModel::updateMenu,
        onDeleteVideoClick = { viewModel.deleteVideos(listOf(it)) },
        onDeleteFolderClick = { viewModel.deleteFolders(listOf(it)) },
        onAddToSync = viewModel::addToMediaInfoSynchronizer,
        onRenameVideoClick = viewModel::renameVideo,
        onRefreshClicked = viewModel::onRefreshClicked,
        adBannerContent = adBannerContent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MediaPickerScreen(
    mediaState: MediaState,
    preferences: ApplicationPreferences,
    isRefreshing: Boolean = false,
    isPermissionGranted: Boolean = true,
    onPlayVideo: (uri: Uri) -> Unit = {},
    onFolderClick: (folderPath: String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    updatePreferences: (ApplicationPreferences) -> Unit = {},
    onDeleteVideoClick: (String) -> Unit,
    onRenameVideoClick: (Uri, String) -> Unit = { _, _ -> },
    onDeleteFolderClick: (Folder) -> Unit,
    onAddToSync: (Uri) -> Unit = {},
    onRefreshClicked: () -> Unit = {},
    adBannerContent: @Composable (() -> Unit)? = null
) {
    var showQuickSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showFieldsDialog by rememberSaveable { mutableStateOf(false) }

    // Tab selection state for Videos, Folders, Tree
    val mediaViewModes = listOf(
        dev.anilbeesetti.nextplayer.core.model.MediaViewMode.VIDEOS,
        dev.anilbeesetti.nextplayer.core.model.MediaViewMode.FOLDERS,
        dev.anilbeesetti.nextplayer.core.model.MediaViewMode.FOLDER_TREE,
    )
    val selectedTabIndex = mediaViewModes.indexOf(preferences.mediaViewMode)

    // Animated gradient background effect
    val infiniteTransition = rememberInfiniteTransition(label = "videoBackground")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    ),
                    start = Offset(0f, gradientShift * 1000),
                    end = Offset(1000f, (1f - gradientShift) * 1000)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
            .statusBarsPadding(),
    ) {
            // Modern Top Bar with glossy effect
        NextCenterAlignedTopAppBar(
            title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            // Using the provided ZukuPlay logo image
                            Image(
                                painter = painterResource(id = dev.anilbeesetti.nextplayer.core.ui.R.drawable.zukuplay_logo),
                                contentDescription = "ZukuPlay Logo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        // Professional Animated ZukuPlay Title
                        AnimatedZukuPlayTitle()
                    }
            },
            actions = {
                // Replace headphone icon with the fields/style icon
                IconButton(onClick = { showFieldsDialog = true }) {
                    Icon(
                        imageVector = NextIcons.Style,
                        contentDescription = stringResource(id = R.string.fields),
                        modifier = Modifier.size(24.dp),
                    )
                }
                IconButton(onClick = { showQuickSettingsDialog = true }) {
                    Icon(
                        imageVector = NextIcons.Sort,
                        contentDescription = stringResource(id = R.string.menu),
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
            ),
        )

            // Section Tabs with enhanced glossy background
        SectionTabs(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { index ->
                val mode = mediaViewModes[index]
                if (preferences.mediaViewMode != mode) {
                    updatePreferences(preferences.copy(mediaViewMode = mode))
                }
            },
        )
            //top banner
            if (AdConstants.SHOW_BANNER_TOP){
                BannerAdViewNew()
            }

        // Content
        Box(modifier = Modifier.weight(1f)) {
            when {
                !isPermissionGranted -> {
                    // Show a message that permissions are handled on app startup
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = NextIcons.Movie,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Storage Permission Required",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Please grant storage permission to view your videos.\nPermissions are requested when the app starts.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    when (mediaState) {
                        is MediaState.Success -> {
                            MediaView(
                                isLoading = false,
                                rootFolder = mediaState.data,
                                preferences = preferences,
                                onVideoClick = onPlayVideo,
                                onFolderClick = onFolderClick,
                                onDeleteVideoClick = onDeleteVideoClick,
                                onDeleteFolderClick = onDeleteFolderClick,
                                onRenameVideoClick = onRenameVideoClick,
                                onVideoLoaded = onAddToSync,
                                adBannerContent = adBannerContent
                            )
                        }
                        is MediaState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
            // Native ad aligned to right end
            if (AdConstants.FLOATING_AD) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp, bottom = 8.dp)
                        .background(Color.Transparent), // ✅ Transparent row
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    NativeAdCornerOverlay(
                        //modifier = Modifier.wrapContentSize()
                    )
                }
            }


            //bottom banner
            if (AdConstants.SHOW_BANNER_BOTTOM){
                BannerAdViewNew()
            }

        }
    }

    // Dialogs
    if (showQuickSettingsDialog) {
        QuickSettingsDialog(
            applicationPreferences = preferences,
            onDismiss = { showQuickSettingsDialog = false },
            updatePreferences = updatePreferences,
        )
    }
    if (showFieldsDialog) {
        FieldsDialog(
            applicationPreferences = preferences,
            onDismiss = { showFieldsDialog = false },
            updatePreferences = updatePreferences,
        )
    }
}

@Composable
fun SectionTabs(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        Triple("Videos", NextIcons.Movie, 0),
        Triple("Folders", NextIcons.Folder, 1),
        Triple("Tree", NextIcons.DashBoard, 2),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(24.dp),
            ),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        tabs.forEach { (label, icon, index) ->
            val selected = selectedTabIndex == index
            val bgColor = if (selected) {
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.Transparent)
                )
            }
            val contentColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 12.dp, horizontal = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = label,
                    color = contentColor,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
fun AnimatedZukuPlayTitle() {
    // Ultra-smooth animation setup
    val infiniteTransition = rememberInfiniteTransition(label = "titleAnimation")

    // Smooth shimmer light effect with better easing
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -120f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // Ultra-subtle breathing scale with smoother curves
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.003f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Background sync animation that matches the main background
    val backgroundSync by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backgroundSync"
    )

    // Make sure the text has enough space and doesn't get truncated
    Box(
        modifier = Modifier
            .widthIn(min = 120.dp) // Ensure minimum width for full text
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f + (backgroundSync * 0.02f)),
                        Color.Blue.copy(alpha = 0.05f + (backgroundSync * 0.02f)),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.03f + (backgroundSync * 0.01f)),
                        Color.Transparent
                    ),
                    start = Offset(0f, backgroundSync * 100),
                    end = Offset(100f, (1f - backgroundSync) * 100)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        // Main title text with proper sizingf
        Text(
            text = "ZukuPlay",
            fontSize = 19.sp, // Slightly smaller to ensure it fits
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp, // Reduced letter spacing to fit better
            maxLines = 1,
            overflow = TextOverflow.Visible, // Changed to visible
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            ),
            modifier = Modifier
                .graphicsLayer {
                    scaleX = breathingScale
                    scaleY = breathingScale
                    alpha = 0.96f + (backgroundSync * 0.02f)
                }
        )

        // Enhanced shimmer light overlay with better blending
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.12f),
                            Color.Cyan.copy(alpha = 0.08f),
                            Color.Blue.copy(alpha = 0.05f),
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        start = Offset(shimmerOffset - 50f, 0f),
                        end = Offset(shimmerOffset + 50f, 60f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
        )
    }
}

@ExperimentalPermissionsApi
private val GrantedPermissionState = object : PermissionState {
    override val permission: String
        get() = ""
    override val status: PermissionStatus
        get() = PermissionStatus.Granted

    override fun launchPermissionRequest() {}
}
