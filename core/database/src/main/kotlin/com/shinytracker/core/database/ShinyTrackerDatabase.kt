package com.shinytracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shinytracker.core.database.dao.CaughtDao
import com.shinytracker.core.database.entities.CaughtEntity

@Database(entities = [CaughtEntity::class], version = 1, exportSchema = true)
abstract class ShinyTrackerDatabase : RoomDatabase() {
    abstract fun caughtDao(): CaughtDao
}
