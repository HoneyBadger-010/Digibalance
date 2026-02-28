package com.CuriosityLabs.digibalance.ui.parent

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CuriosityLabs.digibalance.data.repository.ParentDashboardRepository
import com.CuriosityLabs.digibalance.data.repository.UserRepository
import com.CuriosityLabs.digibalance.util.QRCodeHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeDisplayScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val dashboardRepository = remember { ParentDashboardRepository() }
    
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var linkCode by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timeRemaining by remember { mutableStateOf(300) } // 5 minutes in seconds
    
    // Generate QR code on launch
    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            val parentId = userRepository.getCurrentUserId()
            if (parentId != null) {
                dashboardRepository.generateLinkCode(parentId).onSuccess { code ->
                    linkCode = code
                    // Generate QR code with parent ID
                    qrCodeBitmap = QRCodeHelper.generateQRCode(parentId, 512)
                    isLoading = false
                    
                    // Start countdown timer
                    launch {
                        while (timeRemaining > 0) {
                            kotlinx.coroutines.delay(1000)
                            timeRemaining--
                        }
                    }
                }.onFailure { error ->
                    errorMessage = error.message
                    isLoading = false
                }
            } else {
                errorMessage = "Unable to get parent ID"
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Link Student Device", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onBack) {
                        Text("Go Back")
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF2196F3)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Scan this QR Code",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Open DigiBalance on your child's device and scan this code",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF757575)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // QR Code Display
                    qrCodeBitmap?.let { bitmap ->
                        Card(
                            modifier = Modifier.size(300.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Link Code Display
                    linkCode?.let { code ->
                        Card(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Or enter this code manually:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF757575)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = code,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3),
                                    letterSpacing = 4.dp.value.toInt().toFloat().dp.value.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Timer
                    if (timeRemaining > 0) {
                        Text(
                            text = "Code expires in ${timeRemaining / 60}:${String.format("%02d", timeRemaining % 60)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (timeRemaining < 60) Color(0xFFF44336) else Color(0xFF757575)
                        )
                    } else {
                        Text(
                            text = "Code expired",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                // Regenerate code
                                timeRemaining = 300
                                isLoading = true
                                scope.launch {
                                    val parentId = userRepository.getCurrentUserId()
                                    if (parentId != null) {
                                        dashboardRepository.generateLinkCode(parentId).onSuccess { code ->
                                            linkCode = code
                                            qrCodeBitmap = QRCodeHelper.generateQRCode(parentId, 512)
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Generate New Code")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Instructions
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Instructions:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. Open DigiBalance on your child's device\n" +
                                        "2. Go to Settings → Link to Parent\n" +
                                        "3. Tap 'Scan QR Code' or 'Enter Code'\n" +
                                        "4. Scan this QR code or enter the code above",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF424242)
                            )
                        }
                    }
                }
            }
        }
    }
}
