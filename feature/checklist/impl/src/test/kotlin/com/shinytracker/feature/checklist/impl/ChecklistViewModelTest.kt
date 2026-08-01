package com.shinytracker.feature.checklist.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.shinytracker.core.data.CaughtRepository
import com.shinytracker.core.data.ChecklistRepository
import com.shinytracker.core.data.ProfileShareRepository
import com.shinytracker.core.model.CaughtRecord
import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.sprites.PokemonDexDataSource
import com.shinytracker.core.sprites.ShinyChecklistSource
import com.shinytracker.core.testing.FakeCaughtDao
import com.shinytracker.core.testing.MainDispatcherRule
import com.shinytracker.core.testing.testOnboardingPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChecklistViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var caughtRepository: CaughtRepository
    private lateinit var viewModel: ChecklistViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val checklistSource = ShinyChecklistSource(context, PokemonDexDataSource(context))
        caughtRepository = CaughtRepository(FakeCaughtDao())
        val checklistRepository = ChecklistRepository(checklistSource, caughtRepository)
        val profileShareRepository = ProfileShareRepository(context, caughtRepository)
        val onboardingPreferencesRepository = testOnboardingPreferencesRepository(context)
        viewModel = ChecklistViewModel(checklistRepository, profileShareRepository, onboardingPreferencesRepository)
    }

    @Test
    fun `search filters entries by species name`() =
        runTest {
            viewModel.onSearchChange("bulba")

            val state = viewModel.uiState.first { !it.isLoading && it.searchText == "bulba" }

            assertTrue(state.entries.isNotEmpty())
            assertTrue(state.entries.all { it.dexEntry.name.contains("bulba", ignoreCase = true) })
        }

    @Test
    fun `caught filter only returns caught entries`() =
        runTest {
            caughtRepository.recordIfAbsent(CaughtRecord(DexEntry(1, 0, 0, "Bulbasaur"), shiny = true, caughtAt = 1000L))
            viewModel.onFilterChange(AdvancedFilter(status = ChecklistFilter.CAUGHT))

            val state = viewModel.uiState.first { !it.isLoading && it.filter.status == ChecklistFilter.CAUGHT }

            assertTrue(state.entries.isNotEmpty())
            assertTrue(state.entries.all { it.caught })
            assertEquals(1, state.caughtCount)
        }

    @Test
    fun `toggleCaught flips an entry from uncaught to caught and back`() =
        runTest {
            val target =
                viewModel.uiState
                    .first { !it.isLoading }
                    .entries
                    .first { it.dexEntry.dexId == 1 }

            viewModel.toggleCaught(target)
            val caughtState = viewModel.uiState.first { !it.isLoading && it.caughtCount == 1 }
            assertTrue(caughtState.entries.first { it.dexEntry.dexId == 1 }.caught)

            viewModel.toggleCaught(caughtState.entries.first { it.dexEntry.dexId == 1 })
            val uncaughtState = viewModel.uiState.first { !it.isLoading && it.caughtCount == 0 }
            assertTrue(uncaughtState.entries.all { !it.caught })
        }
}
