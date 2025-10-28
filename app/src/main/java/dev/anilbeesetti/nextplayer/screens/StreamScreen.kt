package dev.anilbeesetti.nextplayer.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.NextCenterAlignedTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamScreen(
    onPlayVideo: ((Uri) -> Unit)? = null,
    viewModel: StreamViewModel = hiltViewModel()
) {
    var url by rememberSaveable { mutableStateOf("") }
    var isUrlFocused by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingProgress by remember { mutableStateOf(0f) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Get history items from ViewModel
    val historyItems by viewModel.streamHistory.collectAsState()
    val listState = rememberLazyListState()
    
    // Loading animation effect
    LaunchedEffect(isLoading) {
        if (isLoading) {
            try {
                for (i in 0..100) {
                    loadingProgress = i / 100f
                    delay(30) // Simulate loading progress
                }
                // Don't automatically stop loading here - let the play function handle it
            } catch (e: Exception) {
                android.util.Log.e("StreamScreen", "Error in loading animation", e)
                isLoading = false
                loadingProgress = 0f
            }
        } else {
            loadingProgress = 0f
        }
    }
    
    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
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
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
        ) {
                            Box(
                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = NextIcons.Movie,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                    Text(
                                text = "Stream",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Modern URL input section
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(600, easing = EaseOutCubic)
                        ) + fadeIn(animationSpec = tween(600))
                    ) {
                        ModernUrlInputCard(
                            url = url,
                            onUrlChange = { url = it },
                            isLoading = isLoading,
                            loadingProgress = loadingProgress,
                            onPlay = {
                                if (url.isNotBlank() && !isLoading) {
                                    isLoading = true
                                    val fileName = viewModel.extractFileName(url)
                                    viewModel.addToHistory(url, fileName)
                                    
                                    // Start loading, then play video after loading completes
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                        try {
                                            delay(3000) // Wait for loading to complete
                                            android.util.Log.d("StreamScreen", "Attempting to play URL: $url")
                                            
                                            // Validate URL format
                                            val uri = Uri.parse(url)
                                            if (uri != null && (uri.scheme == "http" || uri.scheme == "https" || uri.scheme == "rtmp" || uri.scheme == "rtsp")) {
                                                onPlayVideo?.invoke(uri)
                                                focusManager.clearFocus()
                                                
                                                // Reset loading state after successful launch
                                                delay(500) // Small delay to ensure video player starts
                                                isLoading = false
                                                loadingProgress = 0f
                                            } else {
                                                android.util.Log.e("StreamScreen", "Invalid URL format: $url")
                                                android.widget.Toast.makeText(context, "Invalid URL format", android.widget.Toast.LENGTH_LONG).show()
                                                isLoading = false
                                                loadingProgress = 0f
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("StreamScreen", "Error playing video", e)
                                            android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                            isLoading = false
                                            loadingProgress = 0f
                                        }
                                    }
                                }
                            },
                            onClear = { 
                                url = ""
                                focusManager.clearFocus()
                            },
                            onFocusChange = { isUrlFocused = it }
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
            
                // History section
                if (historyItems.isNotEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(800, 200, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(800, 200))
                        ) {
                            ModernHistoryCard(
                                historyItems = historyItems,
                                onItemClick = { url = it },
                                onItemPlay = { uri ->
                                    val fileName = viewModel.extractFileName(uri)
                                    viewModel.addToHistory(uri, fileName)
                                    onPlayVideo?.invoke(Uri.parse(uri))
                                },
                                onItemDelete = { viewModel.deleteHistoryItem(it) },
                                onClearAll = { viewModel.clearHistory() }
                            )
                        }
                    }
                } else {
                    item {
                        // Modern empty state
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.graphicsLayer {
                                    alpha = 0.8f
                                }
                        ) {
                            Icon(
                                    imageVector = NextIcons.Movie,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No streams yet",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your streaming history will appear here\nafter you play your first video",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
fun ModernUrlInputCard(
    url: String,
    onUrlChange: (String) -> Unit,
    isLoading: Boolean,
    loadingProgress: Float,
    onPlay: () -> Unit,
    onClear: () -> Unit,
    onFocusChange: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    
    // Subtle animation for the card
    val cardScale by animateFloatAsState(
        targetValue = if (isLoading) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )
    
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .shadow(
                elevation = if (isLoading) 16.dp else 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isLoading) 0.2f else 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                .padding(24.dp)
                    ) {
            // Professional header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = NextIcons.Link,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Stream from URL",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isLoading) {
                            Text(
                            text = "Loading... ${(loadingProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Paste any video URL to start streaming instantly",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Modern URL input with custom styling and fixed width
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp), // Fixed height to prevent expansion
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    placeholder = { 
                        Text(
                            text = "https://example.com/video.mp4",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = NextIcons.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = if (url.isNotEmpty()) {
                        {
                            IconButton(onClick = onClear) {
                                Icon(
                                    imageVector = NextIcons.Delete,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            if (url.isNotBlank()) {
                                onPlay()
                            }
                        }
                    ),
                    singleLine = true, // Ensure single line
                    maxLines = 1 // Prevent multiple lines
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
                        
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play/Loading button
                Button(
                    onClick = onPlay,
                            modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    enabled = url.isNotBlank() && !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLoading) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    if (isLoading) {
                        // Loading indicator
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                            progress = { loadingProgress }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Loading...",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = NextIcons.Play,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Stream Now",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
                
                // Clear button
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier
                        .height(52.dp)
                        .width(80.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                ) {
                    Icon(
                        imageVector = NextIcons.Delete,
                        contentDescription = "Clear",
                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

@Composable
fun ModernHistoryCard(
    historyItems: List<dev.anilbeesetti.nextplayer.core.model.StreamHistoryItem>,
    onItemClick: (String) -> Unit,
    onItemPlay: (String) -> Unit,
    onItemDelete: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with responsive layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false) // Allow flexible width but don't fill all space
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = NextIcons.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Recent Streams",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp)) // Add spacing between title and button
                
                // Enhanced Clear All button with better responsiveness
                Surface(
                    onClick = onClearAll,
                    modifier = Modifier.wrapContentWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.error
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = NextIcons.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                Text(
                            text = "Clear All",
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // History items
            historyItems.forEach { item ->
                ModernHistoryItem(
                    url = item.url,
                    fileName = item.fileName,
                    onClick = { onItemClick(item.url) },
                    onPlay = { onItemPlay(item.url) },
                    onDelete = { onItemDelete(item.url) }
                )
                
                if (item != historyItems.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ModernHistoryItem(
    url: String,
    fileName: String,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    var urlScrollOffset by remember { mutableStateOf(0f) }
    val maxScrollOffset = remember(url) { (url.length * 8).toFloat() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                            )
                        )
                    ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                    imageVector = NextIcons.Play,
                contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
            )
        }
        
            Spacer(modifier = Modifier.width(16.dp))
        
            // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
                Spacer(modifier = Modifier.height(4.dp))
                
                // Scrollable URL with fixed height
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .pointerInput(url) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                urlScrollOffset = (urlScrollOffset - dragAmount * 2)
                                    .coerceIn(0f, maxScrollOffset)
                            }
                        }
                ) {
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                        overflow = TextOverflow.Visible,
                        modifier = Modifier.graphicsLayer {
                            translationX = -urlScrollOffset
                        }
            )
        }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play button
        Button(
            onClick = onPlay,
                    modifier = Modifier.size(42.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
            )
        ) {
            Icon(
                imageVector = NextIcons.Play,
                contentDescription = "Play",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
            )
        }
        
        // Delete button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = NextIcons.Delete,
                contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )
                }
            }
        }
    }
} 