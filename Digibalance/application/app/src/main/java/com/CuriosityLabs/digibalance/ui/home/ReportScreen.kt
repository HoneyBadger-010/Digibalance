package com.CuriosityLabs.digibalance.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.CuriosityLabs.digibalance.R
import com.CuriosityLabs.digibalance.util.AppUsageInfo
import com.CuriosityLabs.digibalance.util.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ReportScreen(userRole: UserRole) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPeriod by remember { mutableStateOf("Today") }
    var totalScreenTime by remember { mutableStateOf("0h 0m") }
    var productiveTime by remember { mutableStateOf("0h 0m") }
    var distractionTime by remember { mutableStateOf("0h 0m") }
    var topApps by remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedApp by remember { mutableStateOf<AppUsageInfo?>(null) }
    var yesterdayScreenTime by remember { mutableStateOf(0L) }
    
    LaunchedEffect(selectedPeriod) {
        scope.launch(Dispatchers.IO) {
            isLoading = true
            val usageList = when (selectedPeriod) {
                "Today" -> UsageStatsHelper.getTodayUsageStats(context)
                "Weekly" -> UsageStatsHelper.getWeeklyUsageStats(context)
                "Monthly" -> UsageStatsHelper.getMonthlyUsageStats(context)
                else -> emptyList()
            }
            
            val total = usageList.sumOf { it.usageTimeMillis }
            totalScreenTime = UsageStatsHelper.formatDuration(total)
            
            // Get yesterday's screen time for comparison
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val yesterdayStart = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val yesterdayEnd = calendar.timeInMillis
            
            val usageStatsManager = context.getSystemService(android.content.Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
            val yesterdayStats = usageStatsManager.queryUsageStats(
                android.app.usage.UsageStatsManager.INTERVAL_DAILY,
                yesterdayStart,
                yesterdayEnd
            )
            yesterdayScreenTime = yesterdayStats?.sumOf { it.totalTimeInForeground } ?: 0L
            
            // TODO: Get productive apps from database
            val productivePackages = setOf<String>()
            val productive = UsageStatsHelper.calculateProductiveTime(usageList, productivePackages)
            val distraction = UsageStatsHelper.calculateDistractionTime(usageList, productivePackages)
            
            productiveTime = UsageStatsHelper.formatDuration(productive)
            distractionTime = UsageStatsHelper.formatDuration(distraction)
            topApps = usageList.take(7)
            
            isLoading = false
        }
    }
    
    // Show app detail screen if an app is selected
    if (selectedApp != null) {
        AppDetailScreen(
            app = selectedApp!!,
            onBack = { selectedApp = null }
        )
    } else {
        // Main report screen
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFECFDF5), // Light green
                            Color(0xFFFFFFFF), // Pure white
                            Color(0xFFF0FDF4)  // Very light green
                        )
                    )
                )
        ) {
            // Header with screen time summary
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Screen time summary
                        Text(
                            text = "Today's screen time was $totalScreenTime",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            lineHeight = 32.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Comparison with yesterday
                        val todayMillis = parseTimeToMillis(totalScreenTime)
                        val diff = todayMillis - yesterdayScreenTime
                        val diffMinutes = (kotlin.math.abs(diff) / 60000).toInt()
                        
                        if (diffMinutes > 0) {
                            Text(
                                text = if (diff < 0) {
                                    "That's down $diffMinutes minutes from yesterday."
                                } else {
                                    "That's up $diffMinutes minutes from yesterday."
                                },
                                fontSize = 16.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }
            
            // Circular chart with app icons
            item {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    CircularAppUsageChart(
                        apps = topApps,
                        totalTime = totalScreenTime,
                        onAppClick = { app -> selectedApp = app }
                    )
                }
            }
            
            // Top Apps List Section
            if (!isLoading && topApps.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    Text(
                        text = "Top Apps",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        topApps.forEach { app ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedApp = app }
                            ) {
                                AppUsageCard(app)
                            }
                        }
                    }
                }
            }
            
            // Productivity vs Distraction Pie Charts
            if (!isLoading) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                item {
                    Text(
                        text = "Usage Breakdown",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Productive Apps Pie Chart
                        ProductivityPieChart(
                            title = "Productive",
                            time = productiveTime,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Distraction Apps Pie Chart
                        ProductivityPieChart(
                            title = "Distraction",
                            time = distractionTime,
                            color = Color(0xFFFF5252),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AppUsageCard(app: AppUsageInfo) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    
    // Try to get the app icon
    val appIcon = remember(app.packageName) {
        try {
            packageManager.getApplicationIcon(app.packageName)
        } catch (e: Exception) {
            null
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFAFAFA),
                            Color.White
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon - real or fallback
                if (appIcon != null) {
                    // Real app icon
                    Image(
                        painter = rememberDrawablePainter(drawable = appIcon),
                        contentDescription = app.appName,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    // Fallback icon with gradient
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF2196F3).copy(alpha = 0.2f),
                                        Color(0xFF1976D2).copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
                
                Column {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName.split(".").lastOrNull() ?: app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9E9E9E),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            
            // Time badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE3F2FD),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = UsageStatsHelper.formatDuration(app.usageTimeMillis),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    gradientColors: List<Color>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            gradientColors[0],
                            gradientColors[1],
                            gradientColors[0].copy(alpha = 0.8f)
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1A1A1A),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = value,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1A1A),
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF616161),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    // Decorative circle
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.4f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}


@Composable
fun TimeBarGraph(
    totalTime: String,
    productiveTime: String,
    distractionTime: String
) {
    // Parse time strings to get hours
    fun parseTime(time: String): Float {
        val parts = time.split(" ")
        var hours = 0f
        parts.forEachIndexed { index, part ->
            if (part.endsWith("h")) hours += part.dropLast(1).toFloatOrNull() ?: 0f
            if (part.endsWith("m")) hours += (part.dropLast(1).toFloatOrNull() ?: 0f) / 60f
        }
        return hours
    }
    
    val productiveHours = parseTime(productiveTime)
    val distractionHours = parseTime(distractionTime)
    val maxHours = maxOf(productiveHours + distractionHours, 1f)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Productive bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Productive",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(100.dp),
                    color = Color(0xFF4CAF50)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (productiveHours / maxHours).coerceIn(0f, 1f))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
                Text(
                    text = productiveTime,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp),
                    color = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Distraction bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Distraction",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(100.dp),
                    color = Color(0xFFFF5252)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (distractionHours / maxHours).coerceIn(0f, 1f))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF5252), Color(0xFFFF7043))
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
                Text(
                    text = distractionTime,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp),
                    color = Color(0xFFFF5252)
                )
            }
        }
    }
}

