package com.CuriosityLabs.digibalance.ui.awareness

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.CuriosityLabs.digibalance.data.AwarenessVideos

@Composable
fun AwarenessScreen() {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        item {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 2.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Learn", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Digital wellness tips and insights", fontSize = 15.sp, color = Color(0xFF757575))
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        items(AwarenessVideos.videos) { video ->
            VideoCard(video = video, onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
                context.startActivity(intent)
            })
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun VideoCard(video: com.CuriosityLabs.digibalance.data.AwarenessVideo, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                AsyncImage(model = video.thumbnailUrl, contentDescription = video.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.95f)), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color(0xFF3D5AFE), modifier = Modifier.size(32.dp))
                    }
                }
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF3D5AFE)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(text = video.category, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = video.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), lineHeight = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = video.description, fontSize = 14.sp, color = Color(0xFF757575), lineHeight = 20.sp)
            }
        }
    }
}
