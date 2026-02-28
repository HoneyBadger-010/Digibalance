package com.CuriosityLabs.digibalance.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.CuriosityLabs.digibalance.data.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    navController: NavController,
    preferencesManager: PreferencesManager
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Load saved preferences (DARK MODE REMOVED - LIGHT MODE ONLY)
    var selectedAccentColor by remember { mutableStateOf(preferencesManager.accentColor) }
    var selectedChartType by remember { mutableStateOf(preferencesManager.chartType) }
    var compactView by remember { mutableStateOf(preferencesManager.compactView) }
    var showAnimations by remember { mutableStateOf(preferencesManager.showAnimations) }
    
    // Save preferences when they change
    LaunchedEffect(selectedAccentColor) {
        preferencesManager.accentColor = selectedAccentColor
    }
    LaunchedEffect(selectedChartType) {
        preferencesManager.chartType = selectedChartType
    }
    LaunchedEffect(compactView) {
        preferencesManager.compactView = compactView
    }
    LaunchedEffect(showAnimations) {
        preferencesManager.showAnimations = showAnimations
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Appearance",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF5F7FA),
                            Color(0xFFFFFFFF),
                            Color(0xFFEEF2F7)
                        )
                    )
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // DARK MODE REMOVED - App is now Light Mode only
            
            // Accent Color
            item {
                PreferenceSectionCard(
                    title = "Accent Color",
                    icon = Icons.Default.ColorLens,
                    description = "Personalize your app's look"
                ) {
                    AccentColorPicker(
                        selectedColor = selectedAccentColor,
                        onColorSelected = { selectedAccentColor = it }
                    )
                }
            }
            
            // Chart Type
            item {
                PreferenceSectionCard(
                    title = "Chart Style",
                    icon = Icons.Default.BarChart,
                    description = "Default visualization for reports"
                ) {
                    ChartTypePicker(
                        selectedType = selectedChartType,
                        onTypeSelected = { selectedChartType = it }
                    )
                }
            }
            
            // Display Options
            item {
                PreferenceSectionCard(
                    title = "Display Options",
                    icon = Icons.Default.DisplaySettings,
                    description = "Customize your viewing experience"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SwitchPreference(
                            title = "Compact View",
                            description = "Show more content on screen",
                            checked = compactView,
                            onCheckedChange = { compactView = it }
                        )
                        SwitchPreference(
                            title = "Animations",
                            description = "Enable smooth transitions",
                            checked = showAnimations,
                            onCheckedChange = { showAnimations = it }
                        )
                    }
                }
            }
            
            // Preview Card
            item {
                PreviewCard(
                    accentColor = selectedAccentColor,
                    chartType = selectedChartType
                )
            }
        }
    }
}

@Composable
fun PreferenceSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            content()
        }
    }
}

// DARK MODE REMOVED - Theme selector removed

@Composable
fun AccentColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        ColorOption("Blue", Color(0xFF2196F3)),
        ColorOption("Purple", Color(0xFF9C27B0)),
        ColorOption("Green", Color(0xFF4CAF50)),
        ColorOption("Orange", Color(0xFFFF9800)),
        ColorOption("Teal", Color(0xFF009688)),
        ColorOption("Pink", Color(0xFFE91E63))
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(colors) { color ->
            ColorCircle(
                color = color,
                isSelected = selectedColor == color.name,
                onClick = { onColorSelected(color.name) }
            )
        }
    }
}

@Composable
fun ColorCircle(
    color: ColorOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color.color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

data class ColorOption(val name: String, val color: Color)

@Composable
fun ChartTypePicker(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val chartTypes = listOf(
        ChartTypeOption("Pie", Icons.Default.PieChart),
        ChartTypeOption("Bar", Icons.Default.BarChart),
        ChartTypeOption("Line", Icons.Default.ShowChart)
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        chartTypes.forEach { chart ->
            ChartTypeCard(
                chartType = chart,
                isSelected = selectedType == chart.name,
                onClick = { onTypeSelected(chart.name) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ChartTypeCard(
    chartType: ChartTypeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = chartType.icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = chartType.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

data class ChartTypeOption(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun SwitchPreference(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFBDBDBD),
                uncheckedBorderColor = Color(0xFF9E9E9E)
            )
        )
    }
}

@Composable
fun PreviewCard(
    accentColor: String,
    chartType: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Preview,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Preview content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Your app will look like this",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Chip(text = "Light Mode")
                        Chip(text = accentColor)
                        Chip(text = "$chartType Chart")
                    }
                }
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// Duplicate code removed - using the first definition above
