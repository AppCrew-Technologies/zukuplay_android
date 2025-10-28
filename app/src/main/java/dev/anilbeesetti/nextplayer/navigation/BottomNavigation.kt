package dev.anilbeesetti.nextplayer.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.navigation.RoutesConstants.MEDIA_ROUTE
import dev.anilbeesetti.nextplayer.navigation.RoutesConstants.MUSIC_ROUTE
import dev.anilbeesetti.nextplayer.navigation.RoutesConstants.SETTINGS_ROUTE
import dev.anilbeesetti.nextplayer.navigation.RoutesConstants.STREAM_ROUTE
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition

// Root routes for navigation
const val ROOT_LIBRARY_ROUTE = MEDIA_ROUTE
const val ROOT_SETTINGS_ROUTE = SETTINGS_ROUTE

enum class BottomNavDestination(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int
) {
    VIDEO(
        route = MEDIA_ROUTE,
        icon = NextIcons.Movie,
        labelResId = R.string.videos
    ),
    MUSIC(
        route = MUSIC_ROUTE,
        icon = NextIcons.Audio,
        labelResId = R.string.music
    ),
    STREAM(
        route = STREAM_ROUTE,
        icon = NextIcons.Link,
        labelResId = R.string.stream
    ),
    SETTINGS(
        route = SETTINGS_ROUTE,
        icon = NextIcons.Settings,
        labelResId = R.string.settings
    )
}

// Data class for bottom navigation items
data class BottomNavigationItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String
)

@Composable
fun NextPlayerBottomBar(
    navController: androidx.navigation.NavController,
    currentDestination: NavDestination?,
    shouldShowBottomBar: Boolean,
    onNavigate: (String) -> Unit
) {
    // Animated gradient background effect
    val infiniteTransition = rememberInfiniteTransition(label = "bottomNavBackground")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )
    
    // Only show the bottom bar if we're on a main destination
    AnimatedVisibility(
        visible = shouldShowBottomBar,
        enter = slideInVertically(initialOffsetY = { it }) + expandVertically() + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + shrinkVertically() + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        ),
                        start = Offset(0f, gradientShift * 800),
                        end = Offset(800f, (1f - gradientShift) * 800)
                    )
                )
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp, max = 88.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                )
                            ),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ),
                    tonalElevation = 0.dp,
                    containerColor = Color.Transparent,
                    windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            BottomNavDestination.values().forEach { destination ->
                val selected = currentDestination?.hierarchy?.any { 
                    it.route == destination.route 
                } ?: false
                
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(destination.route) },
                    icon = {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (selected) {
                                                Modifier.background(
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                                        )
                                                    ),
                                                    shape = CircleShape
                                                )
                                            } else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                        Icon(
                            imageVector = destination.icon,
                                        contentDescription = stringResource(id = destination.labelResId),
                                        modifier = Modifier.size(24.dp),
                                        tint = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                        )
                                }
                    },
                    label = {
                        Text(
                            text = stringResource(id = destination.labelResId),
                            textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    maxLines = 1,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Transparent,
                                selectedTextColor = Color.Transparent,
                                unselectedIconColor = Color.Transparent,
                                unselectedTextColor = Color.Transparent,
                                indicatorColor = Color.Transparent
                    )
                )
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsatingCircle(color: Color) {
    val scale by animateFloatAsState(
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsating"
    )
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .background(color.copy(alpha = 0.1f), CircleShape)
    )
} 