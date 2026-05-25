package com.playzone.admin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.playzone.admin.ui.screens.auth.AdminLoginScreen
import com.playzone.admin.ui.screens.checkout.CheckoutScreen
import com.playzone.admin.ui.screens.dashboard.AdminMainScreen

sealed class AdminScreen(val route: String) {
    object Login      : AdminScreen("login")
    object Main       : AdminScreen("main")             // host bottom nav
    object Checkout   : AdminScreen("checkout/{bookingId}") {
        fun createRoute(id: String) = "checkout/$id"
    }
}

@Composable
fun AdminNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AdminScreen.Login.route) {

        composable(AdminScreen.Login.route) {
            AdminLoginScreen(
                onLoginSuccess = {
                    navController.navigate(AdminScreen.Main.route) {
                        popUpTo(AdminScreen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AdminScreen.Main.route) {
            AdminMainScreen(
                onLogout = {
                    navController.navigate(AdminScreen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoCheckout = { bookingId ->
                    navController.navigate(AdminScreen.Checkout.createRoute(bookingId))
                }
            )
        }

        composable(
            AdminScreen.Checkout.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { back ->
            CheckoutScreen(
                bookingId = back.arguments?.getString("bookingId") ?: "",
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.navigate(AdminScreen.Main.route) {
                        popUpTo(AdminScreen.Main.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
