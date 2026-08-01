package com.shinytracker.app

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.core.model.DisplayLanguage
import org.junit.Rule
import org.junit.Test

/** Callback-wiring check per docs/testing.md's UI test spec -- which tap fires which callback. */
class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tappingLanguageRadioButtonInvokesOnLanguageChange() {
        var changedTo: DisplayLanguage? = null
        composeTestRule.setContent {
            ShinyTheme {
                SettingsScreen(
                    displayLanguage = DisplayLanguage.ENGLISH,
                    scannerEnabled = false,
                    onLanguageChange = { changedTo = it },
                    onScannerEnabledChange = {},
                    syncState = SettingsSyncState.IDLE,
                    onSyncClick = {},
                    onOpenDrawer = {},
                )
            }
        }

        // DisplayLanguage.entries order: ENGLISH, JAPANESE, CHINESE, FRENCH -- index 1 is Japanese's radio button.
        composeTestRule.onAllNodes(isSelectable())[1].performClick()

        assert(changedTo == DisplayLanguage.JAPANESE) { "expected JAPANESE, got $changedTo" }
    }

    @Test
    fun tappingScannerCheckboxInvokesOnScannerEnabledChangeWithFlippedValue() {
        var toggledTo: Boolean? = null
        composeTestRule.setContent {
            ShinyTheme {
                SettingsScreen(
                    displayLanguage = DisplayLanguage.ENGLISH,
                    scannerEnabled = false,
                    onLanguageChange = {},
                    onScannerEnabledChange = { toggledTo = it },
                    syncState = SettingsSyncState.IDLE,
                    onSyncClick = {},
                    onOpenDrawer = {},
                )
            }
        }

        composeTestRule.onNode(isToggleable()).performClick()

        assert(toggledTo == true) { "expected true, got $toggledTo" }
    }

    @Test
    fun tappingSyncButtonInvokesOnSyncClick() {
        var clicked = false
        composeTestRule.setContent {
            ShinyTheme {
                SettingsScreen(
                    displayLanguage = DisplayLanguage.ENGLISH,
                    scannerEnabled = false,
                    onLanguageChange = {},
                    onScannerEnabledChange = {},
                    syncState = SettingsSyncState.IDLE,
                    onSyncClick = { clicked = true },
                    onOpenDrawer = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Sync checklist").performClick()

        assert(clicked) { "expected onSyncClick to fire" }
    }

    @Test
    fun syncButtonIsDisabledWhileSyncing() {
        composeTestRule.setContent {
            ShinyTheme {
                SettingsScreen(
                    displayLanguage = DisplayLanguage.ENGLISH,
                    scannerEnabled = false,
                    onLanguageChange = {},
                    onScannerEnabledChange = {},
                    syncState = SettingsSyncState.SYNCING,
                    onSyncClick = {},
                    onOpenDrawer = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Sync checklist").assertIsNotEnabled()
    }
}
