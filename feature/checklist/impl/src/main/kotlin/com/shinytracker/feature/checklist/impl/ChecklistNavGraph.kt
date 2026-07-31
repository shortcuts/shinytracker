package com.shinytracker.feature.checklist.impl

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shinytracker.feature.checklist.api.ChecklistRoute

fun NavGraphBuilder.checklistNavGraph(
    onExportProfile: (Uri) -> Unit,
    onToggleWidget: () -> Unit,
    isWidgetRunning: Boolean,
) {
    composable(ChecklistRoute.OWNER) {
        ChecklistScreen(
            onExportProfile = onExportProfile,
            onToggleWidget = onToggleWidget,
            isWidgetRunning = isWidgetRunning,
        )
    }
    composable(
        route = ChecklistRoute.SHARED_PATTERN,
        arguments = listOf(navArgument(ChecklistRoute.SHARED_ARG_KEY) { type = NavType.StringType }),
    ) {
        SharedProfileScreen(ownerLabel = "a friend")
    }
}
