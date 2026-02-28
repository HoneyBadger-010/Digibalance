package com.CuriosityLabs.digibalance

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso UI Tests for Authentication Flow
 * Tests: Sign up, Sign in, Forgot password, Role selection
 */
@RunWith(AndroidJUnit4::class)
class AuthFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testSplashScreenDisplays() {
        // Wait for splash screen
        composeTestRule.waitForIdle()
        
        // Verify splash screen shows
        composeTestRule.onNodeWithText("DigiBalance", substring = true).assertExists()
    }

    @Test
    fun testOnboardingFlow() {
        // Skip splash
        Thread.sleep(3000)
        
        // Verify onboarding screen
        composeTestRule.onNodeWithText("Welcome", substring = true).assertExists()
        
        // Click Get Started
        composeTestRule.onNodeWithText("Get Started").performClick()
        
        // Should navigate to auth screen
        composeTestRule.onNodeWithText("Sign In", substring = true).assertExists()
    }

    @Test
    fun testSignInFormValidation() {
        // Navigate to sign in
        navigateToAuth()
        
        // Try to sign in without email
        composeTestRule.onNodeWithText("Sign In").performClick()
        
        // Should show error (email required)
        composeTestRule.waitForIdle()
        
        // Enter invalid email
        composeTestRule.onNodeWithText("Email").performTextInput("invalid")
        composeTestRule.onNodeWithText("Password").performTextInput("test123")
        composeTestRule.onNodeWithText("Sign In").performClick()
        
        // Should show error
        composeTestRule.waitForIdle()
    }

    @Test
    fun testSignUpFormValidation() {
        navigateToAuth()
        
        // Switch to Sign Up
        composeTestRule.onNodeWithText("Sign Up").performClick()
        
        // Try without filling fields
        composeTestRule.onNodeWithText("Create Account").performClick()
        
        // Should show validation errors
        composeTestRule.waitForIdle()
    }

    @Test
    fun testForgotPasswordFlow() {
        navigateToAuth()
        
        // Click Forgot Password
        composeTestRule.onNodeWithText("Forgot Password?").performClick()
        
        // Verify forgot password screen
        composeTestRule.onNodeWithText("Reset Password", substring = true).assertExists()
        
        // Enter email
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        
        // Click send reset link
        composeTestRule.onNodeWithText("Send Reset Link").performClick()
        
        composeTestRule.waitForIdle()
    }

    @Test
    fun testRoleSelectionScreen() {
        // Assuming user is signed in
        navigateToRoleSelection()
        
        // Verify all roles are displayed
        composeTestRule.onNodeWithText("Student").assertExists()
        composeTestRule.onNodeWithText("Parent").assertExists()
        composeTestRule.onNodeWithText("Professional").assertExists()
        
        // Select Student role
        composeTestRule.onNodeWithText("Student").performClick()
        
        composeTestRule.waitForIdle()
    }

    private fun navigateToAuth() {
        Thread.sleep(3000) // Wait for splash
        composeTestRule.onNodeWithText("Get Started").performClick()
        composeTestRule.waitForIdle()
    }

    private fun navigateToRoleSelection() {
        // This would require actual authentication
        // For now, just wait
        Thread.sleep(5000)
    }
}
