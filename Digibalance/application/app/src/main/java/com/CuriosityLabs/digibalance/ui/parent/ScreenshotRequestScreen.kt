package com.CuriosityLabs.digibalance.ui.parent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.CuriosityLabs.digibalance.data.repository.ScreenshotRequest
import com.CuriosityLabs.digibalance.data.repository.ScreenshotRequestRepository
import com.CuriosityLabs.digibalance.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotRequestScreen(
    studentId: String,
    studentName: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { ScreenshotRequestRepository() }
    val userRepository = remember { UserRepository() }
    
    var requests by remember { mutableStateOf<List<ScreenshotRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isRequesting by remember { mutableStateOf(false) }
    var selectedScreenshot by remember { mutableStateOf<ScreenshotRequest?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Auto-refresh every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            loadRequests(repository, userRepository, studentId) { newRequests ->
                requests = newRequests
            }
            delay(5000)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Screenshots", fontWeight = FontWeight.Bold)
                        Text(
                            studentName,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        isRequesting = true
                        errorMessage = null
                        
                        val parentId = userRepository.getCurrentUserId()
                        if (parentId != null) {
                            repository.createScreenshotRequest(parentId, studentId)
                                .onSuccess {
                                    // Reload requests
                                    loadRequests(repository, userRepository, studentId) { newRequests ->
                                        requests = newRequests
                                    }
                                }
                                .onFailure { error ->
                                    errorMessage = error.message
                                }
                        }
                        
                        isRequesting = false
                    }
                },
                icon = { Icon(Icons.Default.CameraAlt, "Request Screenshot") },
                text = { Text("Request Screenshot") },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8F9FA),
                            Color(0xFFFFFFFF)
                        )
                    )
                )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (requests.isEmpty()) {
                EmptyScreenshotState(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(requests) { request ->
                        ScreenshotRequestCard(
                            request = request,
                            onClick = {
                                if (request.status == "captured") {
                                    selectedScreenshot = request
                                }
                            }
                        )
                    }
                }
            }
            
            // Error message
            errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { errorMessage = null }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
            
            // Loading overlay when requesting
            if (isRequesting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Sending request...", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
    
    // Full-screen screenshot viewer
    selectedScreenshot?.let { screenshot ->
        ScreenshotViewerDialog(
            screenshot = screenshot,
            onDismiss = { selectedScreenshot = null }
        )
    }
}

private fun loadRequests(
    repository: ScreenshotRequestRepository,
    userRepository: UserRepository,
    studentId: String,
    onLoaded: (List<ScreenshotRequest>) -> Unit
) {
    kotlinx.coroutines.GlobalScope.launch {
        val parentId = userRepository.getCurrentUserId()
        if (parentId != null) {
            repository.getParentScreenshotRequests(parentId)
                .onSuccess { allRequests ->
                    // Filter for this student
                    val studentRequests = allRequests.filter { it.student_id == studentId }
                    onLoaded(studentRequests)
                }
        }
    }
}

@Composable
fun ScreenshotRequestCard(
    request: ScreenshotRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = request.status == "captured", onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (request.status) {
                "captured" -> Color(0xFFE8F5E9)
                "pending" -> Color(0xFFFFF9C4)
                "failed" -> Color(0xFFFFEBEE)
                "expired" -> Color(0xFFEEEEEE)
                else -> Color.White
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Status icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            when (request.status) {
                                "captured" -> Color(0xFF4CAF50)
                                "pending" -> Color(0xFFFFC107)
                                "failed" -> Color(0xFFF44336)
                                "expired" -> Color(0xFF9E9E9E)
                                else -> Color.Gray
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (request.status) {
                            "captured" -> Icons.Default.CheckCircle
                            "pending" -> Icons.Default.Schedule
                            "failed" -> Icons.Default.Error
                            "expired" -> Icons.Default.Cancel
                            else -> Icons.Default.Help
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    Text(
                        text = when (request.status) {
                            "captured" -> "Screenshot Captured"
                            "pending" -> "Waiting for screenshot..."
                            "failed" -> "Capture Failed"
                            "expired" -> "Request Expired"
                            else -> "Unknown Status"
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = formatTimestamp(request.request_time),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    if (request.status == "captured" && request.captured_at != null) {
                        Text(
                            text = "Captured ${formatTimestamp(request.captured_at)}",
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
            
            if (request.status == "captured") {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "View",
                    tint = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun EmptyScreenshotState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
        Text(
            text = "No Screenshots Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Text(
            text = "Tap the button below to request a screenshot from your child's device",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ScreenshotViewerDialog(
    screenshot: ScreenshotRequest,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onDismiss)
        ) {
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Screenshot image
            screenshot.screenshot_url?.let { url ->
                Image(
                    painter = rememberAsyncImagePainter(url),
                    contentDescription = "Screenshot",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            // Info overlay
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Captured: ${formatTimestamp(screenshot.captured_at ?: "")}",
                        fontWeight = FontWeight.Medium
                    )
                    screenshot.device_info?.let { info ->
                        Text(
                            text = "Device: $info",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = format.parse(timestamp)
        val now = Date()
        val diff = now.time - (date?.time ?: 0)
        
        when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> "${diff / 86400000}d ago"
        }
    } catch (e: Exception) {
        "Unknown"
    }
}
