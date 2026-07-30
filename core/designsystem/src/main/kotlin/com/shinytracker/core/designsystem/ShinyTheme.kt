package com.shinytracker.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val ShinyShapes =
    Shapes(
        medium = RoundedCornerShape(12.dp),
    )

@Composable
fun ShinyTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) ShinyDarkColorScheme else ShinyLightColorScheme
    MaterialTheme(colorScheme = colorScheme, shapes = ShinyShapes) {
        Surface(color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
            content()
        }
    }
}
