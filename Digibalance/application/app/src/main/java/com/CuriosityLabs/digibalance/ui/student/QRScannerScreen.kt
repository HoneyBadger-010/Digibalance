package com.CuriosityLabs.digibalance.ui.student

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.CuriosityLabs.digibalance.data.repository.UserRepository
import com.CuriosityLabs.digibalance.data.repository.ParentDashboardRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScannerScreen(
    onLinkSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showManualEntry by remember { mutableStateOf(false) }
    var manualCode by remember { mutableStateOf("") }
    val userRepository = remember { UserRepository() }
    val dashboardRepository = remember { ParentDashboardRepository() }
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted && !showManualEntry) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    if (showManualEntry) {
        // Manual code entry dialog
        AlertDialog(
            onDismissRequest = { showManualEntry = false },
            title = { Text("Enter Link Code") },
            text = {
                Column {
                    Text("Enter the 6-digit code from parent's device:")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = manualCode,
                        onValueChange = { if (it.length <= 6) manualCode = it.uppercase() },
                        label = { Text("Link Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFF44336),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (manualCode.length == 6 && !isProcessing) {
                            isProcessing = true
                            scope.launch {
                                val studentId = userRepository.getCurrentUserId()
                                if (studentId != null) {
                                    dashboardRepository.linkStudentWithCode(studentId, manualCode)
                                        .onSuccess {
                                            onLinkSuccess()
                                        }
                                        .onFailure { error ->
                                            errorMessage = error.message
                                            isProcessing = false
                                        }
                                } else {
                                    errorMessage = "Unable to get student ID"
                                    isProcessing = false
                                }
                            }
                        }
                    },
                    enabled = manualCode.length == 6 && !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Link")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showManualEntry = false
                    errorMessage = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A1A1A)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("Cancel", color = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Link to Parent",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { showManualEntry = true }) {
                    Text("Enter Code", color = Color.White)
                }
            }
        }
        
        if (cameraPermissionState.status.isGranted) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    factory = { ctx ->
                        CompoundBarcodeView(ctx).apply {
                            decodeContinuous(object : BarcodeCallback {
                                override fun barcodeResult(result: BarcodeResult?) {
                                    result?.text?.let { parentId ->
                                        if (!isProcessing) {
                                            isProcessing = true
                                            scope.launch {
                                                val studentId = userRepository.getCurrentUserId()
                                                if (studentId != null) {
                                                    // Link student to parent using parent ID from QR
                                                    userRepository.linkStudentToParent(studentId, parentId)
                                                        .onSuccess {
                                                            onLinkSuccess()
                                                        }
                                                        .onFailure { error ->
                                                            errorMessage = error.message
                                                            isProcessing = false
                                                        }
                                                } else {
                                                    errorMessage = "Unable to get student ID"
                                                    isProcessing = false
                                                }
                                            }
                                        }
                                    }
                                }
                            })
                            resume()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Overlay instructions
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF5252)
                            )
                        ) {
                            Text(
                                text = errorMessage!!,
                                modifier = Modifier.padding(16.dp),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        )
                    ) {
                        Text(
                            text = "Point camera at parent's QR code",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please grant camera permission to scan QR codes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}
