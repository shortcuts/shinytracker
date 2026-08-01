package com.shinytracker.core.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.shinytracker.core.model.DisplayLanguage
import com.shinytracker.core.testing.testOnboardingPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OnboardingPreferencesRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = testOnboardingPreferencesRepository(context)

    @Test
    fun `defaults to no language chosen and scanner disabled`() =
        runTest {
            assertNull(repository.displayLanguageChoice.first())
            assertEquals(DisplayLanguage.ENGLISH, repository.displayLanguage.first())
            assertEquals(false, repository.scannerEnabled.first())
        }

    @Test
    fun `completeSetup persists both choices together`() =
        runTest {
            repository.completeSetup(DisplayLanguage.JAPANESE, scannerEnabled = true)

            assertEquals(DisplayLanguage.JAPANESE, repository.displayLanguageChoice.first())
            assertEquals(DisplayLanguage.JAPANESE, repository.displayLanguage.first())
            assertEquals(true, repository.scannerEnabled.first())
        }
}
