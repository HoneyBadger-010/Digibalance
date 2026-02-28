package com.CuriosityLabs.digibalance.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CuriosityLabs.digibalance.data.repository.EmergencyCodeRepository
import com.CuriosityLabs.digibalance.data.repository.UserRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyCodeScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val emergencyRepo = remember { EmergencyCodeRepository() }
    
    var currentCode by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timeRemaining by remember { mutableStateOf(24 * 60 * 60) } // 24 hours in seconds
    
    // Generate initial code
    LaunchedEffect(Unit) {
        generateNewCode(
            userRepository = userRepository,
            emergencyRepo = emergencyRepo,
            onCodeGenerated = { code -> currentCode = code },
            onError = { error -> errorMessage = error },
            onLoadingChange = { loading -> isLoading = loading }
        )
    }
    
    // Countdown timer
    LaunchedEffect(currentCode) {
        if (currentCode != null) {
            while (timeRemaining > 0) {
                kotlinx.coroutines.delay(1000)
                timeRemaining--
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Exit Code", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9800),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF8E1),
                            Color(0xFFFFFFFF),
                            Color(0xFFFFF3E0)
                        )
                    )
                ),
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
                    Button(
                        onClick = {
                            errorMessage = null
                            scope.launch {
                                generateNewCode(
                                    userRepository = userRepository,
                                    emergencyRepo = emergencyRepo,
                                    onCodeGenerated = { code -> currentCode = code },
                                    onError = { error -> errorMessage = error },
                                    onLoadingChange = { loading -> isLoading = loading }
                                )
                            }
                        }
                    ) {
                        Text("Retry")
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
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFFF9800)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Emergency Exit Code",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Share this code with your child to exit Focus Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF757575)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Emergency Code Display
                    currentCode?.let { code ->
                        Card(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Emergency Code",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF757575)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = code,
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800),
                                    letterSpacing = 8.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Timer and Usage Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Valid for:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${timeRemaining / 3600}h ${(timeRemaining % 3600) / 60}m",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (timeRemaining < 3600) Color(0xFFF44336) else Color(0xFF4CAF50)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Usage limit:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "3 times",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Generate New Code Button
                    Button(
                        onClick = {
                            scope.launch {
                                generateNewCode(
                                    userRepository = userRepository,
                                    emergencyRepo = emergencyRepo,
                                    onCodeGenerated = { code -> 
                                        currentCode = code
                                        timeRemaining = 24 * 60 * 60 // Reset timer
                                    },
                                    onError = { error -> errorMessage = error },
                                    onLoadingChange = { loading -> isLoading = loading }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate New Code")
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
                                text = "How to use:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. Share this code with your child\n" +
                                        "2. In Focus Mode, tap 'Exit' button\n" +
                                        "3. Enter this emergency code\n" +
                                        "4. Focus Mode will exit immediately",
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

private suspend fun generateNewCode(
    userRepository: UserRepository,
    emergencyRepo: EmergencyCodeRepository,
    onCodeGenerated: (String) -> Unit,
    onError: (String) -> Unit,
    onLoadingChange: (Boolean) -> Unit
) {
    onLoadingChange(true)
    
    val parentId = userRepository.getCurrentUserId()
    if (parentId != null) {
        emergencyRepo.generateEmergencyCode(
            parentId = parentId,
            maxUsage = 3, // Can be used 3 times
            expiresInHours = 24 // Expires in 24 hours
        ).onSuccess { code ->
            onCodeGenerated(code)
            onLoadingChange(false)
        }.onFailure { error ->
            onError(error.message ?: "Failed to generate code")
            onLoadingChange(false)
        }
    } else {
        onError("Unable to get parent ID")
        onLoadingChange(false)
    }
}