package com.shinytracker.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shinytracker.core.designsystem.ShinyTheme

@Composable
fun OnboardingScreen(
    accessibilityGranted: Boolean,
    overlayGranted: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Set up shinytracker", style = MaterialTheme.typography.headlineSmall)
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

@Preview
@Composable
private fun OnboardingScreenPreview() {
    ShinyTheme {
        OnboardingScreen(
            accessibilityGranted = false,
            overlayGranted = true,
            onOpenAccessibilitySettings = {},
            onOpenOverlaySettings = {},
        )
    }
}

@Preview
@Composable
private fun OnboardingScreenBothGrantedPreview() {
    ShinyTheme {
        OnboardingScreen(
            accessibilityGranted = true,
            overlayGranted = true,
            onOpenAccessibilitySettings = {},
            onOpenOverlaySettings = {},
        )
    }
}
