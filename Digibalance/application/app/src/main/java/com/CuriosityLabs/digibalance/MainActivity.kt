package com.CuriosityLabs.digibalance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import com.CuriosityLabs.digibalance.ui.auth.AuthScreen
import com.CuriosityLabs.digibalance.ui.auth.SplashScreen
import com.CuriosityLabs.digibalance.ui.home.HomeScreen
import com.CuriosityLabs.digibalance.ui.home.RoleSelectionScreen
import com.CuriosityLabs.digibalance.ui.home.UserRole
import com.CuriosityLabs.digibalance.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // FORCE LIGHT MODE ALWAYS - NO DARK MODE
            MyApplicationTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DigiBalanceApp()
                }
            }
        }
    }
}

@Composable
fun DigiBalanceApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { com.CuriosityLabs.digibalance.data.PreferencesManager.getInstance(context) }
    
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var showOnboarding by rememberSaveable { mutableStateOf(false) }
    var showAuth by rememberSaveable { mutableStateOf(false) }
    var showSurvey by rememberSaveable { mutableStateOf(false) }
    var showPermissions by rememberSaveable { mutableStateOf(false) }
    var showRoleSelection by rememberSaveable { mutableStateOf(false) }
    var showGamertagScreen by rememberSaveable { mutableStateOf(false) }
    var isLoadingProfile by rememberSaveable { mutableStateOf(false) }
    var isCheckingSession by rememberSaveable { mutableStateOf(true) }
    var selectedRole by rememberSaveable { mutableStateOf<UserRole?>(null) }
    var userId by rememberSaveable { mutableStateOf<String?>(null) }
    var userGamertag by rememberSaveable { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // TODO-1 & TODO-3: Enhanced session persistence - check both Supabase AND local preferences + Room database
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            android.util.Log.d("MainActivity", "🔍 Checking for existing session...")
            
            // FIRST: Restore session from Room database (survives app clear from recents)
            prefs.restoreFromDatabase(context)
            android.util.Log.d("MainActivity", "✅ Session restored from Room database")
            
            val userRepository = com.CuriosityLabs.digibalance.data.repository.UserRepository()
            val sessionStateRepo = com.CuriosityLabs.digibalance.data.repository.SessionStateRepository()
            
            // Then check local preferences (now restored from database)
            val localUserId = prefs.currentUserId
            val localRole = prefs.currentUserRole
            val localGamertag = prefs.currentUserGamertag
            val onboardingComplete = prefs.onboardingCompleted
            
            // Then verify with Supabase session
            if (userRepository.isUserLoggedIn() || (localUserId != null && prefs.isLoggedIn)) {
                val currentUserId = userRepository.getCurrentUserId() ?: localUserId
                android.util.Log.d("MainActivity", "✅ Active session found! User ID: $currentUserId")
                
                if (currentUserId != null) {
                    // Try to get user profile
                    val result = userRepository.getUserProfile(currentUserId)
                    
                    launch(Dispatchers.Main) {
                        result.onSuccess { userProfile ->
                            android.util.Log.d("MainActivity", "✅ Profile loaded from session")
                            android.util.Log.d("MainActivity", "   - role: ${userProfile.role}")
                            android.util.Log.d("MainActivity", "   - gamertag: ${userProfile.gamertag}")
                            
                            // Save to preferences
                            prefs.isLoggedIn = true
                            prefs.currentUserId = currentUserId
                            prefs.currentUserRole = userProfile.role
                            prefs.currentUserGamertag = userProfile.gamertag
                            prefs.currentUserEmail = userProfile.email
                            prefs.currentUserPhone = userProfile.phone
                            
                            // IMMEDIATELY save to Room database for persistence
                            launch(Dispatchers.IO) {
                                prefs.saveToDatabase(context)
                                android.util.Log.d("MainActivity", "✅ Session saved to Room database")
                            }
                            
                            // Set app state
                            userId = currentUserId
                            userGamertag = userProfile.gamertag
                            
                            if (userProfile.role != null && userProfile.role.isNotBlank()) {
                                selectedRole = when(userProfile.role) {
                                    "Student" -> UserRole.STUDENT
                                    "Parent" -> UserRole.PARENT
                                    "Professional" -> UserRole.PROFESSIONAL
                                    else -> null
                                }
                            }
                            
                            // TODO-3: Check session state to avoid repeating onboarding
                            // Fetch session state from Supabase
                            val sessionResult = sessionStateRepo.getSessionState(currentUserId)
                            
                            sessionResult.onSuccess { sessionState ->
                                android.util.Log.d("MainActivity", "✅ Session state loaded")
                                android.util.Log.d("MainActivity", "   - onboarding_completed: ${sessionState.onboarding_completed}")
                                android.util.Log.d("MainActivity", "   - last_screen: ${sessionState.last_screen}")
                                
                                prefs.onboardingCompleted = sessionState.onboarding_completed
                                prefs.lastScreen = sessionState.last_screen
                            }.onFailure {
                                android.util.Log.w("MainActivity", "Session state not found, using defaults")
                            }
                            
                            // Skip splash and onboarding
                            showSplash = false
                            showOnboarding = false
                            isCheckingSession = false
                            
                            // TODO-3: Only show onboarding screens if NOT completed
                            val needsOnboarding = !prefs.onboardingCompleted
                            
                            if (needsOnboarding) {
                                // User hasn't completed onboarding - resume where they left off
                                if (selectedRole == null) {
                                    showRoleSelection = true
                                } else if (selectedRole != UserRole.PARENT && userProfile.gamertag == null) {
                                    showGamertagScreen = true
                                }
                            } else {
                                // Onboarding complete - go directly to home
                                android.util.Log.d("MainActivity", "✅ Onboarding complete, going to home")
                            }
                            
                        }.onFailure { error ->
                            android.util.Log.e("MainActivity", "❌ Failed to load profile: ${error.message}")
                            // Session exists but profile failed - show auth
                            launch(Dispatchers.Main) {
                                showSplash = false
                                showOnboarding = true
                                isCheckingSession = false
                            }
                        }
                    }
                } else {
                    launch(Dispatchers.Main) {
                        showSplash = false
                        showOnboarding = true
                        isCheckingSession = false
                    }
                }
            } else {
                android.util.Log.d("MainActivity", "❌ No active session found")
                // No session - show normal flow
                launch(Dispatchers.Main) {
                    isCheckingSession = false
                }
            }
        }
    }

    when {
        isCheckingSession -> {
            // Show loading while checking session
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        showSplash -> {
            SplashScreen(
                onNavigateToMain = {
                    showSplash = false
                    showOnboarding = true
                }
            )
        }
        showOnboarding -> {
            com.CuriosityLabs.digibalance.ui.onboarding.OnboardingScreen(
                onGetStarted = {
                    showOnboarding = false
                    showAuth = true
                }
            )
        }
        showAuth -> {
            AuthScreen(
                onSignInSuccess = { fetchedUserId ->
                    // Reset all states on sign-in
                    showAuth = false
                    showSurvey = false
                    showRoleSelection = false
                    showPermissions = false
                    showGamertagScreen = false
                    selectedRole = null
                    userGamertag = null
                    
                    userId = fetchedUserId
                    isLoadingProfile = true
                    
                    // Fetch user profile from Supabase database
                    val userRepository = com.CuriosityLabs.digibalance.data.repository.UserRepository()
                    scope.launch(Dispatchers.IO) {
                        android.util.Log.d("MainActivity", "Starting profile fetch for userId: $fetchedUserId")
                        val result = userRepository.getUserProfile(fetchedUserId)
                        launch(Dispatchers.Main) {
                            result.onSuccess { userProfile ->
                                android.util.Log.d("MainActivity", "✅ Profile fetched successfully!")
                                android.util.Log.d("MainActivity", "   - role: ${userProfile.role}")
                                android.util.Log.d("MainActivity", "   - gamertag: ${userProfile.gamertag}")
                                android.util.Log.d("MainActivity", "   - email: ${userProfile.email}")
                                android.util.Log.d("MainActivity", "   - phone: ${userProfile.phone}")
                                
                                // Save session to preferences
                                prefs.isLoggedIn = true
                                prefs.currentUserId = fetchedUserId
                                prefs.currentUserRole = userProfile.role
                                prefs.currentUserGamertag = userProfile.gamertag
                                prefs.currentUserEmail = userProfile.email
                                prefs.currentUserPhone = userProfile.phone
                                
                                // Check if role is null - if so, user needs to select role
                                if (userProfile.role == null || userProfile.role.isBlank()) {
                                    android.util.Log.d("MainActivity", "❌ Role is NULL or empty, showing role selection")
                                    isLoadingProfile = false
                                    showRoleSelection = true
                                    return@launch
                                }
                                
                                android.util.Log.d("MainActivity", "✅ Role exists: ${userProfile.role}")
                                
                                // Set role from database
                                selectedRole = when(userProfile.role) {
                                    "Student" -> UserRole.STUDENT
                                    "Parent" -> UserRole.PARENT
                                    "Professional" -> UserRole.PROFESSIONAL
                                    else -> {
                                        android.util.Log.w("MainActivity", "Unknown role: ${userProfile.role}")
                                        isLoadingProfile = false
                                        showRoleSelection = true
                                        return@launch
                                    }
                                }
                                userGamertag = userProfile.gamertag
                                
                                // Check if user has completed onboarding
                                // For Students/Professionals: must have gamertag
                                // For Parents: gamertag is optional
                                val hasCompletedOnboarding = when(userProfile.role) {
                                    "Parent" -> true // Parents don't need gamertag
                                    "Student", "Professional" -> userProfile.gamertag != null
                                    else -> false
                                }
                                
                                isLoadingProfile = false
                                
                                if (!hasCompletedOnboarding) {
                                    android.util.Log.d("MainActivity", "User hasn't completed onboarding, showing gamertag screen")
                                    showGamertagScreen = true
                                }
                                // Otherwise, user goes directly to home with their saved role
                            }.onFailure { error ->
                                // If profile doesn't exist or fetch failed
                                android.util.Log.e("MainActivity", "❌ Failed to fetch profile!")
                                android.util.Log.e("MainActivity", "   Error message: ${error.message}")
                                android.util.Log.e("MainActivity", "   Error type: ${error.javaClass.simpleName}")
                                android.util.Log.e("MainActivity", "   Full error: $error", error)
                                // Show role selection so user can continue
                                isLoadingProfile = false
                                showRoleSelection = true
                            }
                        }
                    }
                },
                onSignUpSuccess = { fetchedUserId ->
                    showAuth = false
                    userId = fetchedUserId
                    // New users: collect survey before choosing role
                    showSurvey = true
                },
                onBack = {
                    showAuth = false
                    showOnboarding = true
                }
            )
        }
        showSurvey -> {
            com.CuriosityLabs.digibalance.ui.survey.SurveyScreen(
                onSurveyComplete = { surveyData ->
                    // TODO: Save survey data to Supabase users table
                    showSurvey = false
                    showRoleSelection = true
                },
                onSkip = {
                    // Skip survey and go directly to role selection
                    showSurvey = false
                    showRoleSelection = true
                }
            )
        }
        showPermissions -> {
            com.CuriosityLabs.digibalance.ui.permissions.PermissionsScreen(
                onComplete = {
                    showPermissions = false
                    // After permissions, go to gamertag screen for students/professionals
                    showGamertagScreen = true
                }
            )
        }
        showRoleSelection -> {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    selectedRole = role
                    showRoleSelection = false
                    
                    // Save role to Supabase users table
                    userId?.let { uid ->
                        val userRepository = com.CuriosityLabs.digibalance.data.repository.UserRepository()
                        scope.launch(Dispatchers.IO) {
                            val roleString = when (role) {
                                UserRole.STUDENT -> "Student"
                                UserRole.PARENT -> "Parent"
                                UserRole.PROFESSIONAL -> "Professional"
                            }
                            userRepository.updateUserRole(uid, roleString)
                        }
                    }
                    
                    when (role) {
                        UserRole.PARENT -> {
                            // Parents bypass permissions, go straight to home
                            showPermissions = false
                        }
                        UserRole.STUDENT, UserRole.PROFESSIONAL -> {
                            // Students/Professionals: show permission slides first
                            showPermissions = true
                        }
                    }
                }
            )
        }
        showGamertagScreen -> {
            com.CuriosityLabs.digibalance.ui.auth.GamertagScreen(
                userId = userId ?: "",
                onGamertagCreated = { gamertag ->
                    userGamertag = gamertag
                    showGamertagScreen = false
                }
            )
        }
        isLoadingProfile -> {
            // Show loading screen while fetching profile
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        selectedRole != null && (selectedRole == UserRole.PARENT || userGamertag != null) -> {
            // Start screenshot capture service for students who are linked to parents
            LaunchedEffect(selectedRole) {
                if (selectedRole == UserRole.STUDENT) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val userRepository = com.CuriosityLabs.digibalance.data.repository.UserRepository()
                            val result = userRepository.getCurrentUserProfile()
                            result.onSuccess { user ->
                                if (user.linked_parent_id != null) {
                                    // Student is linked to parent - start screenshot service
                                    android.util.Log.d("MainActivity", "Starting screenshot capture service for linked student")
                                    val serviceIntent = android.content.Intent(context, com.CuriosityLabs.digibalance.service.ScreenshotCaptureService::class.java)
                                    context.startForegroundService(serviceIntent)
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Failed to start screenshot service", e)
                        }
                    }
                }
            }
            
            HomeScreen(
                userRole = selectedRole!!,
                onLogout = {
                    // Handle logout
                    scope.launch {
                        val userRepository = com.CuriosityLabs.digibalance.data.repository.UserRepository()
                        userRepository.signOut()
                        
                        // Clear preferences
                        prefs.clearSession()
                        
                        // Reset all states
                        selectedRole = null
                        userId = null
                        userGamertag = null
                        showAuth = true
                        showOnboarding = true
                    }
                }
            )
        }
        else -> {
            // Fallback - should never reach here
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Welcome to DigiBalance")
            }
        }
    }
}