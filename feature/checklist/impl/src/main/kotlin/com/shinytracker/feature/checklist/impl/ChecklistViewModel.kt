package com.shinytracker.feature.checklist.impl

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinytracker.core.data.ChecklistRepository
import com.shinytracker.core.data.ProfileShareRepository
import com.shinytracker.core.model.ChecklistEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChecklistUiState(
    val entries: List<ChecklistEntry> = emptyList(),
    val searchText: String = "",
    val filter: AdvancedFilter = AdvancedFilter(),
    val caughtCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
)

/** Owner mode: the device owner's own checklist, editable via scanning, exportable for sharing. */
@HiltViewModel
class ChecklistViewModel
    @Inject
    constructor(
        private val checklistRepository: ChecklistRepository,
        private val profileShareRepository: ProfileShareRepository,
    ) : ViewModel() {
        private val searchText = MutableStateFlow("")
        private val filter = MutableStateFlow(AdvancedFilter())

        val uiState: StateFlow<ChecklistUiState> =
            combine(checklistRepository.observeChecklist(), searchText, filter) { entries, search, currentFilter ->
                ChecklistUiState(
                    entries = entries.filterEntries(search, currentFilter),
                    searchText = search,
                    filter = currentFilter,
                    caughtCount = entries.count { it.caught },
                    totalCount = entries.size,
                    isLoading = false,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChecklistUiState())

        fun onSearchChange(text: String) {
            searchText.value = text
        }

        fun onFilterChange(newFilter: AdvancedFilter) {
            filter.value = newFilter
        }

        fun exportProfile(onResult: (Result<Uri>) -> Unit) {
            viewModelScope.launch { onResult(profileShareRepository.exportProfile()) }
        }
    }
