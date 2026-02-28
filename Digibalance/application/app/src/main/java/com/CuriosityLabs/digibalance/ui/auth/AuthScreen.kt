package com.CuriosityLabs.digibalance.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CuriosityLabs.digibalance.ui.theme.*
import kotlinx.coroutines.launch

enum class AuthMode {
    SIGN_IN,
    SIGN_UP
}

enum class IdentifierType {
    EMAIL,
    PHONE
}

@Composable
fun AuthScreen(
    onSignInSuccess: (userId: String) -> Unit,
    onSignUpSuccess: (userId: String) -> Unit,
    onBack: () -> Unit = {}
) {
    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var identifierType by remember { mutableStateOf(IdentifierType.EMAIL) }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPassword by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val userRepository = remember { com.CuriosityLabs.digibalance.data.repository.UserRepository() }

    if (showForgotPassword) {
        ForgotPasswordScreen(
            onBack = { showForgotPassword = false },
            onResetLinkSent = { showForgotPassword = false }
        )
        return
    }

    // Validation states - More lenient email validation
    val isIdentifierValid = remember(identifier, identifierType) {
        when (identifierType) {
            IdentifierType.EMAIL -> {
                // Simple email validation: just check for @ and at least one character on each side
                identifier.contains("@") && 
                identifier.indexOf("@") > 0 && 
                identifier.indexOf("@") < identifier.length - 1
            }
            IdentifierType.PHONE -> identifier.startsWith("+") && identifier.length >= 10
        }
    }

    // Password validation rules
    val passwordMinLength = remember(password) { password.length >= 8 }
    val passwordHasLetter = remember(password) { password.any { it.isLetter() } }
    val passwordHasNumber = remember(password) { password.any { it.isDigit() } }
    val passwordHasSpecialChar = remember(password) { 
        password.any { it.isLetterOrDigit().not() && !it.isWhitespace() } 
    }
    
    val isPasswordValid = remember(password, authMode) {
        if (authMode == AuthMode.SIGN_UP) {
            passwordMinLength && passwordHasLetter && passwordHasNumber
        } else {
            password.isNotEmpty()
        }
    }

    val canSubmit = isIdentifierValid && isPasswordValid && !isLoading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DigiBalanceWhite)
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
                        .background(DigiBalanceBlue.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = DigiBalanceBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = if (authMode == AuthMode.SIGN_IN) "Sign in to DigiBalance" else "Create your account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DigiBalanceDarkBlue,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = if (authMode == AuthMode.SIGN_IN) 
                    "Enter your account details below" 
                else 
                    "Start your digital wellness journey",
                fontSize = 16.sp,
                color = DigiBalanceDarkBlue.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Identifier type toggle (Email/Phone)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                DigiBalanceBlue.copy(alpha = 0.15f),
                                DigiBalanceCyan.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Email option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (identifierType == IdentifierType.EMAIL) 
                                Brush.horizontalGradient(
                                    colors = listOf(DigiBalanceBlue, DigiBalanceIndigo)
                                )
                            else 
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, Color.Transparent)
                                )
                        )
                        .clickable { 
                            // TODO-7: Clear identifier when switching to email
                            identifierType = IdentifierType.EMAIL
                            identifier = ""
                            errorMessage = null
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (identifierType == IdentifierType.EMAIL) 
                            DigiBalanceWhite 
                        else 
                            DigiBalanceBlue
                    )
                }

                // Phone option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (identifierType == IdentifierType.PHONE) 
                                Brush.horizontalGradient(
                                    colors = listOf(DigiBalanceTeal, DigiBalanceCyan)
                                )
                            else 
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, Color.Transparent)
                                )
                        )
                        .clickable { 
                            // TODO-7: Clear identifier when switching to phone
                            identifierType = IdentifierType.PHONE
                            identifier = ""
                            errorMessage = null
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Phone",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (identifierType == IdentifierType.PHONE) 
                            DigiBalanceWhite 
                        else 
                            DigiBalanceTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Identifier input field
            if (identifierType == IdentifierType.EMAIL) {
                // Email input field
                OutlinedTextField(
                    value = identifier,
                    onValueChange = {
                        identifier = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "Email",
                            color = DigiBalanceDarkBlue.copy(alpha = 0.7f)
                        )
                    },
                    placeholder = {
                        Text(
                            text = "you@example.com",
                            color = DigiBalanceBlue.copy(alpha = 0.4f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = DigiBalanceIndigo
                        )
                    },
                    trailingIcon = {
                        if (identifier.isNotEmpty() && isIdentifierValid) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Valid",
                                tint = DigiBalanceGreen
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A),
                        focusedBorderColor = DigiBalanceIndigo,
                        unfocusedBorderColor = DigiBalanceBlue.copy(alpha = 0.3f),
                        focusedLabelColor = DigiBalanceIndigo,
                        cursorColor = DigiBalanceIndigo
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                // Phone input with Country Code Picker
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Label
                    Text(
                        text = "Phone Number",
                        fontSize = 12.sp,
                        color = DigiBalanceDarkBlue.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    // Phone input with CCP
                    PhoneNumberInputWithCCP(
                        value = identifier,
                        onValueChange = {
                            identifier = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        isError = identifier.isNotEmpty() && !isIdentifierValid,
                        label = "Phone Number",
                        placeholder = "1234567890",
                        borderColor = if (identifier.isNotEmpty() && !isIdentifierValid) 
                            DigiBalanceOrange.copy(alpha = 0.5f)
                        else 
                            DigiBalanceBlue.copy(alpha = 0.3f),
                        focusedBorderColor = DigiBalanceTeal,
                        textColor = DigiBalanceDarkBlue
                    )
                    
                    // Validation icon (trailing)
                    if (identifier.isNotEmpty() && isIdentifierValid) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Valid",
                                tint = DigiBalanceGreen,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp, top = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Password input field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { 
                    Text(
                        "Password", 
                        color = DigiBalanceDarkBlue.copy(alpha = 0.7f)
                    ) 
                },
                placeholder = {
                    Text(
                        text = if (authMode == AuthMode.SIGN_UP) 
                            "Min 8 chars, letter & number" 
                        else 
                            "Enter your password",
                        color = DigiBalanceBlue.copy(alpha = 0.4f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = DigiBalancePurple
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) 
                                Icons.Default.Visibility 
                            else 
                                Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = DigiBalanceCyan
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF1A1A1A),
                    unfocusedTextColor = Color(0xFF1A1A1A),
                    focusedBorderColor = DigiBalancePurple,
                    unfocusedBorderColor = DigiBalanceBlue.copy(alpha = 0.3f),
                    focusedLabelColor = DigiBalancePurple,
                    cursorColor = DigiBalancePurple
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Password requirements indicator (only for sign up)
            if (authMode == AuthMode.SIGN_UP && password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Password requirements:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = DigiBalanceDarkBlue.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    PasswordRequirement(
                        text = "At least 8 characters",
                        isValid = passwordMinLength
                    )
                    PasswordRequirement(
                        text = "Contains a letter",
                        isValid = passwordHasLetter
                    )
                    PasswordRequirement(
                        text = "Contains a number",
                        isValid = passwordHasNumber
                    )
                    PasswordRequirement(
                        text = "Contains a special character (optional)",
                        isValid = passwordHasSpecialChar,
                        isOptional = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Forgot password link (only for sign in)
            if (authMode == AuthMode.SIGN_IN) {
                Text(
                    text = "Forgot password?",
                    fontSize = 14.sp,
                    color = DigiBalanceBlue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showForgotPassword = true
                        },
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DigiBalanceOrange.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        fontSize = 14.sp,
                        color = DigiBalanceOrange,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Submit button with gradient
            Button(
                onClick = {
                    if (canSubmit) {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            
                            try {
                                if (authMode == AuthMode.SIGN_UP) {
                                    // Sign Up Flow
                                    val result = if (identifierType == IdentifierType.EMAIL) {
                                        userRepository.signUpWithEmail(identifier, password)
                                    } else {
                                        userRepository.signUpWithPhone(identifier, password)
                                    }
                                    
                                    result.onSuccess { userId ->
                                        // Create user profile in users table with NULL role
                                        // Role will be set when user selects it
                                        val profileResult = userRepository.createUserProfile(
                                            userId = userId,
                                            email = if (identifierType == IdentifierType.EMAIL) identifier else null,
                                            phone = if (identifierType == IdentifierType.PHONE) identifier else null,
                                            role = null, // NULL until user selects role
                                            displayName = ""
                                        )
                                        
                                        profileResult.onSuccess {
                                            isLoading = false
                                            onSignUpSuccess(userId)
                                        }.onFailure { error ->
                                            isLoading = false
                                            errorMessage = "Profile creation failed: ${error.message}"
                                        }
                                    }.onFailure { error ->
                                        isLoading = false
                                        errorMessage = when {
                                            error.message?.contains("already registered") == true -> 
                                                "This ${if (identifierType == IdentifierType.EMAIL) "email" else "phone"} is already registered"
                                            error.message?.contains("Invalid") == true -> 
                                                "Invalid ${if (identifierType == IdentifierType.EMAIL) "email" else "phone number"}"
                                            else -> error.message ?: "Sign up failed"
                                        }
                                    }
                                } else {
                                    // Sign In Flow
                                    val result = if (identifierType == IdentifierType.EMAIL) {
                                        userRepository.signInWithEmail(identifier, password)
                                    } else {
                                        userRepository.signInWithPhone(identifier, password)
                                    }
                                    
                                    result.onSuccess { userId ->
                                        isLoading = false
                                        onSignInSuccess(userId)
                                    }.onFailure { error ->
                                        isLoading = false
                                        errorMessage = when {
                                            error.message?.contains("Invalid") == true -> 
                                                "Invalid credentials"
                                            error.message?.contains("not found") == true -> 
                                                "Account not found"
                                            else -> error.message ?: "Sign in failed"
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "An error occurred: ${e.message}"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (canSubmit) {
                            Brush.horizontalGradient(
                                colors = listOf(DigiBalanceBlue, DigiBalanceIndigo, DigiBalancePurple)
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
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (canSubmit) 4.dp else 0.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = DigiBalanceWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (authMode == AuthMode.SIGN_IN) "Sign in" else "Sign up",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canSubmit) DigiBalanceWhite else DigiBalanceWhite.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle between Sign In and Sign Up
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (authMode == AuthMode.SIGN_IN) 
                        "Don't have an account? " 
                    else 
                        "Already have an account? ",
                    fontSize = 14.sp,
                    color = DigiBalanceDarkBlue.copy(alpha = 0.6f)
                )
                Text(
                    text = if (authMode == AuthMode.SIGN_IN) "Sign up" else "Sign in",
                    fontSize = 14.sp,
                    color = DigiBalanceBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        authMode = if (authMode == AuthMode.SIGN_IN) 
                            AuthMode.SIGN_UP 
                        else 
                            AuthMode.SIGN_IN
                        errorMessage = null
                        password = ""
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PasswordRequirement(
    text: String,
    isValid: Boolean,
    isOptional: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = if (isValid) "Valid" else "Invalid",
            tint = if (isValid) DigiBalanceGreen else DigiBalanceOrange.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isValid) 
                DigiBalanceGreen 
            else if (isOptional) 
                DigiBalanceDarkBlue.copy(alpha = 0.5f)
            else 
                DigiBalanceOrange.copy(alpha = 0.8f),
            fontWeight = if (isValid) FontWeight.Medium else FontWeight.Normal
        )
    }
}

