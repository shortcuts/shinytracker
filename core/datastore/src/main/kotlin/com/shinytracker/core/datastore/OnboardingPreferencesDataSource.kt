package com.shinytracker.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val SCANNER_ENABLED_KEY = booleanPreferencesKey("scanner_enabled")
private val DISPLAY_LANGUAGE_KEY = stringPreferencesKey("display_language")

/** Raw Preferences DataStore read/write for the onboarding gate's two persisted choices. */
@Singleton
class OnboardingPreferencesDataSource
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val scannerEnabled: Flow<Boolean> = dataStore.data.map { it[SCANNER_ENABLED_KEY] ?: false }
        val displayLanguageName: Flow<String?> = dataStore.data.map { it[DISPLAY_LANGUAGE_KEY] }

        suspend fun completeSetup(
            displayLanguageName: String,
            scannerEnabled: Boolean,
        ) {
            dataStore.edit { prefs ->
                prefs[DISPLAY_LANGUAGE_KEY] = displayLanguageName
                prefs[SCANNER_ENABLED_KEY] = scannerEnabled
            }
        }
    }
