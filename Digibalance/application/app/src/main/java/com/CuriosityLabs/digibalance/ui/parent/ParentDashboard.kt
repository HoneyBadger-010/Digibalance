package com.CuriosityLabs.digibalance.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CuriosityLabs.digibalance.data.repository.*
import com.CuriosityLabs.digibalance.util.UsageStatsHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    onShowQRCode: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val dashboardRepository = remember { ParentDashboardRepository() }
    
    var linkedStudents by remember { mutableStateOf<List<LinkedStudent>>(emptyList()) }
    var selectedStudent by remember { mutableStateOf<LinkedStudent?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var linkCode by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        scope.launch {
            val parentId = userRepository.getCurrentUserId() ?: return@launch
            dashboardRepository.getLinkedStudents(parentId).onSuccess { students ->
                linkedStudents = students
                if (students.isNotEmpty() && selectedStudent == null) {
                    selectedStudent = students.first()
                }
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showLinkDialog = true }) {
                        Icon(Icons.Default.Add, "Link Student")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (linkedStudents.isEmpty()) {
            EmptyStudentState(
                onLinkStudent = { showLinkDialog = true },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                StudentSelector(
                    students = linkedStudents,
                    selectedStudent = selectedStudent,
                    onStudentSelected = { selectedStudent = it }
                )
                
                selectedStudent?.let { student ->
                    StudentDashboardContent(
                        student = student,
                        dashboardRepository = dashboardRepository,
                        onShowQRCode = onShowQRCode
                    )
                }
            }
        }
    }
    
    if (showLinkDialog) {
        LinkStudentDialog(
            linkCode = linkCode,
            onGenerateCode = {
                scope.launch {
                    val parentId = userRepository.getCurrentUserId() ?: return@launch
                    dashboardRepository.generateLinkCode(parentId).onSuccess { code ->
                        linkCode = code
                    }
                }
            },
            onDismiss = {
                showLinkDialog = false
                linkCode = ""
            }
        )
    }
}


