package com.shinytracker.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.shinytracker.core.common.constants.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.onboardingDataStore: DataStore<Preferences> by
    preferencesDataStore(name = AppConstants.DataStoreConstants.ONBOARDING_PREFS_NAME)

@Module
@InstallIn(SingletonComponent::class)
object OnboardingPreferencesModule {
    @Provides
    @Singleton
    fun provideOnboardingPreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.onboardingDataStore
}
