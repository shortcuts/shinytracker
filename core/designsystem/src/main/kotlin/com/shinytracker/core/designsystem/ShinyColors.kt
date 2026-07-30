package com.shinytracker.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Values chosen for perceptual consistency across schemes (same teal hue, tuned lightness/chroma
// per surface) and checked against WCAG 4.5:1 for body text pairs.

val ShinyDarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF5FD9CB),
        onPrimary = Color(0xFF06211D),
        primaryContainer = Color(0xFF12413B),
        onPrimaryContainer = Color(0xFF8FEDE0),
        secondaryContainer = Color(0xFF2B3B33),
        onSecondaryContainer = Color(0xFFCDE9DD),
        background = Color(0xFF12181E),
        onBackground = Color(0xFFECF2F1),
        surface = Color(0xFF1B242C),
        onSurface = Color(0xFFECF2F1),
        surfaceVariant = Color(0xFF263039),
        onSurfaceVariant = Color(0xFFB9C4C6),
        error = Color(0xFFFF6B67),
        onError = Color(0xFF3A0906),
    )

val ShinyLightColorScheme =
    lightColorScheme(
        primary = Color(0xFF128A7C),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFC8F3EC),
        onPrimaryContainer = Color(0xFF04332C),
        secondaryContainer = Color(0xFFDCEEE6),
        onSecondaryContainer = Color(0xFF1B3A30),
        background = Color(0xFFF7FAF9),
        onBackground = Color(0xFF12181E),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF12181E),
        surfaceVariant = Color(0xFFE4EAE8),
        onSurfaceVariant = Color(0xFF414B4A),
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF),
    )
