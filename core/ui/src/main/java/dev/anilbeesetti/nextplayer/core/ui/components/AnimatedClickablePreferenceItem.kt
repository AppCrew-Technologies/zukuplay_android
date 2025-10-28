package dev.anilbeesetti.nextplayer.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedClickablePreferenceItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    // Scale animation when hovered
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )
    
    // Elevation animation when hovered
    val elevation by animateFloatAsState(
        targetValue = if (isHovered) 8f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "elevation"
    )
    
    PreferenceItem(
        title = title,
        description = description,
        icon = icon,
        enabled = enabled,
        modifier = modifier
            .graphicsLayer { 
                scaleX = scale
                scaleY = scale
                translationY = -elevation
            }
            .combinedClickable(
                onClick = onClick,
                enabled = enabled,
                onLongClick = onLongClick,
                interactionSource = interactionSource,
                indication = null // Remove default indication as we're using our own animation
            ),
    )
}

@Preview
@Composable
private fun AnimatedClickablePreferenceItemPreview() {
    AnimatedClickablePreferenceItem(
        title = "Title",
        description = "Description of the preference item goes here.",
        icon = NextIcons.DoubleTap,
        onClick = {},
        enabled = true,
    )
} 