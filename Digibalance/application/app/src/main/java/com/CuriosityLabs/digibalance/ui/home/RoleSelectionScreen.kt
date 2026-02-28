package com.CuriosityLabs.digibalance.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.CuriosityLabs.digibalance.R

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (UserRole) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFFFFFFF)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Welcome to DigiBalance",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Text(
                text = "Choose your role to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            RoleCard(
                title = "Parent",
                description = "Monitor and guide your family's digital wellness",
                iconRes = R.drawable.parent,
                gradientColors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)),
                onClick = { onRoleSelected(UserRole.PARENT) }
            )
            
            RoleCard(
                title = "Student",
                description = "Track and improve your digital balance",
                iconRes = R.drawable.student,
                gradientColors = listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7)),
                onClick = { onRoleSelected(UserRole.STUDENT) }
            )
            
            RoleCard(
                title = "Professional",
                description = "Access tools and insights for digital wellness",
                iconRes = R.drawable.professional,
                gradientColors = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
                onClick = { onRoleSelected(UserRole.PROFESSIONAL) }
            )
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    description: String,
    iconRes: Int,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.horizontalGradient(gradientColors))
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "$title icon",
                    modifier = Modifier.size(64.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF424242)
                    )
                }
            }
        }
    }
      
}

