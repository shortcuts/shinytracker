package com.shinytracker.app.smoke

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shinytracker.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * First real content for `make smoke-test`: launches the app. A fresh instrumentation run has
 * neither permission granted, so the onboarding gate is what should render -- granting
 * BIND_ACCESSIBILITY_SERVICE from an instrumentation test isn't supported (docs/testing.md).
 */
@RunWith(AndroidJUnit4::class)
class ChecklistSmokeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchingWithoutPermissionsShowsOnboardingGate() {
        composeTestRule.onNodeWithText("Set up shinytracker").assertExists()
    }
}
