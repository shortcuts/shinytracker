package com.shinytracker.app

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.core.model.DisplayLanguage

@Composable
fun OnboardingScreen(
    displayLanguageChoice: DisplayLanguage?,
    scannerEnabledPref: Boolean,
    accessibilityGranted: Boolean,
    overlayGranted: Boolean,
    onSetupComplete: (DisplayLanguage, Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (displayLanguageChoice == null || !scannerEnabledPref) {
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
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(remember { ScrollState(0) })
                .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))
        Text(text = "Set up shinytracker", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))

        Text(text = "Display language", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Which language the checklist shows species names in.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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

        Button(
            onClick = { onContinue(selectedLanguage, scannerEnabled) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Continue") }
        Spacer(Modifier.height(24.dp))
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
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(remember { ScrollState(0) })
                .padding(horizontal = 20.dp),
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
            icon = Icons.Default.Accessibility,
            actionLabel = "Open Settings",
            onAction = onOpenAccessibilitySettings,
        )
        Spacer(Modifier.height(12.dp))
        PermissionStepCard(
            title = "Display over other apps",
            description = "Needed for the floating scan widget.",
            isGranted = overlayGranted,
            icon = Icons.Default.Layers,
            actionLabel = "Open Settings",
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
    icon: ImageVector,
    actionLabel: String,
    onAction: () -> Unit,
) {
    val badgeContainerColor =
        if (isGranted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val badgeTint =
        if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(badgeContainerColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.CheckCircle else icon,
                        contentDescription = null,
                        tint = badgeTint,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!isGranted) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onAction, modifier = Modifier.weight(1f)) {
                        Text(text = actionLabel, style = MaterialTheme.typography.labelMedium)
                    }
                }
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
            scannerEnabledPref = false,
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
            scannerEnabledPref = true,
            accessibilityGranted = false,
            overlayGranted = true,
            onSetupComplete = { _, _ -> },
            onOpenAccessibilitySettings = {},
            onOpenOverlaySettings = {},
        )
    }
}
