package com.CuriosityLabs.digibalance.ui.survey

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CuriosityLabs.digibalance.ui.theme.*
import kotlinx.coroutines.delay

data class SurveyData(
    var dailyScreenTimeHours: Float? = null,
    var ageBracket: String? = null,
    var occupation: String? = null
)

@Composable
fun SurveyScreen(
    onSurveyComplete: (SurveyData) -> Unit,
    onSkip: () -> Unit = {}
) {
    var dailyScreenTimeHours by remember { mutableStateOf<Float?>(null) }
    var ageBracket by remember { mutableStateOf<String?>(null) }
    var occupation by remember { mutableStateOf<String?>(null) }
    var currentScreen by remember { mutableIntStateOf(0) }
    var showTransition by remember { mutableStateOf(false) }

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
    ) {
        // Progress indicator and skip button
        if (!showTransition && currentScreen < 3) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Progress dots
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(4.dp)
                                    .width(if (index == currentScreen) 32.dp else 16.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (index == currentScreen) 
                                            Color(0xFF3D5AFE) 
                                        else 
                                            Color(0xFFBDBDBD)
                                    )
                            )
                        }
                    }
                    
                    // Skip button
                    TextButton(onClick = onSkip) {
                        Text(
                            text = "Skip",
                            color = Color(0xFF757575),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Screen content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                showTransition -> {
                    TransitionScreen(
                        screenTimeHours = dailyScreenTimeHours ?: 4.5f,
                        onComplete = {
                            showTransition = false
                            currentScreen = 3
                        }
                    )
                }
                currentScreen == 3 -> {
                    ProjectionScreen(
                        surveyData = SurveyData(
                            dailyScreenTimeHours = dailyScreenTimeHours,
                            ageBracket = ageBracket,
                            occupation = occupation
                        ),
                        onContinue = { 
                            onSurveyComplete(SurveyData(
                                dailyScreenTimeHours = dailyScreenTimeHours,
                                ageBracket = ageBracket,
                                occupation = occupation
                            ))
                        },
                        onBack = {
                            showTransition = false
                            currentScreen = 2
                        }
                    )
                }
                currentScreen == 0 -> {
                    ScreenTimeScreen(
                        selectedValue = dailyScreenTimeHours,
                        onSelect = { dailyScreenTimeHours = it },
                        onNext = { currentScreen = 1 },
                        onBack = null // First screen, no back button
                    )
                }
                currentScreen == 1 -> {
                    AgeScreen(
                        selectedValue = ageBracket,
                        onSelect = { ageBracket = it },
                        onNext = { currentScreen = 2 },
                        onBack = { currentScreen = 0 }
                    )
                }
                currentScreen == 2 -> {
                    OccupationScreen(
                        selectedValue = occupation,
                        onSelect = { occupation = it },
                        onNext = { showTransition = true },
                        onBack = { currentScreen = 1 }
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenTimeScreen(
    selectedValue: Float?,
    onSelect: (Float) -> Unit,
    onNext: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val options = listOf(
        "Less than 2 hours" to 1.5f,
        "2-3 hours" to 2.5f,
        "3-4 hours" to 3.5f,
        "4-5 hours" to 4.5f,
        "5-6 hours" to 5.5f,
        "6-7 hours" to 6.5f,
        "More than 7 hours" to 8.0f
    )

    SurveyScreenTemplate(
        title = "How much time do you spend on your phone daily?",
        subtitle = "Be honest. This helps us personalize your experience.",
        options = options.map { it.first },
        selectedOption = options.find { it.second == selectedValue }?.first,
        onSelect = { selected ->
            options.find { it.first == selected }?.second?.let { onSelect(it) }
        },
        onNext = onNext,
        onBack = onBack,
        canProceed = selectedValue != null
    )
}

@Composable
fun AgeScreen(
    selectedValue: String?,
    onSelect: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val options = listOf(
        "Under 18",
        "18-24",
        "25-34",
        "35-44",
        "45-54",
        "55+"
    )

    SurveyScreenTemplate(
        title = "How old are you?",
        subtitle = "This helps us understand your digital habits.",
        options = options,
        selectedOption = selectedValue,
        onSelect = onSelect,
        onNext = onNext,
        onBack = onBack,
        canProceed = selectedValue != null
    )
}

@Composable
fun OccupationScreen(
    selectedValue: String?,
    onSelect: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val options = listOf(
        "Student",
        "Software Development",
        "Healthcare",
        "Education",
        "Business/Finance",
        "Creative/Design",
        "Other"
    )

    SurveyScreenTemplate(
        title = "What do you do?",
        subtitle = "We'll tailor the experience to your needs.",
        options = options,
        selectedOption = selectedValue,
        onSelect = onSelect,
        onNext = onNext,
        onBack = onBack,
        canProceed = selectedValue != null
    )
}

@Composable
fun SurveyScreenTemplate(
    title: String,
    subtitle: String,
    options: List<String>,
    selectedOption: String?,
    onSelect: (String) -> Unit,
    onNext: () -> Unit,
    onBack: (() -> Unit)? = null,
    canProceed: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Title
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            lineHeight = 36.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Subtitle
        Text(
            text = subtitle,
            fontSize = 16.sp,
            color = Color(0xFF757575),
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Options (scrollable)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEach { option ->
                SurveyOption(
                    text = option,
                    isSelected = option == selectedOption,
                    onClick = { onSelect(option) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (onBack != null) 
                Arrangement.SpaceBetween 
            else 
                Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF757575)
                    )
                ) {
                    Text(
                        text = "Back",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Next button
            Button(
                onClick = onNext,
                enabled = canProceed,
                modifier = Modifier
                    .height(56.dp)
                    .then(if (onBack == null) Modifier.fillMaxWidth() else Modifier.weight(1f).padding(start = 8.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3D5AFE),
                    disabledContainerColor = Color(0xFFBDBDBD)
                )
            ) {
                Text(
                    text = "Next",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SurveyOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF3D5AFE))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF1A1A1A) else Color(0xFF424242),
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFF3D5AFE),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun TransitionScreen(
    screenTimeHours: Float,
    onComplete: () -> Unit
) {
    val isDramatic = screenTimeHours >= 7.0f
    
    LaunchedEffect(Unit) {
        delay(2500)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Text(
                text = if (isDramatic) {
                    "You have a serious\nfocus debt."
                } else {
                    "Some not-so-good news,\nand some great news."
                },
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDramatic) Color(0xFFFF5722) else Color(0xFF3D5AFE),
                textAlign = TextAlign.Center,
                lineHeight = 44.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (isDramatic) {
                    "Here's the cost..."
                } else {
                    "Here's what we found..."
                },
                fontSize = 18.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProjectionScreen(
    surveyData: SurveyData,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val screenTimeHours = surveyData.dailyScreenTimeHours ?: 4.5f
    
    // Calculations
    val daysPerYear = (screenTimeHours * 365) / 24
    val lifetimeProjection = calculateLifetimeProjection(screenTimeHours)
    
    var showStats by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(300)
        showStats = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 80.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Shocking headline
        Text(
            text = "At your current pace...",
            fontSize = 20.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Spacer(modifier = Modifier.weight(0.3f))

        // Main projection statistic
        AnimatedVisibility(
            visible = showStats,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(1000))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Large number
                Text(
                    text = String.format("%.1f", lifetimeProjection),
                    fontSize = 96.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF5722),
                    textAlign = TextAlign.Center
                )
                
                // Unit
                Text(
                    text = "YEARS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    letterSpacing = 4.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(
                    text = "of your life will be spent\nlooking down at your phone",
                    fontSize = 18.sp,
                    color = Color(0xFF424242),
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Secondary stat
        AnimatedVisibility(
            visible = showStats,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(1000, delayMillis = 500))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "That's",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = String.format("%.0f", daysPerYear),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF3D5AFE)
                    )
                    Text(
                        text = "days per year",
                        fontSize = 16.sp,
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = "Ready to take back control?",
                fontSize = 16.sp,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Back and Continue buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF757575)
                    )
                ) {
                    Text(
                        text = "Back",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                        .padding(start = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3D5AFE)
                    )
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun calculateLifetimeProjection(dailyHours: Float): Float {
    // Assumptions:
    // - Average lifespan: 80 years
    // - Waking hours per day: 16 hours
    // - Years of phone use: assume from age 10 to 80 = 70 years
    
    val yearsOfPhoneUse = 70f
    val hoursPerDay = dailyHours
    val totalHoursInLifetime = hoursPerDay * 365 * yearsOfPhoneUse
    
    // Calculate years spent on phone
    val yearsSpentOnPhone = (totalHoursInLifetime / (16 * 365))
    
    return yearsSpentOnPhone
}
