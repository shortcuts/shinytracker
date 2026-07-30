package com.shinytracker.app.smoke

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shinytracker.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** First real content for `make smoke-test`: launches the app, opens the checklist, asserts it renders. */
@RunWith(AndroidJUnit4::class)
class ChecklistSmokeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigatingToChecklistRendersTheScreen() {
        composeTestRule.onNodeWithText("View checklist").performClick()
        composeTestRule.onNodeWithText("Shiny Checklist").assertExists()
    }
}
