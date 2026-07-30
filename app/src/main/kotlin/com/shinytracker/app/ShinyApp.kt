package com.shinytracker.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ShinyApp(
    isServiceEnabled: Boolean = false,
    statusText: String = "",
    onOpenAccessibilitySettings: () -> Unit = {},
    onCaptureScreenshot: () -> Unit = {},
    onScrollBoxDown: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "shinytracker")
        if (!isServiceEnabled) {
            Button(onClick = onOpenAccessibilitySettings) { Text("Enable accessibility service") }
        } else {
            Button(onClick = onCaptureScreenshot) { Text("Capture screenshot") }
            Button(onClick = onScrollBoxDown) { Text("Scroll box down") }
        }
        if (statusText.isNotEmpty()) {
            Text(text = statusText)
        }
    }
}

@Preview
@Composable
private fun ShinyAppPreview() {
    ShinyApp()
}
