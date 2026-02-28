package com.CuriosityLabs.digibalance.util

// TODO-19: Motivational quotes for productive apps
object MotivationalQuotes {
    
    private val quotes = listOf(
        "Great work! You're being productive! 🌟",
        "Keep up the excellent focus! 💪",
        "You're making great progress! 🚀",
        "Productivity looks good on you! ✨",
        "You're crushing it! Keep going! 🎯",
        "Focus mode: ACTIVATED! 🔥",
        "Your future self will thank you! 🙌",
        "Excellence in action! 👏",
        "You're on fire today! 🌟",
        "Making every minute count! ⏰",
        "Discipline equals freedom! 💎",
        "Small steps, big results! 📈",
        "You're building great habits! 🏆",
        "Consistency is key! 🔑",
        "Your dedication is inspiring! ⭐"
    )
    
    fun getRandomQuote(): String {
        return quotes.random()
    }
    
    fun getQuoteForDuration(minutes: Int): String {
        return when {
            minutes < 5 -> "Great start! Keep the momentum going! 🚀"
            minutes < 15 -> "15 minutes of focus! You're doing amazing! 💪"
            minutes < 30 -> "Half an hour of productivity! Impressive! 🌟"
            minutes < 60 -> "Almost an hour! Your dedication is remarkable! 🏆"
            else -> "Over an hour of focus! You're a productivity champion! 👑"
        }
    }
}
