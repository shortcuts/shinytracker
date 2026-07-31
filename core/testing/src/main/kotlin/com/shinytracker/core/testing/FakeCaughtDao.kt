package com.shinytracker.core.testing

import com.shinytracker.core.database.dao.CaughtDao
import com.shinytracker.core.database.entities.CaughtEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCaughtDao : CaughtDao {
    private val state = MutableStateFlow<List<CaughtEntity>>(emptyList())

    override suspend fun insertIfAbsent(entity: CaughtEntity): Long {
        val exists =
            state.value.any {
                it.dexId == entity.dexId && it.formId == entity.formId && it.costumeId == entity.costumeId && it.shiny == entity.shiny
            }
        if (exists) return -1L
        state.value = state.value + entity
        return state.value.size.toLong()
    }

    override fun observeAll(): Flow<List<CaughtEntity>> = state.map { list -> list.sortedByDescending { it.caughtAt } }

    override suspend fun delete(
        dexId: Int,
        formId: Int,
        costumeId: Int,
        shiny: Boolean,
    ) {
        state.value =
            state.value.filterNot {
                it.dexId == dexId && it.formId == formId && it.costumeId == costumeId && it.shiny == shiny
            }
    }
}
