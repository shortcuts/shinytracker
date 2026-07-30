package com.shinytracker.feature.checklist.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinytracker.core.data.ProfileShareRepository
import com.shinytracker.core.model.ChecklistEntry
import com.shinytracker.core.model.Generation
import com.shinytracker.core.sprites.ShinyChecklistSource
import com.shinytracker.feature.checklist.api.ChecklistRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SharedProfileUiState(
    val entries: List<ChecklistEntry> = emptyList(),
    val searchText: String = "",
    val filter: AdvancedFilter = AdvancedFilter(),
    val caughtCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
)

/**
 * Read-only mode: someone else's shared caught list, imported via [ProfileShareRepository].
 * Never touches CaughtRepository -- these are not the device owner's own catches.
 */
@HiltViewModel
class SharedProfileViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val profileShareRepository: ProfileShareRepository,
        private val checklistSource: ShinyChecklistSource,
    ) : ViewModel() {
        private val loadedEntries = MutableStateFlow<List<ChecklistEntry>?>(null)
        private val loadFailed = MutableStateFlow(false)
        private val searchText = MutableStateFlow("")
        private val filter = MutableStateFlow(AdvancedFilter())

        val uiState: StateFlow<SharedProfileUiState> =
            combine(loadedEntries, searchText, filter, loadFailed) { entries, search, currentFilter, failed ->
                if (entries == null) {
                    SharedProfileUiState(isLoading = !failed, loadFailed = failed)
                } else {
                    SharedProfileUiState(
                        entries = entries.filterEntries(search, currentFilter),
                        searchText = search,
                        filter = currentFilter,
                        caughtCount = entries.count { it.caught },
                        totalCount = entries.size,
                        isLoading = false,
                    )
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SharedProfileUiState())

        init {
            val uri = ChecklistRoute.parseSharedProfileArg(savedStateHandle[ChecklistRoute.SHARED_ARG_KEY])
            viewModelScope.launch {
                if (uri == null) {
                    loadFailed.value = true
                    return@launch
                }
                profileShareRepository
                    .importProfile(uri)
                    .onSuccess { imported ->
                        val species = checklistSource.observeChecklist().first()
                        loadedEntries.value =
                            species.map { dex ->
                                val match = imported.firstOrNull { it.dexEntry.dexId == dex.dexId }
                                ChecklistEntry(
                                    dexEntry = dex,
                                    caught = match != null,
                                    caughtAt = match?.caughtAt,
                                    generation = Generation.fromDexId(dex.dexId),
                                )
                            }
                    }.onFailure {
                        loadFailed.value = true
                    }
            }
        }

        fun onSearchChange(text: String) {
            searchText.value = text
        }

        fun onFilterChange(newFilter: AdvancedFilter) {
            filter.value = newFilter
        }
    }
