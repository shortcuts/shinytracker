package com.shinytracker.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.core.model.DisplayLanguage

enum class SettingsSyncState { IDLE, SYNCING, SUCCESS, ERROR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    displayLanguage: DisplayLanguage,
    scannerEnabled: Boolean,
    onLanguageChange: (DisplayLanguage) -> Unit,
    onScannerEnabledChange: (Boolean) -> Unit,
    syncState: SettingsSyncState,
    onSyncClick: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Open navigation menu")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Text(text = "Display language", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            DisplayLanguage.entries.forEach { language ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = displayLanguage == language,
                        onClick = { onLanguageChange(language) },
                    )
                    Text(text = language.label())
                }
            }
            Spacer(Modifier.height(24.dp))

            ScannerToggleCard(checked = scannerEnabled, onCheckedChange = onScannerEnabledChange)

            Spacer(Modifier.height(24.dp))
            Text(text = "Checklist data", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onSyncClick, enabled = syncState != SettingsSyncState.SYNCING) {
                    Text("Sync checklist")
                }
                Spacer(Modifier.width(12.dp))
                when (syncState) {
                    SettingsSyncState.SYNCING -> {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }

                    SettingsSyncState.SUCCESS -> {
                        Text("Synced", color = MaterialTheme.colorScheme.primary)
                    }

                    SettingsSyncState.ERROR -> {
                        Text("Sync failed", color = MaterialTheme.colorScheme.error)
                    }

                    SettingsSyncState.IDLE -> {}
                }
            }
        }
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    ShinyTheme {
        SettingsScreen(
            displayLanguage = DisplayLanguage.ENGLISH,
            scannerEnabled = false,
            onLanguageChange = {},
            onScannerEnabledChange = {},
            syncState = SettingsSyncState.IDLE,
            onSyncClick = {},
            onOpenDrawer = {},
        )
    }
}
