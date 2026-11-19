package com.vezhny.cookdiary.ui

import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vezhny.cookdiary.R
import com.vezhny.cookdiary.ui.dishes.DishEditScreen
import com.vezhny.cookdiary.ui.dishes.DishesScreen
import com.vezhny.cookdiary.ui.history.HistoryScreen
import com.vezhny.cookdiary.ui.home.HomeScreen
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable object HomeRoute
@Serializable object DishesRoute
@Serializable object HistoryRoute
@Serializable data class DishEditRoute(val dishId: Long = 0)

private data class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    @StringRes val label: Int,
    val icon: ImageVector,
)

@Suppress("DEPRECATION") // Icons.Filled.List: AutoMirrored variant lives in material-icons-extended.
private val topLevelDestinations = listOf(
    TopLevelDestination(HomeRoute, HomeRoute::class, R.string.nav_home, Icons.Filled.Home),
    TopLevelDestination(DishesRoute, DishesRoute::class, R.string.nav_dishes, Icons.Filled.List),
    TopLevelDestination(HistoryRoute, HistoryRoute::class, R.string.nav_history, Icons.Filled.DateRange),
)

@Composable
fun CookDiaryApp() {
    val navController = rememberNavController()
    val backStackEntryState = navController.currentBackStackEntryAsState()
    val backStackEntry by backStackEntryState
    val currentDestination = backStackEntry?.destination
    val showBottomBar = topLevelDestinations.any { currentDestination?.hasRoute(it.routeClass) == true }

    fun navigateTopLevel(route: Any) = navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute(dest.routeClass) == true,
                            onClick = { navigateTopLevel(dest.route) },
                            icon = { Icon(dest.icon, contentDescription = null) },
                            label = { Text(stringResource(dest.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(padding),
            // Short cross-fade instead of the default 700 ms one: while it runs the old screen
            // ignores taps (see InputOnlyWhenCurrent), so keep that window small.
            enterTransition = { fadeIn(tween(TRANSITION_MILLIS)) },
            exitTransition = { fadeOut(tween(TRANSITION_MILLIS)) },
        ) {
            screen<HomeRoute>(backStackEntryState) {
                HomeScreen(onAddDish = { navController.navigate(DishEditRoute()) })
            }
            screen<DishesRoute>(backStackEntryState) {
                DishesScreen(onEditDish = { id -> navController.navigate(DishEditRoute(id)) })
            }
            screen<DishEditRoute>(backStackEntryState) { entry ->
                DishEditScreen(
                    dishId = entry.toRoute<DishEditRoute>().dishId,
                    onDone = { navController.popBackStack() },
                )
            }
            screen<HistoryRoute>(backStackEntryState) { HistoryScreen(onChooseDish = { navigateTopLevel(HomeRoute) }) }
        }
    }
}

private const val TRANSITION_MILLIS = 150

/** A destination whose screen accepts touches only while it is the current one; see [InputOnlyWhenCurrent]. */
private inline fun <reified T : Any> NavGraphBuilder.screen(
    currentEntry: State<NavBackStackEntry?>,
    noinline content: @Composable (NavBackStackEntry) -> Unit,
) = composable<T> { entry ->
    InputOnlyWhenCurrent(isCurrent = entry.id == currentEntry.value?.id) { content(entry) }
}

/**
 * After navigating, the outgoing screen stays composed on top of the incoming one until the
 * transition ends (a few frames even with no animation) and would get the touches. A quick tap
 * meant for the new screen then lands on the old one, e.g. "What should I cook?" right after
 * switching tabs opened the dish whose row was underneath. A non-current screen is covered by a
 * layer that swallows all touches: a tap in that short window is lost rather than misdirected.
 */
@Composable
private fun InputOnlyWhenCurrent(isCurrent: Boolean, content: @Composable () -> Unit) {
    Box {
        content()
        if (!isCurrent) {
            Box(
                Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                        }
                    },
            )
        }
    }
}
