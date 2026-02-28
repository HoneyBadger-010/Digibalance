package com.CuriosityLabs.digibalance.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CuriosityLabs.digibalance.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class ForgotPasswordState {
    object Initial : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class Success(val message: String) : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onResetLinkSent: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<ForgotPasswordState>(ForgotPasswordState.Initial) }
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-detect if input is email or phone
    val isEmail = remember(identifier) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(identifier).matches()
    }
    val isPhone = remember(identifier) {
        identifier.startsWith("+") && identifier.length >= 10
    }
    val isValid = isEmail || isPhone

    // Auto-redirect after success
    LaunchedEffect(state) {
        if (state is ForgotPasswordState.Success) {
            delay(3000)
            onResetLinkSent()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DigiBalanceBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DigiBalanceWhite.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = DigiBalanceWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Headline
            Text(
                text = "Forgot Your Password?",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DigiBalanceWhite,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "Enter the Email Address or Phone Number you used to sign up. We will send a secure password reset link to your registered account.",
                fontSize = 16.sp,
                color = DigiBalanceWhite.copy(alpha = 0.7f),
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Input field
            OutlinedTextField(
                value = identifier,
                onValueChange = {
                    identifier = it
                    state = ForgotPasswordState.Initial
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = "Email / Phone Number",
                        color = DigiBalanceWhite.copy(alpha = 0.7f)
                    )
                },
                placeholder = {
                    Text(
                        text = "you@example.com or +1234567890",
                        color = DigiBalanceWhite.copy(alpha = 0.4f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isEmail) 
                            Icons.Default.Email 
                        else if (isPhone) 
                            Icons.Default.Phone 
                        else 
                            Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = DigiBalanceCyan
                    )
                },
                trailingIcon = {
                    if (identifier.isNotEmpty() && isValid) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Valid",
                            tint = DigiBalanceGreen
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isEmail) 
                        KeyboardType.Email 
                    else 
                        KeyboardType.Phone
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DigiBalanceCyan,
                    unfocusedBorderColor = DigiBalanceWhite.copy(alpha = 0.3f),
                    focusedLabelColor = DigiBalanceCyan,
                    cursorColor = DigiBalanceCyan,
                    focusedTextColor = DigiBalanceWhite,
                    unfocusedTextColor = DigiBalanceWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Success/Error message
            AnimatedVisibility(visible = state is ForgotPasswordState.Success || state is ForgotPasswordState.Error) {
                when (val currentState = state) {
                    is ForgotPasswordState.Success -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DigiBalanceGreen.copy(alpha = 0.15f))
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = DigiBalanceGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = currentState.message,
                                    fontSize = 14.sp,
                                    color = DigiBalanceWhite,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                    is ForgotPasswordState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DigiBalanceOrange.copy(alpha = 0.15f))
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = DigiBalanceOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = currentState.message,
                                    fontSize = 14.sp,
                                    color = DigiBalanceWhite,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Send Reset Link button
            Button(
                onClick = {
                    if (isValid) {
                        state = ForgotPasswordState.Loading
                        // Simulate API call delay (no database connection - removed)
                        coroutineScope.launch {
                            delay(1500)
                            state = ForgotPasswordState.Success(
                                "Success! Check your ${if (isEmail) "email" else "phone"} for a reset link."
                            )
                        }
                    }
                },
                enabled = isValid && state !is ForgotPasswordState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isValid && state !is ForgotPasswordState.Loading) {
                                Brush.horizontalGradient(
                                    colors = listOf(DigiBalanceBlue, DigiBalanceCyan)
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        DigiBalanceBlue.copy(alpha = 0.3f),
                                        DigiBalanceBlue.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (state is ForgotPasswordState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = DigiBalanceWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Send Reset Link",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DigiBalanceWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
