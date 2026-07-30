package com.shinytracker.core.data

import com.shinytracker.core.database.dao.CaughtDao
import com.shinytracker.core.database.entities.CaughtEntity
import com.shinytracker.core.model.CaughtRecord
import com.shinytracker.core.model.DexEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaughtRepository
    @Inject
    constructor(
        private val caughtDao: CaughtDao,
    ) {
        fun observeCaught(): Flow<List<CaughtRecord>> = caughtDao.observeAll().map { entities -> entities.map { it.toDomain() } }

        /** Returns true if this is newly recorded, false if it was already caught. */
        suspend fun recordIfAbsent(record: CaughtRecord): Boolean = caughtDao.insertIfAbsent(record.toEntity()) != -1L
    }

private fun CaughtEntity.toDomain() = CaughtRecord(DexEntry(dexId, formId, costumeId, name), shiny, caughtAt)

private fun CaughtRecord.toEntity() = CaughtEntity(dexEntry.dexId, dexEntry.formId, dexEntry.costumeId, shiny, dexEntry.name, caughtAt)
