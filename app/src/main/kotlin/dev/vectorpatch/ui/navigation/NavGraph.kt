package dev.vectorpatch.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import dev.vectorpatch.ui.screens.*

sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home     : Screen("home",     "Home",     Icons.Filled.Home,      Icons.Outlined.Home)
    object Modules  : Screen("modules",  "Modules",  Icons.Filled.Extension, Icons.Outlined.Extension)
    object Apps     : Screen("apps",     "Apps",     Icons.Filled.Apps,      Icons.Outlined.Apps)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings,  Icons.Outlined.Settings)
}

private val bottomNavItems = listOf(Screen.Home, Screen.Modules, Screen.Apps, Screen.Settings)

@Composable
fun VectorPatchNavGraph() {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentDest   = navBackStack?.destination
    val showBottom    = bottomNavItems.any { it.route == currentDest?.route }

    Scaffold(
        modifier  = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottom) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDest?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon    = { Icon(if (selected) screen.selectedIcon else screen.unselectedIcon, screen.label) },
                            label   = { Text(screen.label) },
                            selected = selected,
                            onClick  = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            enterTransition  = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(280)) + fadeIn(tween(280)) },
            exitTransition   = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(280)) + fadeOut(tween(280)) },
            popEnterTransition  = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(280)) + fadeIn(tween(280)) },
            popExitTransition   = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(280)) + fadeOut(tween(280)) },
        ) {
            composable(Screen.Home.route)     { HomeScreen(paddingValues = padding, navController = navController) }
            composable(Screen.Modules.route)  { ModulesScreen(paddingValues = padding) }
            composable(Screen.Apps.route)     { AppsScreen(paddingValues = padding, onPatchApp = { pkg -> navController.navigate("patch/$pkg") }) }
            composable(Screen.Settings.route) { SettingsScreen(paddingValues = padding) }
            composable("patch/{packageName}") { back ->
                PatchScreen(
                    packageName    = back.arguments?.getString("packageName") ?: "",
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
