package com.shinytracker.feature.checklist.impl

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.core.model.ChecklistEntry
import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.model.Generation
import com.shinytracker.core.model.PokemonType

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
        filter = uiState.filter,
        onFilterChange = viewModel::onFilterChange,
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
        filter = uiState.filter,
        onFilterChange = viewModel::onFilterChange,
        caughtCount = uiState.caughtCount,
        totalCount = uiState.totalCount,
        entries = uiState.entries,
        isLoading = uiState.isLoading,
        errorMessage = if (uiState.loadFailed) "Could not open this shared profile." else null,
        banner = { SharedProfileBanner(ownerLabel) },
        actions = {},
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChecklistScaffold(
    title: String,
    searchText: String,
    onSearchChange: (String) -> Unit,
    filter: AdvancedFilter,
    onFilterChange: (AdvancedFilter) -> Unit,
    caughtCount: Int,
    totalCount: Int,
    entries: List<ChecklistEntry>,
    isLoading: Boolean,
    errorMessage: String?,
    banner: (@Composable () -> Unit)?,
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchExpanded by remember { mutableStateOf(false) }
    var filterSheetVisible by remember { mutableStateOf(false) }
    var collapsed by rememberSaveable { mutableStateOf(setOf<String>()) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    IconButton(onClick = { searchExpanded = !searchExpanded }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { filterSheetVisible = true }) {
                        BadgedBox(badge = { if (filter.isActive) Badge() }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
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
                    label = { Text("Search by name or dex #") },
                    singleLine = true,
                )
            }
            if (!isLoading) {
                ProgressHeader(caughtCount = caughtCount, totalCount = totalCount)
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                errorMessage != null -> EmptyState(errorMessage)
                entries.isEmpty() -> EmptyState("No species match your search or filter.")
                else ->
                    ChecklistList(entries, collapsed) { generation ->
                        collapsed = if (generation.name in collapsed) collapsed - generation.name else collapsed + generation.name
                    }
            }
        }
    }

    if (filterSheetVisible) {
        ModalBottomSheet(onDismissRequest = { filterSheetVisible = false }) {
            FilterSheetContent(filter = filter, onFilterChange = onFilterChange, onClose = { filterSheetVisible = false })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSheetContent(
    filter: AdvancedFilter,
    onFilterChange: (AdvancedFilter) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
        Text("Status", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
            ChecklistFilter.entries.forEach { status ->
                FilterChip(
                    selected = filter.status == status,
                    onClick = { onFilterChange(filter.copy(status = status)) },
                    label = { Text(status.label()) },
                )
            }
        }

        Text("Generation", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
            Generation.entries.forEach { gen ->
                FilterChip(
                    selected = gen in filter.generations,
                    onClick = { onFilterChange(filter.copy(generations = filter.generations.toggle(gen))) },
                    label = { Text(gen.displayName()) },
                )
            }
        }

        Text("Type", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)) {
            PokemonType.entries.forEach { type ->
                FilterChip(
                    selected = type in filter.types,
                    onClick = { onFilterChange(filter.copy(types = filter.types.toggle(type))) },
                    label = { Text(type.displayName()) },
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onFilterChange(AdvancedFilter()) }) { Text("Clear all") }
            TextButton(onClick = onClose) { Text("Done") }
        }
    }
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChecklistList(
    entries: List<ChecklistEntry>,
    collapsed: Set<String>,
    onToggleCollapse: (Generation) -> Unit,
) {
    val grouped = entries.groupBy { it.generation }.toSortedMap(compareBy { it.ordinal })
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        grouped.forEach { (generation, regionEntries) ->
            item(key = "header-${generation.name}") {
                RegionHeader(
                    generation = generation,
                    caughtCount = regionEntries.count { it.caught },
                    totalCount = regionEntries.size,
                    collapsed = generation.name in collapsed,
                    onToggle = { onToggleCollapse(generation) },
                )
            }
            if (generation.name !in collapsed) {
                // ponytail: LazyVerticalGrid nested in a LazyColumn item crashes with an
                // infinite-height-constraint exception; FlowRow wraps naturally instead.
                item(key = "grid-${generation.name}") {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        regionEntries.forEach { entry ->
                            SpriteTile(entry)
                        }
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
        Text(
            text = "$caughtCount / $totalCount caught",
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
        )
        val progress = if (totalCount > 0) caughtCount / totalCount.toFloat() else 0f
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
    }
}

private fun ChecklistFilter.label(): String =
    when (this) {
        ChecklistFilter.ALL -> "All"
        ChecklistFilter.CAUGHT -> "Caught"
        ChecklistFilter.NOT_CAUGHT -> "Missing"
    }

@Composable
private fun RegionHeader(
    generation: Generation,
    caughtCount: Int,
    totalCount: Int,
    collapsed: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(if (collapsed) 180f else 0f, label = "chevron")
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = generation.displayName(), style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$caughtCount/$totalCount",
                style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
            )
            Spacer(Modifier.size(4.dp))
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = if (collapsed) "Expand ${generation.displayName()}" else "Collapse ${generation.displayName()}",
                modifier = Modifier.rotate(rotation).size(20.dp),
            )
        }
    }
}

private fun Generation.displayName(): String = name.lowercase().replaceFirstChar(Char::uppercase)

private fun PokemonType.displayName(): String = name.lowercase().replaceFirstChar(Char::uppercase)

@Composable
private fun SpriteTile(
    entry: ChecklistEntry,
    modifier: Modifier = Modifier,
) {
    val caughtLabel = if (entry.caught) "caught" else "not caught"
    val grayscale = remember { ColorMatrix().apply { setToSaturation(0f) } }
    Box(
        modifier =
            modifier
                .size(76.dp)
                .padding(4.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .clearAndSetSemantics {
                    contentDescription = "#%03d %s, %s".format(entry.dexEntry.dexId, entry.dexEntry.name, caughtLabel)
                },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = spriteAssetUri(entry.dexEntry),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                colorFilter = if (entry.caught) null else ColorFilter.colorMatrix(grayscale),
                alpha = if (entry.caught) 1f else 0.4f,
            )
            Text(
                text = "#%03d".format(entry.dexEntry.dexId),
                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
            )
        }
        if (entry.caught) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopEnd).size(14.dp),
            )
        }
    }
}

/** All checklist sprites are shiny -- this app only tracks the shiny form. */
private fun spriteAssetUri(dexEntry: DexEntry): String {
    val costume = if (dexEntry.costumeId != 0) "_%02d".format(dexEntry.costumeId) else ""
    return "file:///android_asset/sprites/pokemon_icon_%03d_%02d%s_shiny.png"
        .format(dexEntry.dexId, dexEntry.formId, costume)
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
            filter = AdvancedFilter(),
            onFilterChange = {},
            caughtCount = 1,
            totalCount = 3,
            entries = sampleEntries(),
            isLoading = false,
            errorMessage = null,
            banner = null,
            actions = {},
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
            filter = AdvancedFilter(),
            onFilterChange = {},
            caughtCount = 1,
            totalCount = 3,
            entries = sampleEntries(),
            isLoading = false,
            errorMessage = null,
            banner = { SharedProfileBanner("Ash") },
            actions = {},
        )
    }
}

@Preview
@Composable
private fun EmptyStatePreview() {
    ShinyTheme { EmptyState("No species match your search or filter.") }
}
