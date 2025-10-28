package dev.anilbeesetti.nextplayer.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import android.util.Log

data class NotificationPopupData(
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val ctaText: String? = null,
    val ctaLink: String? = null,
    val buttons: List<PopupButton> = emptyList(),
    val backgroundColor: String? = null,
    val textColor: String? = null
)

data class PopupButton(
    val text: String,
    val link: String,
    val style: ButtonStyle = ButtonStyle.PRIMARY
)

enum class ButtonStyle {
    PRIMARY, SECONDARY, OUTLINE
}

@Composable
fun NotificationPopup(
    data: NotificationPopupData,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    
    // Debug logging
    Log.d("NotificationPopup", "🎨 Rendering popup with data:")
    Log.d("NotificationPopup", "  Title: ${data.title}")
    Log.d("NotificationPopup", "  Body: ${data.body}")
    Log.d("NotificationPopup", "  Image URL: ${data.imageUrl}")
    Log.d("NotificationPopup", "  CTA Text: ${data.ctaText}")
    Log.d("NotificationPopup", "  Buttons count: ${data.buttons.size}")
    data.buttons.forEachIndexed { index, button ->
        Log.d("NotificationPopup", "  Button $index: ${button.text} (${button.style})")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .clickable(enabled = false) { /* Prevent dismiss on card click */ },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = data.backgroundColor?.let { 
                                try { Color(android.graphics.Color.parseColor(it)) } 
                                catch (e: Exception) { MaterialTheme.colorScheme.surface }
                            } ?: MaterialTheme.colorScheme.surface
                        )
                ) {
                    // Header with close button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = data.textColor?.let { 
                                    try { Color(android.graphics.Color.parseColor(it)) } 
                                    catch (e: Exception) { MaterialTheme.colorScheme.onSurface }
                                } ?: MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Actual image display using Coil
                        data.imageUrl?.let { imageUrl ->
                            if (imageUrl.isNotBlank()) {
                                Log.d("NotificationPopup", "🖼️ Loading image: $imageUrl")
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Notification Image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        onLoading = { 
                                            Log.d("NotificationPopup", "🔄 Image loading: $imageUrl")
                                        },
                                        onSuccess = { 
                                            Log.d("NotificationPopup", "✅ Image loaded successfully: $imageUrl")
                                        },
                                        onError = { 
                                            Log.e("NotificationPopup", "❌ Image load failed: $imageUrl")
                                        },
                                        fallback = androidx.compose.ui.graphics.painter.ColorPainter(
                                            MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        error = androidx.compose.ui.graphics.painter.ColorPainter(
                                            MaterialTheme.colorScheme.outline
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            } else {
                                Log.d("NotificationPopup", "⚠️ Image URL is blank, skipping image display")
                            }
                        } ?: run {
                            Log.d("NotificationPopup", "ℹ️ No image URL provided")
                        }

                        // Title
                        Text(
                            text = data.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = data.textColor?.let { 
                                try { Color(android.graphics.Color.parseColor(it)) } 
                                catch (e: Exception) { MaterialTheme.colorScheme.onSurface }
                            } ?: MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Body text
                        Text(
                            text = data.body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = data.textColor?.let { 
                                try { Color(android.graphics.Color.parseColor(it)) } 
                                catch (e: Exception) { MaterialTheme.colorScheme.onSurfaceVariant }
                            } ?: MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Main CTA button (if provided)
                        data.ctaText?.let { ctaText ->
                            data.ctaLink?.let { ctaLink ->
                                Button(
                                    onClick = {
                                        try {
                                            uriHandler.openUri(ctaLink)
                                        } catch (e: Exception) {
                                            // Handle error - maybe show toast
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(25.dp)
                                ) {
                                    Text(
                                        text = ctaText,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // Additional buttons
                        data.buttons.forEach { button ->
                            when (button.style) {
                                ButtonStyle.PRIMARY -> {
                                    Button(
                                        onClick = {
                                            try {
                                                uriHandler.openUri(button.link)
                                            } catch (e: Exception) {
                                                // Handle error
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text(text = button.text)
                                    }
                                }
                                ButtonStyle.SECONDARY -> {
                                    FilledTonalButton(
                                        onClick = {
                                            try {
                                                uriHandler.openUri(button.link)
                                            } catch (e: Exception) {
                                                // Handle error
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text(text = button.text)
                                    }
                                }
                                ButtonStyle.OUTLINE -> {
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                uriHandler.openUri(button.link)
                                            } catch (e: Exception) {
                                                // Handle error
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text(text = button.text)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Bottom padding
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
} 