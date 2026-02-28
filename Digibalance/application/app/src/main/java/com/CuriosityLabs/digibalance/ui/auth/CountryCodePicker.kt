package com.CuriosityLabs.digibalance.ui.auth

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.hbb20.CountryCodePicker
import com.CuriosityLabs.digibalance.ui.theme.*

/**
 * Integrated phone number input with Country Code Picker.
 * Provides E.164 format output (e.g., +911234567890).
 */
@Composable
fun PhoneNumberInputWithCCP(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    label: String = "Phone Number",
    placeholder: String = "Enter phone number",
    borderColor: Color = DigiBalanceBlue.copy(alpha = 0.3f),
    focusedBorderColor: Color = DigiBalanceTeal,
    textColor: Color = DigiBalanceDarkBlue
) {
    val context = LocalContext.current
    var countryCode by remember { mutableStateOf("+1") }
    var carrierNumber by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var isInternalUpdate by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    
    // Initialize from value only once
    LaunchedEffect(Unit) {
        if (!isInitialized && value.isNotEmpty()) {
            if (value.startsWith("+")) {
                // Extract country code (1-4 digits after +)
                val codeMatch = Regex("^(\\+\\d{1,4})").find(value)
                if (codeMatch != null) {
                    countryCode = codeMatch.value
                    carrierNumber = value.substring(codeMatch.value.length)
                } else {
                    carrierNumber = value.replace("+", "")
                }
            } else {
                carrierNumber = value
            }
            isInitialized = true
        } else if (!isInitialized) {
            isInitialized = true
        }
    }
    
    // Handle external value changes (only if significantly different)
    LaunchedEffect(value) {
        if (isInternalUpdate) {
            isInternalUpdate = false
            return@LaunchedEffect
        }
        
        // Calculate what we expect the value to be
        val expectedValue = if (carrierNumber.isNotEmpty()) {
            "$countryCode$carrierNumber"
        } else {
            countryCode
        }
        
        // If value matches what we expect, user is typing - don't re-parse
        if (value == expectedValue) {
            return@LaunchedEffect
        }
        
        // Only re-parse if value starts with a different country code
        // This handles external updates but preserves user's country selection
        if (value.startsWith("+") && value.startsWith(countryCode)) {
            // Same country code, just update carrier number
            carrierNumber = value.substring(countryCode.length)
        } else if (value.startsWith("+") && !value.startsWith(countryCode)) {
            // Different country code - this is an external change, parse it
            val codeMatch = Regex("^(\\+\\d{1,4})").find(value)
            if (codeMatch != null) {
                countryCode = codeMatch.value
                carrierNumber = value.substring(codeMatch.value.length)
            }
        } else if (!value.startsWith("+")) {
            // No country code in value, keep current country code
            carrierNumber = value
        }
    }
    
    // Update parent when country code or carrier number changes
    LaunchedEffect(countryCode, carrierNumber) {
        if (!isInitialized) return@LaunchedEffect
        
        val fullNumber = if (carrierNumber.isNotEmpty()) {
            "$countryCode$carrierNumber"
        } else {
            countryCode
        }
        if (fullNumber != value) {
            isInternalUpdate = true
            onValueChange(fullNumber)
        }
    }
    
    // Container with border
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (isFocused) focusedBorderColor else borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Country Code Picker
            AndroidView(
                factory = { ctx ->
                    createStyledCCP(
                        context = ctx,
                        currentCountryCode = countryCode,
                        textColor = textColor,
                        enabled = enabled,
                        onCountrySelected = { newCountryCode ->
                            countryCode = newCountryCode
                        }
                    )
                },
                update = { ccp ->
                    // Update if country code changes externally
                    if (ccp.selectedCountryCodeWithPlus != countryCode) {
                        try {
                            val codeWithoutPlus = countryCode.replace("+", "")
                            ccp.setCountryForPhoneCode(codeWithoutPlus.toIntOrNull() ?: 1)
                        } catch (e: Exception) {
                            // Ignore if conversion fails
                        }
                    }
                },
                modifier = Modifier
                    .width(110.dp)
                    .height(48.dp)
            )
            
            // Divider
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(
                        borderColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // Phone number TextField
            TextField(
                value = carrierNumber,
                onValueChange = { newValue ->
                    // Only allow digits
                    val digitsOnly = newValue.filter { it.isDigit() }
                    carrierNumber = digitsOnly
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                placeholder = {
                    Text(
                        text = placeholder,
                        color = textColor.copy(alpha = 0.4f),
                        fontSize = 16.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = focusedBorderColor
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = enabled,
                shape = RoundedCornerShape(0.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = textColor
                )
            )
        }
    }
}

/**
 * Creates a styled CountryCodePicker for dark/high-contrast theme.
 */
private fun createStyledCCP(
    context: Context,
    currentCountryCode: String,
    textColor: Color,
    enabled: Boolean,
    onCountrySelected: (String) -> Unit
): CountryCodePicker {
    return CountryCodePicker(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        // Basic configuration
        showFlag(true)
        showFullName(false)
        showNameCode(false)
        
        // Set default country
        try {
            if (currentCountryCode.isNotEmpty() && currentCountryCode != "+1") {
                val codeWithoutPlus = currentCountryCode.replace("+", "")
                val codeInt = codeWithoutPlus.toIntOrNull()
                if (codeInt != null) {
                    setCountryForPhoneCode(codeInt)
                } else {
                    setDefaultCountryUsingNameCode("US")
                }
            } else {
                setDefaultCountryUsingNameCode("US")
            }
        } catch (e: Exception) {
            setDefaultCountryUsingNameCode("US")
        }
        
        // Styling for high contrast / dark theme
        val textColorInt = android.graphics.Color.rgb(
            (textColor.red * 255).toInt().coerceIn(0, 255),
            (textColor.green * 255).toInt().coerceIn(0, 255),
            (textColor.blue * 255).toInt().coerceIn(0, 255)
        )
        
        setContentColor(textColorInt)
        setArrowColor(textColorInt)
        
        // Try to set dialog text color if method exists
        try {
            val dialogTextColorMethod = CountryCodePicker::class.java.getMethod("setDialogTextColor", Int::class.java)
            dialogTextColorMethod.invoke(this, textColorInt)
        } catch (e: Exception) {
            // Method doesn't exist in this version, skip
        }
        
        // Dark dialog background
        setDialogBackgroundColor(android.graphics.Color.parseColor("#1A1A1A"))
        
        // Enable/disable
        isEnabled = enabled
        setCcpClickable(enabled)
        
        // Register listener
        setOnCountryChangeListener {
            val newCode = selectedCountryCodeWithPlus
            onCountrySelected(newCode)
        }
    }
}

/**
 * Extracts country code from E.164 format phone number.
 */
fun extractCountryCode(phoneNumber: String): String {
    if (phoneNumber.startsWith("+")) {
        val match = Regex("^(\\+\\d{1,4})").find(phoneNumber)
        return match?.value ?: "+1"
    }
    return "+1"
}

/**
 * Extracts carrier number (without country code) from E.164 format.
 */
fun extractCarrierNumber(phoneNumber: String): String {
    if (phoneNumber.startsWith("+")) {
        val match = Regex("^(\\+\\d{1,4})").find(phoneNumber)
        return if (match != null) {
            phoneNumber.substring(match.value.length)
        } else {
            phoneNumber.replace("+", "")
        }
    }
    return phoneNumber
}