@Composable
fun StudentSelector(
    students: List<LinkedStudent>,
    selectedStudent: LinkedStudent?,
    onStudentSelected: (LinkedStudent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Student",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            students.forEach { student ->
                StudentChip(
                    student = student,
                    isSelected = student.id == selectedStudent?.id,
                    onClick = { onStudentSelected(student) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun StudentChip(
    student: LinkedStudent,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF2196F3) else Color(0xFF9E9E9E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (student.display_name?.firstOrNull() ?: "S").toString().uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.display_name ?: "Student",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )
                student.gamertag?.let {
                    Text(
                        text = "@$it",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun StudentDashboardContent(
    student: LinkedStudent,
    dashboardRepository: ParentDashboardRepository,
    onShowQRCode: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var todayStats by remember { mutableStateOf<StudentUsageStats?>(null) }
    var weeklyStats by remember { mutableStateOf<List<StudentUsageStats>>(emptyList()) }
    var topApps by remember { mutableStateOf<List<StudentAppUsage>>(emptyList()) }
    var ruleCompliance by remember { mutableStateOf<List<RuleCompliance>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(student.id) {
        scope.launch {
            isLoading = true
            
            launch {
                dashboardRepository.getStudentTodayStats(student.id).onSuccess {
                    todayStats = it
                }
            }
            launch {
                dashboardRepository.getStudentWeeklyStats(student.id).onSuccess {
                    weeklyStats = it
                }
            }
            launch {
                dashboardRepository.getStudentTopApps(student.id).onSuccess {
                    topApps = it
                }
            }
            launch {
                dashboardRepository.getStudentRuleCompliance(student.id).onSuccess {
                    ruleCompliance = it
                }
            }
            
            isLoading = false
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            item {
                TodaySummaryCard(todayStats)
            }
            
            item {
                WeeklyTrendCard(weeklyStats)
            }
            
            if (topApps.isNotEmpty()) {
                item {
                    TopAppsCard(topApps)
                }
            }
            
            if (ruleCompliance.isNotEmpty()) {
                item {
                    RuleComplianceCard(ruleCompliance)
                }
            }
            
            // Screenshot Request Card
            item {
                ScreenshotRequestCard(
                    studentId = student.id,
                    studentName = student.display_name ?: "Student"
                )
            }
            
            item {
                QuickActionsCard(student, dashboardRepository, onShowQRCode)
            }
        }
    }
}


@Composable
fun TodaySummaryCard(stats: StudentUsageStats?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Today's Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Screen Time",
                    value = UsageStatsHelper.formatDuration(stats?.total_screen_time_ms ?: 0),
                    color = Color(0xFF2196F3)
                )
                SummaryItem(
                    label = "Productive",
                    value = UsageStatsHelper.formatDuration(stats?.productive_time_ms ?: 0),
                    color = Color(0xFF4CAF50)
                )
                SummaryItem(
                    label = "Distraction",
                    value = UsageStatsHelper.formatDuration(stats?.distraction_time_ms ?: 0),
                    color = Color(0xFFFF5722)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Apps Opened",
                    value = "${stats?.apps_opened_count ?: 0}",
                    color = Color(0xFF9C27B0)
                )
                SummaryItem(
                    label = "Focus Sessions",
                    value = "${stats?.focus_sessions_count ?: 0}",
                    color = Color(0xFFFF9800)
                )
                SummaryItem(
                    label = "Alerts",
                    value = "${stats?.alerts_triggered_count ?: 0}",
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF757575)
        )
    }
}

@Composable
fun WeeklyTrendCard(weeklyStats: List<StudentUsageStats>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Weekly Trend",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (weeklyStats.isEmpty()) {
                Text(
                    text = "No data available for this week",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575)
                )
            } else {
                val avgScreenTime = weeklyStats.map { it.total_screen_time_ms }.average().toLong()
                val avgProductive = weeklyStats.map { it.productive_time_ms }.average().toLong()
                val avgDistraction = weeklyStats.map { it.distraction_time_ms }.average().toLong()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TrendItem(
                        label = "Avg Screen Time",
                        value = UsageStatsHelper.formatDuration(avgScreenTime),
                        color = Color(0xFF2196F3)
                    )
                    TrendItem(
                        label = "Avg Productive",
                        value = UsageStatsHelper.formatDuration(avgProductive),
                        color = Color(0xFF4CAF50)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Total days tracked: ${weeklyStats.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
fun TrendItem(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF757575)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}


@Composable
fun TopAppsCard(topApps: List<StudentAppUsage>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Top Apps Today",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            topApps.forEach { app ->
                AppUsageRow(app)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AppUsageRow(app: StudentAppUsage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (app.is_productive) Color(0xFF4CAF50) else Color(0xFFFF5722))
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.app_name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = if (app.is_productive) "Productive" else "Distraction",
                style = MaterialTheme.typography.bodySmall,
                color = if (app.is_productive) Color(0xFF4CAF50) else Color(0xFFFF5722)
            )
        }
        
        Text(
            text = UsageStatsHelper.formatDuration(app.usage_time_ms),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3)
        )
    }
}

@Composable
fun RuleComplianceCard(compliance: List<RuleCompliance>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Rule,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rule Compliance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            compliance.forEach { rule ->
                ComplianceRow(rule)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ComplianceRow(rule: RuleCompliance) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = rule.rule_type.replace("_", " "),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
            
            Row {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Followed",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = " ${rule.followed_count}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Violated",
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = " ${rule.violated_count}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFF44336)
                )
            }
        }
        
        rule.most_violated_app?.let {
            Text(
                text = "Most violated: $it",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
fun QuickActionsCard(
    student: LinkedStudent,
    dashboardRepository: ParentDashboardRepository,
    onShowQRCode: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var showUnlinkDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* TODO: Navigate to rules */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Icon(Icons.Default.Settings, "Settings")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage Rules")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onShowQRCode,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.QrCode, "QR Code")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Show QR Code to Link")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = { showUnlinkDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336))
            ) {
                Icon(Icons.Default.LinkOff, "Unlink")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unlink Student")
            }
        }
    }
    
    if (showUnlinkDialog) {
        AlertDialog(
            onDismissRequest = { showUnlinkDialog = false },
            title = { Text("Unlink Student") },
            text = {
                Text("Are you sure you want to unlink ${student.display_name}? All parental controls will be removed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val userRepository = UserRepository()
                            val parentId = userRepository.getCurrentUserId() ?: return@launch
                            dashboardRepository.unlinkStudent(parentId, student.id)
                            showUnlinkDialog = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF44336))
                ) {
                    Text("Unlink")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun EmptyStudentState(
    onLinkStudent: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FamilyRestroom,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = Color(0xFFBDBDBD)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "No Students Linked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Link your child's device to start monitoring their digital wellness",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF757575),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onLinkStudent,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(Icons.Default.Add, "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Link Student Device")
            }
        }
    }
}

@Composable
fun LinkStudentDialog(
    linkCode: String,
    onGenerateCode: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Link Student Device") },
        text = {
            Column {
                Text("Generate a link code and enter it on your child's device:")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (linkCode.isEmpty()) {
                    Button(
                        onClick = onGenerateCode,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate Link Code")
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Link Code",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF757575)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = linkCode,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3),
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Valid for 5 minutes",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "On your child's device:\n1. Open DigiBalance\n2. Go to Settings > Link to Parent\n3. Enter this code",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF424242)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
@Composable
fun ScreenshotRequestCard(
    studentId: String,
    studentName: String
) {
    var showScreenshotScreen by remember { mutableStateOf(false) }
    
    // Since this card only appears for LinkedStudents (already filtered by parent dashboard),
    // we can show it directly. The screenshot service will handle connection status.
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Screenshot,
                    contentDescription = "Screenshot",
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Screenshot Monitoring",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Request and view screenshots from $studentName's device to monitor their activity.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { showScreenshotScreen = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Request Screenshot"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Request Screenshot")
            }
        }
    }
    
    // Show screenshot screen when button is clicked
    if (showScreenshotScreen) {
        ScreenshotRequestScreen(
            studentId = studentId,
            studentName = studentName,
            onBack = { showScreenshotScreen = false }
        )
    }
}