package com.shinytracker.feature.checklist.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.feature.checklist.api.ChecklistRoute
import kotlinx.coroutines.launch

@Composable
fun ChecklistDrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
) {
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalDrawerSheet {
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = currentRoute == ChecklistRoute.OWNER,
            onClick = {
                navController.navigate(ChecklistRoute.OWNER) {
                    popUpTo(ChecklistRoute.OWNER) { inclusive = true }
                    launchSingleTop = true
                }
                scope.launch { drawerState.close() }
            },
        )
    }
}

@Preview
@Composable
private fun ChecklistDrawerContentPreview() {
    ShinyTheme {
        ChecklistDrawerContent(
            navController = rememberNavController(),
            drawerState = rememberDrawerState(initialValue = DrawerValue.Open),
        )
    }
}
