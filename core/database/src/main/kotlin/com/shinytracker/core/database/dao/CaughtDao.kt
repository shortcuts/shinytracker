package com.shinytracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shinytracker.core.database.entities.CaughtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaughtDao {
    /** Returns the inserted rowId, or -1 if ignored because the primary key already exists. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entity: CaughtEntity): Long

    @Query("SELECT * FROM caught ORDER BY caughtAt DESC")
    fun observeAll(): Flow<List<CaughtEntity>>

    @Query(
        "DELETE FROM caught WHERE dexId = :dexId AND formId = :formId AND costumeId = :costumeId AND shiny = :shiny",
    )
    suspend fun delete(
        dexId: Int,
        formId: Int,
        costumeId: Int,
        shiny: Boolean,
    )
}
