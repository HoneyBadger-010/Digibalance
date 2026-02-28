package com.CuriosityLabs.digibalance.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CuriosityLabs.digibalance.R
import com.CuriosityLabs.digibalance.data.repository.FeedbackRepository
import com.CuriosityLabs.digibalance.data.repository.UserRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(userRole: UserRole) {
    val scope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf("Bug Report") }
    var feedbackMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    val categories = listOf(
        "Bug Report",
        "Feature Request",
        "General Comment",
        "Performance Issue",
        "UI/UX Feedback"
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFCE4EC).copy(alpha = 0.3f),
                        Color(0xFFFFFFFF),
                        Color(0xFFE1F5FE).copy(alpha = 0.3f)
                    )
                )
            ),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Feedback",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Help us improve DigiBalance",
                        fontSize = 15.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "We'd love to hear from you",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Share your feedback, report bugs, or suggest new features.",
                        fontSize = 15.sp,
                        color = Color(0xFF757575),
                        lineHeight = 22.sp
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        item {
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
        
        item {
            var expanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Category") },
                    trailingIcon = { 
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A),
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedLabelColor = Color(0xFF2196F3),
                        unfocusedLabelColor = Color(0xFF757575),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .exposedDropdownSize()
                        .background(Color.White)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = category,
                                    color = Color(0xFF1A1A1A),
                                    fontWeight = FontWeight.Medium
                                ) 
                            },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color(0xFF1A1A1A)
                            ),
                            modifier = Modifier.background(Color.White)
                        )
                    }
                }
            }
        }
        
        item {
            Text(
                text = "Your Message",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
        
        item {
            OutlinedTextField(
                value = feedbackMessage,
                onValueChange = { feedbackMessage = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                label = { Text("Tell us more...", color = Color(0xFF424242)) },
                placeholder = { Text("Describe your feedback in detail", color = Color(0xFF9E9E9E)) },
                maxLines = 10,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF1A1A1A),
                    unfocusedTextColor = Color(0xFF1A1A1A),
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = Color(0xFFBDBDBD),
                    focusedLabelColor = Color(0xFF2196F3),
                    unfocusedLabelColor = Color(0xFF757575),
                    cursorColor = Color(0xFF2196F3)
                )
            )
        }
        
        item {
            Button(
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        val userRepository = UserRepository()
                        val userId = userRepository.getCurrentUserId()
                        
                        // Submit feedback to database
                        val feedbackRepository = FeedbackRepository()
                        val result = feedbackRepository.submitFeedback(
                            userId = userId,
                            category = selectedCategory,
                            message = feedbackMessage
                        )
                        
                        result.onSuccess {
                            android.util.Log.d("HelpScreen", "Feedback submitted successfully")
                            showSuccessMessage = true
                            feedbackMessage = ""
                        }.onFailure { error ->
                            android.util.Log.e("HelpScreen", "Failed to submit feedback: ${error.message}")
                        }
                        
                        isSubmitting = false
                        
                        // Hide success message after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        showSuccessMessage = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = feedbackMessage.isNotBlank() && !isSubmitting,
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF2196F3),
                                    Color(0xFF1976D2)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            "Submit Feedback",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
        
        if (showSuccessMessage) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text(
                        text = "✓ Feedback submitted successfully!",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
