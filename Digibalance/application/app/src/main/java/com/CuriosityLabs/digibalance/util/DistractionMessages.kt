package com.CuriosityLabs.digibalance.util

// TODO-20: Creative distraction warning messages
object DistractionMessages {
    
    private val warningMessages = listOf(
        "⏰ Time Check: You've been here for a while. Is this productive?",
        "🎯 Focus Alert: Remember your goals! Time to refocus?",
        "⚠️ Distraction Detected: This app is eating your time!",
        "🚨 Reality Check: ${getTimeWasted()} minutes could be better spent!",
        "💭 Think About It: What could you accomplish instead?",
        "🔔 Gentle Reminder: Your future self is counting on you!",
        "⏳ Time Flies: Make sure it's flying in the right direction!",
        "🎪 Distraction Zone: You're in the danger zone!",
        "🌟 You're Better Than This: Time to get back on track!",
        "💪 Break Free: Don't let this app control your time!"
    )
    
    private val alternativeSuggestions = listOf(
        "Try reading a book instead 📚",
        "How about a quick workout? 💪",
        "Time for a productive task? ✅",
        "Learn something new today! 🎓",
        "Connect with a friend in person 👥",
        "Work on your goals 🎯",
        "Practice a skill 🎨",
        "Go for a walk 🚶",
        "Meditate for clarity 🧘",
        "Plan your day ahead 📅"
    )
    
    fun getRandomWarning(): String {
        return warningMessages.random()
    }
    
    fun getAlternativeSuggestion(): String {
        return alternativeSuggestions.random()
    }
    
    fun getTimeWastedMessage(minutes: Int): String {
        return when {
            minutes < 10 -> "You've spent $minutes minutes here. Still manageable!"
            minutes < 30 -> "⚠️ $minutes minutes gone! Time to switch gears?"
            minutes < 60 -> "🚨 $minutes minutes! That's almost an hour of your life!"
            else -> "🔴 Over an hour wasted! Your goals are waiting!"
        }
    }
    
    private fun getTimeWasted(): Int {
        return (5..60).random() // Placeholder for actual time tracking
    }
    
    fun getMotivationalExit(): String {
        return listOf(
            "Great decision! Your productivity awaits! 🚀",
            "Smart choice! Time to be awesome! ⭐",
            "You've got this! Back to greatness! 💪",
            "Excellent! Your future self thanks you! 🙌",
            "Wise move! Let's make today count! 🎯"
        ).random()
    }
}
