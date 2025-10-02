package com.example.mywishlistapp

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Baseline Profile Generator for My WishList App
 * 
 * This generates a baseline profile that improves app startup time and performance
 * by pre-compiling critical code paths into ART (Android Runtime) optimized code.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() = rule.collect(
        packageName = "com.example.mywishlistapp.debug",
        profileBlock = {
            // Start the app and wait for it to be idle
            startActivityAndWait()
            
            // Wait for main screen content to be visible
            device.waitForIdle(3000)
            
            // Navigate through critical user journeys
            
            // 1. Dashboard interaction - scroll and view statistics
            device.swipe(
                device.displayWidth / 2,
                device.displayHeight * 3 / 4,
                device.displayWidth / 2,
                device.displayHeight / 4,
                20
            )
            device.waitForIdle(1000)
            
            // 2. Navigate to Add/Edit screen (most common user action)
            device.findObject(By.desc("Add Wish")).click()
            device.waitForIdle(2000)
            
            // 3. Fill form fields (common user interaction)
            device.findObject(By.text("Title")).click()
            device.waitForIdle(500)
            
            // 4. Navigate back to main screen
            device.pressBack()
            device.waitForIdle(1500)
            
            // 5. Navigate to wish list
            device.findObject(By.text("WISHLIST")).click()
            device.waitForIdle(1500)
            
            // 6. Scroll the wish list (performance critical)
            device.swipe(
                device.displayWidth / 2,
                device.displayHeight * 3 / 4,
                device.displayWidth / 2,
                device.displayHeight / 4,
                25
            )
            device.waitForIdle(1000)
            
            // 7. Open a wish item
            device.findObject(By.clickable(true)).click()
            device.waitForIdle(2000)
            
            // 8. Navigate back
            device.pressBack()
            device.waitForIdle(1000)
            
            // 9. Navigate to dashboard
            device.findObject(By.text("Dashboard")).click()
            device.waitForIdle(1500)
            
            // 10. Access settings (less frequent but important for theming)
            device.findObject(By.text("Settings")).click()
            device.waitForIdle(1500)
            
            // Return to home
            device.pressBack()
            device.waitForIdle(1000)
        }
    )
}
