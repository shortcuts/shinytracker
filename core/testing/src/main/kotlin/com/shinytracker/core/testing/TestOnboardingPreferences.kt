package com.shinytracker.core.testing

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.shinytracker.core.data.OnboardingPreferencesRepository
import com.shinytracker.core.datastore.OnboardingPreferencesDataSource
import java.io.File

/**
 * Real (temp-file-backed) [OnboardingPreferencesRepository] for tests -- no fake needed, Preferences
 * DataStore reads/writes are fast. Each call gets its own uniquely-named file so parallel/repeated
 * test runs never share state.
 */
fun testOnboardingPreferencesRepository(context: Context): OnboardingPreferencesRepository {
    val dataStore =
        PreferenceDataStoreFactory.create(
            produceFile = { File.createTempFile("onboarding_prefs", ".preferences_pb", context.filesDir) },
        )
    return OnboardingPreferencesRepository(OnboardingPreferencesDataSource(dataStore))
}
