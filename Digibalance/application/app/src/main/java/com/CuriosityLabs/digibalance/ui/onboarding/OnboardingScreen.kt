package com.CuriosityLabs.digibalance.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CuriosityLabs.digibalance.ui.theme.*

data class OnboardingPage(
    val headline: String,
    val bodyText: String,
    val icon: ImageVector,
    val gradientColors: List<androidx.compose.ui.graphics.Color>,
    val iconBackgroundColor: androidx.compose.ui.graphics.Color
)

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            headline = "Find Your Unbreakable Focus",
            bodyText = "Block distracting apps and create a secure 'Focus Mode'. Your rules stay active even if you restart your phone.",
            icon = Icons.Default.Lock,
            gradientColors = listOf(
                DigiBalanceDarkBlue.copy(alpha = 0.1f),
                DigiBalanceBlue.copy(alpha = 0.05f),
                DigiBalanceWhite
            ),
            iconBackgroundColor = DigiBalanceBlue
        ),
        OnboardingPage(
            headline = "Guide Their Digital Habits",
            bodyText = "Set app limits, manage screen time, and get detailed reports. Your rules work instantly, even when your child's device is offline.",
            icon = Icons.Default.People,
            gradientColors = listOf(
                DigiBalanceBlue.copy(alpha = 0.1f),
                DigiBalanceLightBlue.copy(alpha = 0.05f),
                DigiBalanceWhite
            ),
            iconBackgroundColor = DigiBalanceDarkBlue
        ),
        OnboardingPage(
            headline = "Make Productivity Rewarding",
            bodyText = "Compete on the global leaderboard, earn badges for your progress, and turn your focused time into a game.",
            icon = Icons.Default.Star,
            gradientColors = listOf(
                DigiBalanceLightBlue.copy(alpha = 0.1f),
                DigiBalanceBlue.copy(alpha = 0.05f),
                DigiBalanceWhite
            ),
            iconBackgroundColor = DigiBalanceBlue
        )
    )

    val configuration = LocalConfiguration.current
    val screenWidth = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    
    var currentPage by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DigiBalanceWhite)
    ) {
        // Background gradient for current page
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = pages[currentPage].gradientColors,
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page Content with swipe gesture
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                val threshold = screenWidth * 0.25f
                                when {
                                    dragOffset > threshold && currentPage > 0 -> {
                                        currentPage--
                                    }
                                    dragOffset < -threshold && currentPage < pages.size - 1 -> {
                                        currentPage++
                                    }
                                }
                                dragOffset = 0
                            }
                        ) { _, dragAmount ->
                            dragOffset = (dragOffset + dragAmount).toInt()
                        }
                    }
            ) {
                OnboardingPageContent(page = pages[currentPage])
            }

            // Page Indicators
            Row(
                modifier = Modifier
                    .padding(vertical = 32.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.forEachIndexed { index, _ ->
                    PageIndicator(
                        isSelected = index == currentPage,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip/Previous button
                if (currentPage > 0) {
                    TextButton(
                        onClick = { currentPage-- },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("<", color = DigiBalanceBlue, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Next/Get Started button
                if (currentPage < pages.size - 1) {
                    Button(
                        onClick = { currentPage++ },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DigiBalanceBlue
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Text(">", color = DigiBalanceWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onGetStarted,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DigiBalanceBlue
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = "Get Started",
                            color = DigiBalanceWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Get Started",
                            tint = DigiBalanceWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    val iconScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "iconScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 100.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Icon with elegant background
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(iconScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            page.iconBackgroundColor.copy(alpha = 0.2f),
                            page.iconBackgroundColor.copy(alpha = 0.1f)
                        ),
                        center = Offset(70f, 70f),
                        radius = 70f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(page.iconBackgroundColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = page.iconBackgroundColor,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        // Headline
        Text(
            text = page.headline,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DigiBalanceDarkBlue,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Body Text
        Text(
            text = page.bodyText,
            fontSize = 16.sp,
            color = DigiBalanceBlack.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun PageIndicator(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val width by animateFloatAsState(
        targetValue = if (isSelected) 32f else 8f,
        animationSpec = tween(300),
        label = "indicatorWidth"
    )

    Box(
        modifier = modifier
            .height(8.dp)
            .width(with(density) { width.dp })
            .clip(RoundedCornerShape(4.dp))
            .background(
                color = if (isSelected) DigiBalanceBlue else DigiBalanceBlue.copy(alpha = 0.3f)
            )
    )
}
