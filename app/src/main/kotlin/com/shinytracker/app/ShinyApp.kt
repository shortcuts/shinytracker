package com.shinytracker.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.feature.scan.impl.ScanSlotResult

@Composable
fun ShinyApp(
    isServiceEnabled: Boolean = false,
    isWidgetRunning: Boolean = false,
    statusText: String = "",
    scanResults: List<ScanSlotResult> = emptyList(),
    reviewQueueSize: Int = 0,
    onOpenAccessibilitySettings: () -> Unit = {},
    onToggleWidget: () -> Unit = {},
    onCaptureScreenshot: () -> Unit = {},
    onScrollBoxDown: () -> Unit = {},
    onRunFullScan: () -> Unit = {},
    onOpenChecklist: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "shinytracker", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(32.dp))

        StatusCard(
            isServiceEnabled = isServiceEnabled,
            onOpenAccessibilitySettings = onOpenAccessibilitySettings,
            onRunFullScan = onRunFullScan,
        )

        if (isServiceEnabled) {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onCaptureScreenshot) { Text("Capture screenshot") }
                TextButton(onClick = onScrollBoxDown) { Text("Scroll box down") }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onToggleWidget) {
                Text(if (isWidgetRunning) "Disable scan widget" else "Enable scan widget")
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(label = "New this scan", value = scanResults.count { it.isNew }.toString())
                StatTile(label = "Needs review", value = reviewQueueSize.toString())
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onOpenChecklist) { Text("View checklist") }

        if (statusText.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(text = statusText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusCard(
    isServiceEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onRunFullScan: () -> Unit,
) {
    Card(shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val dotColor = if (isServiceEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
                Spacer(Modifier.size(8.dp))
                Text(
                    text = if (isServiceEnabled) "Scan service ready" else "Scan service disabled",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(16.dp))
            if (isServiceEnabled) {
                Button(onClick = onRunFullScan) { Text("Run full scan") }
            } else {
                Button(onClick = onOpenAccessibilitySettings) { Text("Enable accessibility service") }
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
) {
    Card(shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"))
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Preview
@Composable
private fun ShinyAppPreview() {
    ShinyTheme { ShinyApp() }
}

@Preview
@Composable
private fun ShinyAppEnabledPreview() {
    ShinyTheme { ShinyApp(isServiceEnabled = true, reviewQueueSize = 2) }
}
