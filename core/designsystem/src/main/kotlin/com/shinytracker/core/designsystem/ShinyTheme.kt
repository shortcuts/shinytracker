package com.shinytracker.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ShinyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ShinyDarkColorScheme,
        content = content,
    )
}
