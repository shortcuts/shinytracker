package com.shinytracker.core.data

import com.shinytracker.core.datastore.OnboardingPreferencesDataSource
import com.shinytracker.core.model.DisplayLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Onboarding's two persisted choices: display language and whether the scanner is enabled. */
@Singleton
class OnboardingPreferencesRepository
    @Inject
    constructor(
        private val dataSource: OnboardingPreferencesDataSource,
    ) {
        val scannerEnabled: Flow<Boolean> = dataSource.scannerEnabled

        /** Raw persisted choice -- null until onboarding's setup step is completed. */
        val displayLanguageChoice: Flow<DisplayLanguage?> =
            dataSource.displayLanguageName.map { name -> name?.let { runCatching { DisplayLanguage.valueOf(it) }.getOrNull() } }

        /** [displayLanguageChoice] defaulted to English -- for rendering, never null. */
        val displayLanguage: Flow<DisplayLanguage> = displayLanguageChoice.map { it ?: DisplayLanguage.ENGLISH }

        suspend fun completeSetup(
            displayLanguage: DisplayLanguage,
            scannerEnabled: Boolean,
        ) {
            dataSource.completeSetup(displayLanguage.name, scannerEnabled)
        }
    }
