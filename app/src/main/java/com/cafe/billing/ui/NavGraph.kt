package com.cafe.billing.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cafe.billing.ui.bill.BillScreen
import com.cafe.billing.ui.history.SalesHistoryScreen
import com.cafe.billing.ui.menu.MenuManagementScreen
import com.cafe.billing.ui.order.OrderScreen

// ============================================================
// NAVIGATION
// Defines all screen routes and wires them together.
// Each screen is a composable destination in the NavHost.
// ============================================================

/** Sealed class of all route strings — avoids typos */
sealed class Screen(val route: String) {
    object Menu    : Screen("menu")
    object Order   : Screen("order")
    object Bill    : Screen("bill/{orderId}") {
        /** Build the navigation route with the actual orderId */
        fun createRoute(orderId: Int) = "bill/$orderId"
    }
    object History : Screen("history")
}

/**
 * The root NavHost that connects all screens.
 * Place this inside MainActivity's setContent{}.
 */
@Composable
fun CafeNavGraph(navController: NavHostController) {
    NavHost(
        navController  = navController,
        startDestination = Screen.Order.route     // App opens on the Order screen
    ) {

        // ── Order screen (Home) ──────────────────────────
        composable(Screen.Order.route) {
            OrderScreen(
                onNavigateToBill = { orderId ->
                    navController.navigate(Screen.Bill.createRoute(orderId))
                },
                onNavigateToMenu = {
                    navController.navigate(Screen.Menu.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        // ── Menu management screen ───────────────────────
        composable(Screen.Menu.route) {
            MenuManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Bill screen (receives orderId from navigation) ──
        composable(
            route = Screen.Bill.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            BillScreen(
                orderId = orderId,
                onNavigateBack = { navController.popBackStack() },
                onNewOrder = {
                    // Go back to Order screen and clear the back stack
                    navController.navigate(Screen.Order.route) {
                        popUpTo(Screen.Order.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Sales history screen ─────────────────────────
        composable(Screen.History.route) {
            SalesHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
