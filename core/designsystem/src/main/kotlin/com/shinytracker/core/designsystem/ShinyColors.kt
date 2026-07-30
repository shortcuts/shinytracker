package com.shinytracker.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val ShinyBg = Color(0xFF10151A)
val ShinySurface = Color(0xFF1A2229)
val ShinyText = Color(0xFFEAF2F0)
val ShinyAccent = Color(0xFF64D8CB)
val ShinyError = Color(0xFFE5484D)

val ShinyDarkColorScheme =
    darkColorScheme(
        primary = ShinyAccent,
        onPrimary = ShinyBg,
        background = ShinyBg,
        onBackground = ShinyText,
        surface = ShinySurface,
        onSurface = ShinyText,
        error = ShinyError,
        onError = ShinyText,
    )
