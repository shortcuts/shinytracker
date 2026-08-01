package com.shinytracker.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.core.model.DisplayLanguage

@Composable
fun OnboardingScreen(
    displayLanguageChoice: DisplayLanguage?,
    accessibilityGranted: Boolean,
    overlayGranted: Boolean,
    onSetupComplete: (DisplayLanguage, Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (displayLanguageChoice == null) {
        SetupStep(onContinue = onSetupComplete, modifier = modifier)
    } else {
        PermissionsStep(
            accessibilityGranted = accessibilityGranted,
            overlayGranted = overlayGranted,
            onOpenAccessibilitySettings = onOpenAccessibilitySettings,
            onOpenOverlaySettings = onOpenOverlaySettings,
            modifier = modifier,
        )
    }
}

@Composable
private fun SetupStep(
    onContinue: (DisplayLanguage, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedLanguage by remember { mutableStateOf(DisplayLanguage.ENGLISH) }
    var scannerEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Set up shinytracker", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        Text(text = "Display language", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        DisplayLanguage.entries.forEach { language ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selectedLanguage == language, onClick = { selectedLanguage = language })
                Text(text = language.label())
            }
        }
        Spacer(Modifier.height(24.dp))

        ScannerToggleCard(checked = scannerEnabled, onCheckedChange = { scannerEnabled = it })
        Spacer(Modifier.height(24.dp))

        Button(onClick = { onContinue(selectedLanguage, scannerEnabled) }) { Text("Continue") }
    }
}

@Composable
internal fun ScannerToggleCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(shape = MaterialTheme.shapes.medium, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checked, onCheckedChange = onCheckedChange)
                Text(text = "Enable Scanner", style = MaterialTheme.typography.titleSmall)
            }
            Text(
                text =
                    "Turns on the box-scan feature, which reads your shiny box via screenshots. " +
                        "Leave this off to track shinies manually instead.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PermissionsStep(
    accessibilityGranted: Boolean,
    overlayGranted: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Set up scanning", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Grant these permissions to start scanning your shiny box.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        PermissionStepCard(
            title = "Accessibility service",
            description = "Lets shinytracker read your shiny box via screenshots.",
            isGranted = accessibilityGranted,
            onAction = onOpenAccessibilitySettings,
        )
        Spacer(Modifier.height(12.dp))
        PermissionStepCard(
            title = "Display over other apps",
            description = "Needed for the floating scan widget.",
            isGranted = overlayGranted,
            onAction = onOpenOverlaySettings,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "This screen updates automatically once both are granted.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PermissionStepCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onAction: () -> Unit,
) {
    Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            if (isGranted) {
                Text(
                    text = "Granted",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Button(onClick = onAction) { Text("Open Settings") }
            }
        }
    }
}

internal fun DisplayLanguage.label(): String =
    when (this) {
        DisplayLanguage.ENGLISH -> "English"
        DisplayLanguage.JAPANESE -> "Japanese"
        DisplayLanguage.CHINESE -> "Chinese"
        DisplayLanguage.FRENCH -> "French"
    }

@Preview
@Composable
private fun OnboardingScreenSetupStepPreview() {
    ShinyTheme {
        OnboardingScreen(
            displayLanguageChoice = null,
            accessibilityGranted = false,
            overlayGranted = false,
            onSetupComplete = { _, _ -> },
            onOpenAccessibilitySettings = {},
            onOpenOverlaySettings = {},
        )
    }
}

@Preview
@Composable
private fun OnboardingScreenPermissionsStepPreview() {
    ShinyTheme {
        OnboardingScreen(
            displayLanguageChoice = DisplayLanguage.ENGLISH,
            accessibilityGranted = false,
            overlayGranted = true,
            onSetupComplete = { _, _ -> },
            onOpenAccessibilitySettings = {},
            onOpenOverlaySettings = {},
        )
    }
}
