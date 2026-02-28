package com.CuriosityLabs.digibalance.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DigiBalanceLightBlue,
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFD1E4FF),
    
    secondary = DigiBalanceTeal,
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF004D61),
    onSecondaryContainer = Color(0xFFB3E5FC),
    
    tertiary = DigiBalancePurple,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF4A148C),
    onTertiaryContainer = Color(0xFFE1BEE7),
    
    error = DigiBalanceRed,
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = Color(0xFF121212),
    onBackground = Color(0xFFE1E1E1),
    
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE1E1E1),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFC4C4C4),
    
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFF424242),
    
    inverseSurface = Color(0xFFE1E1E1),
    inverseOnSurface = Color(0xFF1E1E1E),
    inversePrimary = DigiBalanceBlue,
    
    surfaceTint = DigiBalanceLightBlue,
    scrim = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = DigiBalanceBlue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    
    secondary = DigiBalanceTeal,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF006064),
    
    tertiary = DigiBalancePurple,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF3E5F5),
    onTertiaryContainer = Color(0xFF4A148C),
    
    error = DigiBalanceRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    
    background = DigiBalanceLightGray,
    onBackground = DigiBalanceBlack,
    
    surface = DigiBalanceWhite,
    onSurface = DigiBalanceBlack,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = DigiBalanceGray,
    
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),
    
    inverseSurface = DigiBalanceDarkGray,
    inverseOnSurface = DigiBalanceWhite,
    inversePrimary = DigiBalanceLightBlue,
    
    surfaceTint = DigiBalanceBlue,
    scrim = Color(0xFF000000)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // FORCE LIGHT MODE ALWAYS
    dynamicColor: Boolean = false, // DISABLE DYNAMIC COLORS
    content: @Composable () -> Unit
) {
    // ALWAYS USE LIGHT COLOR SCHEME - NO DARK MODE
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}