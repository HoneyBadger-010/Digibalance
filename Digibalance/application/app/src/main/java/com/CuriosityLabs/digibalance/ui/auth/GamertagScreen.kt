package com.CuriosityLabs.digibalance.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.CuriosityLabs.digibalance.data.repository.UserRepository
import kotlinx.coroutines.launch

@Composable
fun GamertagScreen(
    userId: String,
    onGamertagCreated: (String) -> Unit
) {
    var gamertag by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }
    var isAvailable by remember { mutableStateOf<Boolean?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFF3E5F5)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose Your Gamertag",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "This will be visible on the leaderboard",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF424242)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = gamertag,
            onValueChange = { 
                if (it.length <= 12 && it.matches(Regex("[a-zA-Z0-9_]*"))) {
                    gamertag = it
                    isAvailable = null
                    errorMessage = null
                }
            },
            label = { Text("Gamertag", color = Color(0xFF424242)) },
            supportingText = { 
                Text(
                    "${gamertag.length}/12 characters (letters, numbers, underscore only)",
                    color = Color(0xFF757575)
                )
            },
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1A1A1A),
                unfocusedTextColor = Color(0xFF1A1A1A),
                focusedBorderColor = Color(0xFF2196F3),
                unfocusedBorderColor = Color(0xFF9E9E9E),
                cursorColor = Color(0xFF2196F3),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        if (isAvailable == true) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "✓ Available!",
                color = Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                scope.launch {
                    isChecking = true
                    errorMessage = null
                    
                    // Validate length
                    if (gamertag.length < 3) {
                        errorMessage = "Gamertag must be at least 3 characters"
                        isChecking = false
                        return@launch
                    }
                    
                    // Update gamertag in Supabase
                    val result = userRepository.updateGamertag(userId, gamertag)
                    
                    result.onSuccess {
                        isAvailable = true
                        onGamertagCreated(gamertag)
                    }.onFailure { error ->
                        errorMessage = error.message ?: "Failed to create gamertag"
                        isAvailable = false
                    }
                    
                    isChecking = false
                }
            },
            enabled = gamertag.length >= 3 && !isChecking,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            if (isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Continue")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "You can change your gamertag once per month",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF757575)
        )
    }
}
