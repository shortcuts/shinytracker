package com.shinytracker.feature.checklist.impl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.core.model.ChecklistEntry
import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.model.Generation
import org.junit.Rule
import org.junit.Test

/** Seeded-data render + search-filter check, per docs/testing.md's checklist UI test spec. */
class ChecklistScaffoldUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val entries =
        listOf(
            ChecklistEntry(DexEntry(1, 0, 0, "Bulbasaur"), caught = true, caughtAt = 1000L, generation = Generation.KANTO),
            ChecklistEntry(DexEntry(4, 0, 0, "Charmander"), caught = false, caughtAt = null, generation = Generation.KANTO),
        )

    @Test
    fun rendersSeededEntriesAndFiltersBySearch() {
        composeTestRule.setContent {
            var searchText by remember { mutableStateOf("") }
            ShinyTheme {
                ChecklistScaffold(
                    title = "Shiny Checklist",
                    searchText = searchText,
                    onSearchChange = { searchText = it },
                    caughtCount = 1,
                    totalCount = 2,
                    entries = entries.filter { it.dexEntry.name.contains(searchText, ignoreCase = true) },
                    isLoading = false,
                    errorMessage = null,
                    banner = null,
                    actions = {},
                    filterChips = null,
                )
            }
        }

        composeTestRule.onNodeWithText("BU").assertExists()
        composeTestRule.onNodeWithText("CH").assertExists()

        composeTestRule.onNodeWithText("Search").performClick()
        composeTestRule.onNodeWithText("Search species").performTextInput("Bulba")

        composeTestRule.onNodeWithText("BU").assertExists()
        composeTestRule.onNodeWithText("CH").assertDoesNotExist()
    }
}
