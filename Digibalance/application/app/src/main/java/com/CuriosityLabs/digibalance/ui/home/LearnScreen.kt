package com.CuriosityLabs.digibalance.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.CuriosityLabs.digibalance.R

data class AwarenessVideo(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val youtubeVideoId: String
)

@Composable
fun LearnScreen(userRole: UserRole) {
    var videos by remember { mutableStateOf<List<AwarenessVideo>>(emptyList()) }
    var selectedVideo by remember { mutableStateOf<AwarenessVideo?>(null) }
    
    LaunchedEffect(Unit) {
        // TODO: Load videos from Supabase
        videos = listOf(
            AwarenessVideo(
                id = "1",
                title = "Digital Wellness Basics",
                description = "Learn the fundamentals of digital wellness",
                thumbnailUrl = "",
                youtubeVideoId = "dQw4w9WgXcQ"
            ),
            AwarenessVideo(
                id = "2",
                title = "Managing Screen Time",
                description = "Tips for healthy screen time habits",
                thumbnailUrl = "",
                youtubeVideoId = "dQw4w9WgXcQ"
            ),
            AwarenessVideo(
                id = "3",
                title = "Focus and Productivity",
                description = "Boost your productivity with these techniques",
                thumbnailUrl = "",
                youtubeVideoId = "dQw4w9WgXcQ"
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.awareness),
                contentDescription = "Learn",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Awareness Videos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
        
        if (videos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(videos) { video ->
                    VideoCard(
                        video = video,
                        onClick = { selectedVideo = video }
                    )
                }
            }
        }
    }
    
    // Video player dialog
    selectedVideo?.let { video ->
        AlertDialog(
            onDismissRequest = { selectedVideo = null },
            title = { Text(video.title) },
            text = {
                Column {
                    Text(video.description)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Video ID: ${video.youtubeVideoId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    // TODO: Integrate YouTube player
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedVideo = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun VideoCard(
    video: AwarenessVideo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Thumbnail placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.awareness),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
            }
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = video.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}
