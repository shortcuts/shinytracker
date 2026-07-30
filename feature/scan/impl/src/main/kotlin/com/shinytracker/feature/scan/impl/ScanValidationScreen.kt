package com.shinytracker.feature.scan.impl

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.model.MatchResult

/**
 * Full-screen confirm/reject panel for both the screenshot and automated-scan flows.
 * Empty [entries] means nothing is left to review — [onDismiss] should be called by the caller
 * once that happens.
 */
@Composable
fun ScanValidationScreen(
    entries: List<PendingReview>,
    onConfirm: (index: Int, chosen: MatchResult) -> Unit,
    onReject: (index: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Review scan results", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Dismiss") }
        }
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Nothing left to review", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(entries.size) { index ->
                    ReviewEntryCard(
                        entry = entries[index],
                        onConfirm = { chosen -> onConfirm(index, chosen) },
                        onReject = { onReject(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewEntryCard(
    entry: PendingReview,
    onConfirm: (MatchResult) -> Unit,
    onReject: () -> Unit,
) {
    var selected by remember(entry) { mutableIntStateOf(0) }
    val candidate = entry.candidates.getOrNull(selected)

    Card(shape = MaterialTheme.shapes.medium) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                bitmap = entry.crop.asImageBitmap(),
                contentDescription = candidate?.dexEntry?.name ?: "Unidentified crop",
                modifier = Modifier.size(48.dp),
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(text = candidate?.let { it.dexEntry.name + if (it.shiny) " ✨" else "" } ?: "No match")
                if (candidate != null) {
                    Text(
                        text = "${(candidate.confidence * 100).toInt()}% match",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                if (entry.candidates.size > 1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        entry.candidates.forEachIndexed { i, alt ->
                            if (i != selected) {
                                AssistChip(onClick = { selected = i }, label = { Text(alt.dexEntry.name) })
                            }
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Button(onClick = { candidate?.let(onConfirm) }, enabled = candidate != null) { Text("Confirm") }
                OutlinedButton(onClick = onReject) { Text("Reject") }
            }
        }
    }
}

@Preview
@Composable
private fun ScanValidationScreenPreview() {
    val crop = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
    val candidates =
        listOf(
            MatchResult(DexEntry(25, 0, 0, "Pikachu"), shiny = true, confidence = 0.92f),
            MatchResult(DexEntry(26, 0, 0, "Raichu"), shiny = true, confidence = 0.61f),
        )
    ShinyTheme {
        ScanValidationScreen(
            entries = listOf(PendingReview(crop, candidates)),
            onConfirm = { _, _ -> },
            onReject = {},
            onDismiss = {},
        )
    }
}
