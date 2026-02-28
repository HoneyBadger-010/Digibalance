package com.CuriosityLabs.digibalance.data

data class AwarenessVideo(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val thumbnailUrl: String,
    val videoUrl: String
)

object AwarenessVideos {
    val videos = listOf(
        AwarenessVideo(
            id = "JJq0EBgeA54",
            title = "Why Maintain Screen Time",
            description = "Understanding the importance of managing your screen time for better health and productivity",
            category = "General",
            thumbnailUrl = "https://img.youtube.com/vi/JJq0EBgeA54/maxresdefault.jpg",
            videoUrl = "https://www.youtube.com/watch?v=JJq0EBgeA54"
        ),
        AwarenessVideo(
            id = "cEb7PdjTxxE",
            title = "Screen Time Management Tips",
            description = "Practical strategies and tips for maintaining healthy screen time habits",
            category = "Productivity",
            thumbnailUrl = "https://img.youtube.com/vi/cEb7PdjTxxE/maxresdefault.jpg",
            videoUrl = "https://www.youtube.com/watch?v=cEb7PdjTxxE"
        ),
        AwarenessVideo(
            id = "3U3Ews1loiE",
            title = "Digital Wellness Guide",
            description = "A comprehensive guide to digital wellness and mindful technology use",
            category = "Focus",
            thumbnailUrl = "https://img.youtube.com/vi/3U3Ews1loiE/maxresdefault.jpg",
            videoUrl = "https://www.youtube.com/watch?v=3U3Ews1loiE"
        )
    )
}
