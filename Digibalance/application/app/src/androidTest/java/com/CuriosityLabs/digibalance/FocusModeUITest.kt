package com.CuriosityLabs.digibalance

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso UI Tests for Focus Mode
 * Tests: Start focus, timer display, app selection, emergency exit
 */
@RunWith(AndroidJUnit4::class)
class FocusModeUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testFocusModeTabExists() {
        // Navigate to home (assuming logged in)
        Thread.sleep(5000)
        
        // Click Focus tab
        composeTestRule.onNodeWithContentDescription("Focus").performClick()
        
        // Verify Focus screen loaded
        composeTestRule.onNodeWithText("Focus Mode", substring = true).assertExists()
    }

    @Test
    fun testAppSelectionDialog() {
        navigateToFocusMode()
        
        // Click to select apps
        composeTestRule.onNodeWithText("Select Apps").performClick()
        
        // Verify app selection dialog
        composeTestRule.onNodeWithText("Choose Apps", substring = true).assertExists()
        
        // Should show list of apps
        composeTestRule.waitForIdle()
    }

    @Test
    fun testStartFocusMode() {
        navigateToFocusMode()
        
        // Set duration
        composeTestRule.onNodeWithText("60 min").performClick()
        
        // Start focus mode
        composeTestRule.onNodeWithText("Start Focus").performClick()
        
        // Should show confirmation or start immediately
        composeTestRule.waitForIdle()
    }

    @Test
    fun testTimerDisplay() {
        navigateToFocusMode()
        
        // Start focus
        composeTestRule.onNodeWithText("Start Focus").performClick()
        
        // Wait a bit
        Thread.sleep(2000)
        
        // Timer should be counting down
        // Check for time format (MM:SS)
        composeTestRule.onNodeWithText(":", substring = true).assertExists()
    }

    @Test
    fun testEmergencyExitButton() {
        navigateToFocusMode()
        
        // Start focus
        composeTestRule.onNodeWithText("Start Focus").performClick()
        
        Thread.sleep(1000)
        
        // Look for emergency exit
        composeTestRule.onNodeWithText("Emergency Exit", substring = true).performClick()
        
        // Should show PIN dialog
        composeTestRule.onNodeWithText("Enter PIN", substring = true).assertExists()
    }

    @Test
    fun testFocusModeSettings() {
        navigateToFocusMode()
        
        // Open settings
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        
        // Verify settings screen
        composeTestRule.onNodeWithText("Focus Mode Settings", substring = true).assertExists()
    }

    private fun navigateToFocusMode() {
        Thread.sleep(5000) // Wait for app to load
        composeTestRule.onNodeWithContentDescription("Focus").performClick()
        composeTestRule.waitForIdle()
    }
}
