package com.shinytracker.feature.checklist.impl

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.shinytracker.core.data.CaughtRepository
import com.shinytracker.core.data.ProfileShareRepository
import com.shinytracker.core.sprites.PokemonDexDataSource
import com.shinytracker.core.sprites.ShinyChecklistSource
import com.shinytracker.core.testing.FakeCaughtDao
import com.shinytracker.core.testing.MainDispatcherRule
import com.shinytracker.core.testing.testOnboardingPreferencesRepository
import com.shinytracker.feature.checklist.api.ChecklistRoute
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class SharedProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val profileShareRepository = ProfileShareRepository(context, CaughtRepository(FakeCaughtDao()))
    private val checklistSource = ShinyChecklistSource(context, PokemonDexDataSource(context))
    private val onboardingPreferencesRepository = testOnboardingPreferencesRepository(context)

    private fun sharedProfileFile(): Uri {
        val file =
            File(context.cacheDir, "incoming-shared.json").apply {
                writeText(
                    """{"schemaVersion":1,"entries":[
                    |{"dexId":1,"formId":0,"costumeId":0,"shiny":true,"name":"Bulbasaur","caughtAt":1000}
                    |]}
                    """.trimMargin(),
                )
            }
        return Uri.fromFile(file)
    }

    @Test
    fun `loads the imported profile and marks only its own species as caught`() =
        runTest {
            val savedStateHandle = SavedStateHandle(mapOf(ChecklistRoute.SHARED_ARG_KEY to Uri.encode(sharedProfileFile().toString())))
            val viewModel =
                SharedProfileViewModel(savedStateHandle, profileShareRepository, checklistSource, onboardingPreferencesRepository)

            val state = viewModel.uiState.first { !it.isLoading }

            assertTrue(state.entries.isNotEmpty())
            assertEquals(1, state.caughtCount)
            assertTrue(state.entries.first { it.dexEntry.dexId == 1 }.caught)
        }

    @Test
    fun `missing profile arg surfaces a load failure instead of crashing`() =
        runTest {
            val savedStateHandle = SavedStateHandle()
            val viewModel =
                SharedProfileViewModel(savedStateHandle, profileShareRepository, checklistSource, onboardingPreferencesRepository)

            val state = viewModel.uiState.first { !it.isLoading }

            assertTrue(state.loadFailed)
        }
}
