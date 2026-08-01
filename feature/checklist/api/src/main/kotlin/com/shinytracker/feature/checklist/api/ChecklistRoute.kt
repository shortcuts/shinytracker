package com.shinytracker.feature.checklist.api

import android.net.Uri

/**
 * Navigation contract for the checklist feature, plus the app-shell routes (Settings) the
 * drawer it hosts needs to reference across module boundaries.
 */
object ChecklistRoute {
    private const val SHARED_ARG = "profileUri"

    const val OWNER = "checklist"
    const val SETTINGS = "settings"
    const val SHARED_ARG_KEY = SHARED_ARG
    const val SHARED_PATTERN = "checklist/shared/{$SHARED_ARG}"

    fun sharedRoute(profileUri: Uri): String = "checklist/shared/${Uri.encode(profileUri.toString())}"

    /** Parses the shared-profile nav arg back into a Uri; null if missing or malformed. */
    fun parseSharedProfileArg(encoded: String?): Uri? = encoded?.let { runCatching { Uri.parse(Uri.decode(it)) }.getOrNull() }
}
