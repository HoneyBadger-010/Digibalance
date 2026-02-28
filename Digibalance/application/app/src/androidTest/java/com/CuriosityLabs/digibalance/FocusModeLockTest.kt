package com.CuriosityLabs.digibalance

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Automator Tests for Focus Mode Lock
 * Tests escape attempts: Home button, Recent apps, Status bar, Notifications
 * This is the "ransomware lock" validation
 */
@RunWith(AndroidJUnit4::class)
class FocusModeLockTest {

    private lateinit var device: UiDevice
    private lateinit var context: Context
    private val PACKAGE_NAME = "com.CuriosityLabs.digibalance"
    private val TIMEOUT = 5000L

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = ApplicationProvider.getApplicationContext()
        
        // Launch app
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        
        device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
    }

    @Test
    fun testHomeButtonBlocked() {
        // Start focus mode first (manual step or automated)
        startFocusMode()
        
        // Try to press home button
        device.pressHome()
        
        // Wait a bit
        Thread.sleep(1000)
        
        // Verify we're still in DigiBalance
        val currentPackage = device.currentPackageName
        assertEquals("Home button should be blocked", PACKAGE_NAME, currentPackage)
    }

    @Test
    fun testRecentAppsBlocked() {
        startFocusMode()
        
        // Try to open recent apps
        device.pressRecentApps()
        
        Thread.sleep(1000)
        
        // Should still be in DigiBalance
        val currentPackage = device.currentPackageName
        assertEquals("Recent apps should be blocked", PACKAGE_NAME, currentPackage)
    }

    @Test
    fun testBackButtonBlocked() {
        startFocusMode()
        
        // Try back button
        device.pressBack()
        
        Thread.sleep(500)
        
        // Should still be in focus mode
        val currentPackage = device.currentPackageName
        assertEquals("Back button should be blocked", PACKAGE_NAME, currentPackage)
    }

    @Test
    fun testStatusBarPullBlocked() {
        startFocusMode()
        
        // Try to pull down status bar
        device.swipe(
            device.displayWidth / 2,
            0,
            device.displayWidth / 2,
            device.displayHeight / 2,
            10
        )
        
        Thread.sleep(1000)
        
        // Should not show notification shade
        val notificationShade = device.findObject(By.pkg("com.android.systemui"))
        assertNull("Status bar should be blocked", notificationShade)
    }

    @Test
    fun testNotificationAccessBlocked() {
        startFocusMode()
        
        // Try to expand notifications
        device.openNotification()
        
        Thread.sleep(1000)
        
        // Should still be in DigiBalance
        val currentPackage = device.currentPackageName
        assertEquals("Notifications should be blocked", PACKAGE_NAME, currentPackage)
    }

    @Test
    fun testQuickSettingsBlocked() {
        startFocusMode()
        
        // Try to open quick settings (double swipe down)
        device.swipe(
            device.displayWidth / 2,
            0,
            device.displayWidth / 2,
            device.displayHeight,
            5
        )
        
        Thread.sleep(500)
        
        device.swipe(
            device.displayWidth / 2,
            0,
            device.displayWidth / 2,
            device.displayHeight,
            5
        )
        
        Thread.sleep(1000)
        
        // Should still be in DigiBalance
        val currentPackage = device.currentPackageName
        assertEquals("Quick settings should be blocked", PACKAGE_NAME, currentPackage)
    }

    @Test
    fun testAppSwitchingBlocked() {
        startFocusMode()
        
        // Try to launch another app via intent
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        context.startActivity(launchIntent)
        
        Thread.sleep(1000)
        
        // Should redirect back to DigiBalance
        val currentPackage = device.currentPackageName
        assertEquals("App switching should be blocked", PACKAGE_NAME, currentPackage)
    }

    @Test
    fun testPowerButtonBehavior() {
        startFocusMode()
        
        // Press power button (screen off)
        device.sleep()
        
        Thread.sleep(1000)
        
        // Wake up
        device.wakeUp()
        
        Thread.sleep(1000)
        
        // Should still be in focus mode
        val currentPackage = device.currentPackageName
        assertEquals("Should return to focus mode after wake", PACKAGE_NAME, currentPackage)
    }

    @Test
    fun testVolumeButtonsWork() {
        startFocusMode()
        
        // Volume buttons should still work
        device.pressKeyCode(android.view.KeyEvent.KEYCODE_VOLUME_UP)
        
        Thread.sleep(500)
        
        // Should still be in DigiBalance
        val currentPackage = device.currentPackageName
        assertEquals("Volume buttons should work but not exit", PACKAGE_NAME, currentPackage)
    }

    @Test
    fun testScreenRotation() {
        startFocusMode()
        
        // Rotate screen
        device.setOrientationLeft()
        
        Thread.sleep(1000)
        
        // Should still be in focus mode
        val currentPackage = device.currentPackageName
        assertEquals("Rotation should not exit focus mode", PACKAGE_NAME, currentPackage)
        
        // Rotate back
        device.setOrientationNatural()
    }

    @Test
    fun testMultipleEscapeAttempts() {
        startFocusMode()
        
        // Try multiple escape methods rapidly
        repeat(5) {
            device.pressHome()
            Thread.sleep(200)
            device.pressRecentApps()
            Thread.sleep(200)
            device.pressBack()
            Thread.sleep(200)
        }
        
        // Should still be locked
        val currentPackage = device.currentPackageName
        assertEquals("Multiple escape attempts should fail", PACKAGE_NAME, currentPackage)
    }

    @Test
    fun testEmergencyExitWorks() {
        startFocusMode()
        
        // Find and click emergency exit button
        val emergencyButton = device.findObject(
            By.text("Emergency Exit").clickable(true)
        )
        
        if (emergencyButton != null) {
            emergencyButton.click()
            
            Thread.sleep(1000)
            
            // Should show PIN dialog
            val pinDialog = device.findObject(By.text("Enter PIN"))
            assertNotNull("PIN dialog should appear", pinDialog)
            
            // Enter correct PIN (if known)
            // For testing, you'd need to know the PIN or use a test PIN
        }
    }

    private fun startFocusMode() {
        // Navigate to Focus tab
        val focusTab = device.findObject(By.desc("Focus"))
        focusTab?.click()
        
        Thread.sleep(1000)
        
        // Click Start Focus button
        val startButton = device.findObject(By.text("Start Focus"))
        startButton?.click()
        
        // Wait for focus mode to activate
        Thread.sleep(2000)
    }
}