// Helper function to parse time string to milliseconds
fun parseTimeToMillis(time: String): Long {
    val parts = time.split(" ")
    var millis = 0L
    parts.forEachIndexed { index, part ->
        if (part.endsWith("h")) millis += (part.dropLast(1).toLongOrNull() ?: 0L) * 3600000L
        if (part.endsWith("m")) millis += (part.dropLast(1).toLongOrNull() ?: 0L) * 60000L
    }
    return millis
}

@Composable
fun CircularAppUsageChart(
    apps: List<AppUsageInfo>,
    totalTime: String,
    onAppClick: (AppUsageInfo) -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    
    // App colors for the chart
    val appColors = listOf(
        Color(0xFF4CAF50), // Green - WhatsApp
        Color(0xFFFFC107), // Yellow/Orange - Chrome
        Color(0xFFE91E63), // Pink - Instagram
        Color(0xFF000000), // Black - Netflix
        Color(0xFF2196F3), // Blue - Twitter
        Color(0xFFFF5252), // Red - YouTube
        Color(0xFF00BCD4)  // Cyan - Other
    )
    
    val totalMillis = apps.sumOf { it.usageTimeMillis }.toFloat()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(Color.White)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Draw circular chart - smaller radius, thicker stroke
        Canvas(
            modifier = Modifier.size(240.dp)
        ) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2f
            val strokeWidth = 50f
            val center = Offset(size.width / 2f, size.height / 2f)
            
            var startAngle = -90f
            
            apps.forEachIndexed { index, app ->
                val sweepAngle = (app.usageTimeMillis / totalMillis) * 360f
                
                drawArc(
                    color = appColors.getOrElse(index) { appColors.last() },
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                
                startAngle += sweepAngle
            }
        }
        
        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = totalTime,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
        
        // App icons positioned around the circle - smaller icons, closer distance
        apps.forEachIndexed { index, app ->
            val appIcon = remember(app.packageName) {
                try {
                    packageManager.getApplicationIcon(app.packageName)
                } catch (e: Exception) {
                    null
                }
            }
            
            val angle = -90f + apps.take(index).sumOf { it.usageTimeMillis }.toFloat() / totalMillis * 360f + 
                        (app.usageTimeMillis / totalMillis * 360f) / 2f
            val angleRad = Math.toRadians(angle.toDouble())
            val distance = 150f
            
            val x = (kotlin.math.cos(angleRad) * distance).toFloat()
            val y = (kotlin.math.sin(angleRad) * distance).toFloat()
            
            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onAppClick(app) },
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    Image(
                        painter = rememberDrawablePainter(drawable = appIcon),
                        contentDescription = app.appName,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(appColors.getOrElse(index) { appColors.last() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    app: AppUsageInfo,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var selectedView by remember { mutableStateOf("Daily") }
    
    val appIcon = remember(app.packageName) {
        try {
            packageManager.getApplicationIcon(app.packageName)
        } catch (e: Exception) {
            null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "More",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A1A))
                .padding(padding)
        ) {
            // App header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Digital Wellbeing &",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                    Text(
                        text = "parental controls",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }
            }
            
            // App icon and name
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (appIcon != null) {
                        Image(
                            painter = rememberDrawablePainter(drawable = appIcon),
                            contentDescription = app.appName,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF2196F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.appName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = app.appName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            // Time display with chart
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // View selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Screen time", "Daily", "Hourly").forEach { view ->
                            Surface(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clickable { selectedView = view },
                                shape = RoundedCornerShape(20.dp),
                                color = if (selectedView == view) Color(0xFF3D5AFE) else Color(0xFF2A2A2A)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = view,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    if (view != "Screen time") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Time display
                    Text(
                        text = UsageStatsHelper.formatDuration(app.usageTimeMillis),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        text = "Today",
                        fontSize = 16.sp,
                        color = Color(0xFF9E9E9E)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Simple bar chart
                    SimpleBarChart(app.usageTimeMillis)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Date
                    val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
                    Text(
                        text = dateFormat.format(Date()),
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
            
            // Settings section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9E9E9E),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    SettingsItem(
                        icon = "⏱",
                        title = "App timer",
                        subtitle = "No timer"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SettingsItem(
                        icon = "🔔",
                        title = "Manage notifications",
                        subtitle = null
                    )
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SimpleBarChart(usageTimeMillis: Long) {
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
    
    // Simulate weekly data with some variation
    val weeklyData = remember {
        List(7) { index ->
            if (index == currentDay) {
                usageTimeMillis
            } else {
                // Generate random data for other days (50-150% of current day)
                (usageTimeMillis * (0.5f + Math.random().toFloat())).toLong()
            }
        }
    }
    
    val maxUsage = weeklyData.maxOrNull() ?: 1L
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEachIndexed { index, day ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Bar with animation
                    val animatedHeight by animateFloatAsState(
                        targetValue = (weeklyData[index].toFloat() / maxUsage.toFloat() * 120f).coerceIn(10f, 120f),
                        animationSpec = tween(durationMillis = 800, delayMillis = index * 100)
                    )
                    
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(animatedHeight.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = if (index == currentDay) {
                                        listOf(Color(0xFF3D5AFE), Color(0xFF667EEA))
                                    } else {
                                        listOf(Color(0xFF9E9E9E), Color(0xFFBDBDBD))
                                    }
                                ),
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Day label
                    Text(
                        text = day,
                        fontSize = 11.sp,
                        fontWeight = if (index == currentDay) FontWeight.Bold else FontWeight.Normal,
                        color = if (index == currentDay) Color.White else Color(0xFF9E9E9E)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF3D5AFE), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Today",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF9E9E9E), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Previous Days",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

@Composable
fun ProductivityPieChart(
    title: String,
    time: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pie chart
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasSize = size.minDimension
                    val strokeWidth = 20f
                    
                    // Background circle
                    drawArc(
                        color = Color(0xFFF0F0F0),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    
                    // Parse time to get percentage (assuming 8 hours max per day)
                    val parts = time.split(" ")
                    var hours = 0f
                    parts.forEach { part ->
                        if (part.endsWith("h")) hours += part.dropLast(1).toFloatOrNull() ?: 0f
                        if (part.endsWith("m")) hours += (part.dropLast(1).toFloatOrNull() ?: 0f) / 60f
                    }
                    val percentage = (hours / 8f).coerceIn(0f, 1f)
                    val sweepAngle = percentage * 360f
                    
                    // Colored arc
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                
                // Center percentage
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val parts = time.split(" ")
                    var hours = 0f
                    parts.forEach { part ->
                        if (part.endsWith("h")) hours += part.dropLast(1).toFloatOrNull() ?: 0f
                        if (part.endsWith("m")) hours += (part.dropLast(1).toFloatOrNull() ?: 0f) / 60f
                    }
                    val percentage = ((hours / 8f) * 100).coerceIn(0f, 100f).toInt()
                    
                    Text(
                        text = "$percentage%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Time
            Text(
                text = time,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: String,
    title: String,
    subtitle: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier.size(40.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.White
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}
