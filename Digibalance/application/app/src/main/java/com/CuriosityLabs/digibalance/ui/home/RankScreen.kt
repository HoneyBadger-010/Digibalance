package com.CuriosityLabs.digibalance.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CuriosityLabs.digibalance.R
import com.CuriosityLabs.digibalance.data.repository.LeaderboardEntry
import com.CuriosityLabs.digibalance.data.repository.LeaderboardRepository
import kotlinx.coroutines.launch

@Composable
fun RankScreen(userRole: UserRole) {
    // Hardcoded leaderboard data for showcase
    val leaderboard = remember {
        listOf(
            LeaderboardEntry(
                user_id = "1",
                gamertag = "FocusKing",
                productive_hours_weekly = 42.5,
                focus_sessions_weekly = 28,
                distraction_alerts_weekly = 3
            ),
            LeaderboardEntry(
                user_id = "2",
                gamertag = "StudyMaster",
                productive_hours_weekly = 38.2,
                focus_sessions_weekly = 25,
                distraction_alerts_weekly = 5
            ),
            LeaderboardEntry(
                user_id = "3",
                gamertag = "CodeNinja",
                productive_hours_weekly = 35.8,
                focus_sessions_weekly = 22,
                distraction_alerts_weekly = 7
            ),
            LeaderboardEntry(
                user_id = "4",
                gamertag = "TaskCrusher",
                productive_hours_weekly = 32.4,
                focus_sessions_weekly = 20,
                distraction_alerts_weekly = 8
            ),
            LeaderboardEntry(
                user_id = "5",
                gamertag = "WorkWarrior",
                productive_hours_weekly = 29.7,
                focus_sessions_weekly = 18,
                distraction_alerts_weekly = 10
            ),
            LeaderboardEntry(
                user_id = "6",
                gamertag = "FlowState",
                productive_hours_weekly = 27.3,
                focus_sessions_weekly = 16,
                distraction_alerts_weekly = 12
            ),
            LeaderboardEntry(
                user_id = "7",
                gamertag = "DeepFocus",
                productive_hours_weekly = 25.1,
                focus_sessions_weekly = 15,
                distraction_alerts_weekly = 14
            ),
            LeaderboardEntry(
                user_id = "8",
                gamertag = "ZenMode",
                productive_hours_weekly = 22.8,
                focus_sessions_weekly = 13,
                distraction_alerts_weekly = 16
            ),
            LeaderboardEntry(
                user_id = "9",
                gamertag = "Achiever",
                productive_hours_weekly = 20.5,
                focus_sessions_weekly = 12,
                distraction_alerts_weekly = 18
            ),
            LeaderboardEntry(
                user_id = "10",
                gamertag = "Hustler",
                productive_hours_weekly = 18.2,
                focus_sessions_weekly = 10,
                distraction_alerts_weekly = 20
            )
        )
    }
    val isLoading = false
    val errorMessage: String? = null
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC), // Clean light
                        Color(0xFFFFFFFF), // Pure white
                        Color(0xFFF1F5F9)  // Subtle blue
                    )
                )
            )
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Leaderboard",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Top performers this week",
                    fontSize = 15.sp,
                    color = Color(0xFF757575)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.leaderboard),
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Leaderboard Not Available",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The leaderboard table needs to be set up in your database.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { /* Retry disabled for hardcoded data */ }
                        ) {
                            Text("Retry")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Run FIX_LEADERBOARD.sql in Supabase",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2196F3),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            leaderboard.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No leaderboard data yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFF9C4).copy(alpha = 0.3f),
                                    Color(0xFFFFFFFF),
                                    Color(0xFFFFF3E0).copy(alpha = 0.2f)
                                )
                            )
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(leaderboard) { index, entry ->
                        LeaderboardCard(
                            rank = index + 1,
                            entry = entry
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardCard(
    rank: Int,
    entry: LeaderboardEntry
) {
    val isTopThree = rank <= 3
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color(0xFF3D5AFE) // Blue
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTopThree) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank badge
                Box(
                    modifier = Modifier
                        .size(if (isTopThree) 52.dp else 48.dp)
                        .clip(CircleShape)
                        .background(rankColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$rank",
                        fontSize = if (isTopThree) 20.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = rankColor
                    )
                }
                
                // User info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.gamertag,
                        fontSize = if (isTopThree) 18.sp else 16.sp,
                        fontWeight = if (isTopThree) FontWeight.Bold else FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%.1f", entry.productive_hours_weekly)} hours productive",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
            
            // Medal icon for top 3
            if (isTopThree) {
                val medalIcon = when (rank) {
                    1 -> R.drawable.medal_first
                    2 -> R.drawable.medal_second
                    3 -> R.drawable.medal_third
                    else -> null
                }
                
                if (medalIcon != null) {
                    Image(
                        painter = painterResource(id = medalIcon),
                        contentDescription = "Medal $rank",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}
