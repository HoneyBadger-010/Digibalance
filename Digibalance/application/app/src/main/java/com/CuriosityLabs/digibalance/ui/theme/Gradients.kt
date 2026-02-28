package com.CuriosityLabs.digibalance.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Modern Gradient Definitions for DigiBalance

// Primary Gradients
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF667EEA), // Purple-Blue
        Color(0xFF764BA2)  // Purple
    )
)

val SecondaryGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4FACFE), // Light Blue
        Color(0xFF00F2FE)  // Cyan
    )
)

val SuccessGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF11998E), // Teal
        Color(0xFF38EF7D)  // Green
    )
)

val WarningGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFB75E), // Orange
        Color(0xFFED8F03)  // Dark Orange
    )
)

val ErrorGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFF6B6B), // Light Red
        Color(0xFFEE5A52)  // Red
    )
)

// Background Gradients
val LightBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF8FAFC), // Very light gray
        Color(0xFFFFFFFF), // Pure white
        Color(0xFFF1F5F9)  // Light blue-gray
    )
)

val CardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF), // White
        Color(0xFFFAFBFC)  // Very light gray
    )
)

val FocusGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFEBF4FF), // Light blue
        Color(0xFFFFFFFF), // White
        Color(0xFFF0F9FF), // Very light blue
        Color(0xFFFEF7FF)  // Very light purple
    )
)

val RankGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF7ED), // Light orange
        Color(0xFFFFFFFF), // White
        Color(0xFFFEF3C7)  // Light yellow
    )
)

val ReportGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFECFDF5), // Light green
        Color(0xFFFFFFFF), // White
        Color(0xFFF0FDF4)  // Very light green
    )
)

// Button Gradients
val PrimaryButtonGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF2196F3), // Material Blue
        Color(0xFF1976D2)  // Darker Blue
    )
)

val SecondaryButtonGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF06B6D4), // Cyan
        Color(0xFF0891B2)  // Darker Cyan
    )
)

val SuccessButtonGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF10B981), // Emerald
        Color(0xFF059669)  // Darker Emerald
    )
)

val WarningButtonGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFF59E0B), // Amber
        Color(0xFFD97706)  // Darker Amber
    )
)

val ErrorButtonGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFEF4444), // Red
        Color(0xFFDC2626)  // Darker Red
    )
)

// Card Shadow Colors
val CardShadowLight = Color(0x1A000000) // 10% black
val CardShadowMedium = Color(0x33000000) // 20% black
val CardShadowDark = Color(0x4D000000) // 30% black

// Accent Colors for UI Elements
val AccentBlue = Color(0xFF3B82F6)
val AccentGreen = Color(0xFF10B981)
val AccentPurple = Color(0xFF8B5CF6)
val AccentOrange = Color(0xFFF59E0B)
val AccentRed = Color(0xFFEF4444)
val AccentTeal = Color(0xFF06B6D4)

// Text Colors with Better Contrast
val TextPrimaryDark = Color(0xFF111827)
val TextSecondaryDark = Color(0xFF6B7280)
val TextTertiaryDark = Color(0xFF9CA3AF)
val TextOnPrimary = Color(0xFFFFFFFF)
val TextOnSecondary = Color(0xFFFFFFFF)

// Surface Colors
val SurfaceElevated = Color(0xFFFFFFFF)
val SurfaceContainer = Color(0xFFF9FAFB)
val SurfaceVariant = Color(0xFFF3F4F6)
val SurfaceDim = Color(0xFFE5E7EB)