package com.shinytracker.core.database.di

import android.content.Context
import androidx.room.Room
import com.shinytracker.core.common.constants.AppConstants
import com.shinytracker.core.database.ShinyTrackerDatabase
import com.shinytracker.core.database.dao.CaughtDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideShinyTrackerDatabase(
        @ApplicationContext context: Context,
    ): ShinyTrackerDatabase =
        Room
            .databaseBuilder(context, ShinyTrackerDatabase::class.java, AppConstants.DatabaseConstants.DATABASE_NAME)
            .build()

    @Provides
    fun provideCaughtDao(database: ShinyTrackerDatabase): CaughtDao = database.caughtDao()
}
