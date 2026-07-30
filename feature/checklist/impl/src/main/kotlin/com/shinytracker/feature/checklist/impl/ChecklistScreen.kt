package com.shinytracker.feature.checklist.impl

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.core.model.ChecklistEntry
import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.model.Generation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    onExportProfile: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChecklistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChecklistScaffold(
        title = "Shiny Checklist",
        searchText = uiState.searchText,
        onSearchChange = viewModel::onSearchChange,
        caughtCount = uiState.caughtCount,
        totalCount = uiState.totalCount,
        entries = uiState.entries,
        isLoading = uiState.isLoading,
        errorMessage = null,
        banner = null,
        actions = {
            IconButton(onClick = { viewModel.exportProfile { result -> result.onSuccess(onExportProfile) } }) {
                Icon(Icons.Default.Share, contentDescription = "Share my profile")
            }
        },
        filterChips = { FilterChipRow(selected = uiState.filter, onSelect = viewModel::onFilterChange) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedProfileScreen(
    ownerLabel: String,
    modifier: Modifier = Modifier,
    viewModel: SharedProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChecklistScaffold(
        title = "Shared Checklist",
        searchText = uiState.searchText,
        onSearchChange = viewModel::onSearchChange,
        caughtCount = uiState.caughtCount,
        totalCount = uiState.totalCount,
        entries = uiState.entries,
        isLoading = uiState.isLoading,
        errorMessage = if (uiState.loadFailed) "Could not open this shared profile." else null,
        banner = { SharedProfileBanner(ownerLabel) },
        actions = {},
        filterChips = null,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChecklistScaffold(
    title: String,
    searchText: String,
    onSearchChange: (String) -> Unit,
    caughtCount: Int,
    totalCount: Int,
    entries: List<ChecklistEntry>,
    isLoading: Boolean,
    errorMessage: String?,
    banner: (@Composable () -> Unit)?,
    actions: @Composable () -> Unit,
    filterChips: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var searchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    IconButton(onClick = { searchExpanded = !searchExpanded }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    actions()
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            banner?.invoke()
            if (searchExpanded) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    label = { Text("Search species") },
                    singleLine = true,
                )
            }
            if (!isLoading) {
                ProgressHeader(caughtCount = caughtCount, totalCount = totalCount)
                filterChips?.invoke()
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                errorMessage != null -> EmptyState(errorMessage)
                entries.isEmpty() -> EmptyState("No species match your search. Try a different name or clear the filter.")
                else -> ChecklistList(entries)
            }
        }
    }
}

@Composable
private fun ChecklistList(entries: List<ChecklistEntry>) {
    val grouped = entries.groupBy { it.generation }.toSortedMap(compareBy { it.ordinal })
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        grouped.forEach { (generation, regionEntries) ->
            item(key = "header-${generation.name}") {
                RegionStickyHeader(generation, caughtCount = regionEntries.count { it.caught }, totalCount = regionEntries.size)
            }
            item(key = "grid-${generation.name}") {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 80.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                ) {
                    items(regionEntries, key = { it.dexEntry.dexId to it.dexEntry.formId }) { entry ->
                        ChecklistEntryIcon(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(
    caughtCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = "$caughtCount / $totalCount caught", style = MaterialTheme.typography.titleMedium)
        val progress = if (totalCount > 0) caughtCount / totalCount.toFloat() else 0f
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
    }
}

@Composable
private fun FilterChipRow(
    selected: ChecklistFilter,
    onSelect: (ChecklistFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(ChecklistFilter.entries.toList()) { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(filter.label()) },
            )
        }
    }
}

private fun ChecklistFilter.label(): String =
    when (this) {
        ChecklistFilter.ALL -> "All"
        ChecklistFilter.CAUGHT -> "Caught"
        ChecklistFilter.NOT_CAUGHT -> "Not caught"
    }

@Composable
private fun RegionStickyHeader(
    generation: Generation,
    caughtCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = generation.displayName(), style = MaterialTheme.typography.titleSmall)
        Text(text = "$caughtCount/$totalCount", style = MaterialTheme.typography.labelMedium)
    }
}

private fun Generation.displayName(): String = name.lowercase().replaceFirstChar(Char::uppercase)

@Composable
private fun ChecklistEntryIcon(
    entry: ChecklistEntry,
    modifier: Modifier = Modifier,
) {
    // ponytail: sprite art needs an image loader reading core:sprites' vendored PNGs
    // (e.g. Coil) -- out of this task's scope. Text tile until that lands.
    val background = if (entry.caught) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val caughtLabel = if (entry.caught) "caught" else "not caught"
    Column(
        modifier =
            modifier
                .size(72.dp)
                .padding(4.dp)
                .background(background, RoundedCornerShape(12.dp))
                .padding(8.dp)
                .clearAndSetSemantics {
                    contentDescription = "#%03d %s, %s".format(entry.dexEntry.dexId, entry.dexEntry.name, caughtLabel)
                },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text =
                entry.dexEntry.name
                    .take(2)
                    .uppercase(),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(text = "#%03d".format(entry.dexEntry.dexId), style = MaterialTheme.typography.labelSmall)
        if (entry.caught) {
            Icon(Icons.Default.Check, contentDescription = "Caught", modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SharedProfileBanner(
    ownerLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(16.dp),
    ) {
        Text(text = "Viewing $ownerLabel's shared profile — read only", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun sampleEntries(): List<ChecklistEntry> =
    listOf(
        ChecklistEntry(DexEntry(1, 0, 0, "Bulbasaur"), caught = true, caughtAt = 1000L, generation = Generation.KANTO),
        ChecklistEntry(DexEntry(4, 0, 0, "Charmander"), caught = false, caughtAt = null, generation = Generation.KANTO),
        ChecklistEntry(DexEntry(152, 0, 0, "Chikorita"), caught = false, caughtAt = null, generation = Generation.JOHTO),
    )

@Preview
@Composable
private fun ChecklistScaffoldPreview() {
    ShinyTheme {
        ChecklistScaffold(
            title = "Shiny Checklist",
            searchText = "",
            onSearchChange = {},
            caughtCount = 1,
            totalCount = 3,
            entries = sampleEntries(),
            isLoading = false,
            errorMessage = null,
            banner = null,
            actions = {},
            filterChips = { FilterChipRow(selected = ChecklistFilter.ALL, onSelect = {}) },
        )
    }
}

@Preview
@Composable
private fun SharedProfileScaffoldPreview() {
    ShinyTheme {
        ChecklistScaffold(
            title = "Shared Checklist",
            searchText = "",
            onSearchChange = {},
            caughtCount = 1,
            totalCount = 3,
            entries = sampleEntries(),
            isLoading = false,
            errorMessage = null,
            banner = { SharedProfileBanner("Ash") },
            actions = {},
            filterChips = null,
        )
    }
}

@Preview
@Composable
private fun EmptyStatePreview() {
    ShinyTheme { EmptyState("No species match your search or filter.") }
}
