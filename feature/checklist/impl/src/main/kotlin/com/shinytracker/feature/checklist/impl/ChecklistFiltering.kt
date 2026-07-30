package com.shinytracker.feature.checklist.impl

import com.shinytracker.core.model.ChecklistEntry
import com.shinytracker.core.model.Generation
import com.shinytracker.core.model.PokemonType

enum class ChecklistFilter { ALL, CAUGHT, NOT_CAUGHT }

data class AdvancedFilter(
    val status: ChecklistFilter = ChecklistFilter.ALL,
    val generations: Set<Generation> = emptySet(),
    val types: Set<PokemonType> = emptySet(),
) {
    val isActive: Boolean
        get() = status != ChecklistFilter.ALL || generations.isNotEmpty() || types.isNotEmpty()
}

/** Search matches species name by substring, or dex id by numeric prefix. */
fun List<ChecklistEntry>.filterEntries(
    search: String,
    advancedFilter: AdvancedFilter,
): List<ChecklistEntry> = filter { entry -> matchesSearch(entry, search) && matchesFilter(entry, advancedFilter) }

private fun matchesSearch(
    entry: ChecklistEntry,
    search: String,
): Boolean =
    when {
        search.isBlank() -> true
        search.all { it.isDigit() } -> entry.dexEntry.dexId.toString().startsWith(search)
        else -> entry.dexEntry.name.contains(search, ignoreCase = true)
    }

private fun matchesFilter(
    entry: ChecklistEntry,
    advancedFilter: AdvancedFilter,
): Boolean =
    when (advancedFilter.status) {
        ChecklistFilter.ALL -> true
        ChecklistFilter.CAUGHT -> entry.caught
        ChecklistFilter.NOT_CAUGHT -> !entry.caught
    } &&
        (advancedFilter.generations.isEmpty() || entry.generation in advancedFilter.generations) &&
        (advancedFilter.types.isEmpty() || entry.dexEntry.types.any { it in advancedFilter.types })
