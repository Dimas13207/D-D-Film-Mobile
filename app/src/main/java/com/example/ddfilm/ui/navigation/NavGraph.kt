package com.example.ddfilm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ddfilm.ui.screens.DetailsScreen
import com.example.ddfilm.ui.screens.HomeScreen
import com.example.ddfilm.ui.screens.PlayerScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Search : Screen("search")
    object Transactions : Screen("transactions")
    object TopUp : Screen("topup")
    object Settings : Screen("settings")
    object Details : Screen("details/{titleId}") {
        fun createRoute(titleId: Int) = "details/$titleId"
    }
    object Player : Screen("player/{episodeId}") {
        fun createRoute(episodeId: Int) = "player/$episodeId"
    }
}

@Composable
fun NavGraph(navController: NavHostController, sessionManager: com.example.ddfilm.data.api.SessionManager) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            com.example.ddfilm.ui.screens.LoginScreen(
                sessionManager = sessionManager,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            com.example.ddfilm.ui.screens.RegisterScreen(
                sessionManager = sessionManager,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                sessionManager = sessionManager,
                onTitleClick = { titleId ->
                    navController.navigate(Screen.Details.createRoute(titleId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            com.example.ddfilm.ui.screens.ProfileScreen(
                sessionManager = sessionManager,
                onBackClick = { navController.popBackStack() },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onTransactionHistoryClick = {
                    navController.navigate(Screen.Transactions.route)
                },
                onTopUpClick = {
                    navController.navigate(Screen.TopUp.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Transactions.route) {
            com.example.ddfilm.ui.screens.TransactionHistoryScreen(
                sessionManager = sessionManager,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.TopUp.route) {
            com.example.ddfilm.ui.screens.TopUpScreen(
                sessionManager = sessionManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            com.example.ddfilm.ui.screens.SettingsScreen(
                sessionManager = sessionManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            com.example.ddfilm.ui.screens.SearchScreen(
                onTitleClick = { titleId ->
                    navController.navigate(Screen.Details.createRoute(titleId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Details.route) { backStackEntry ->
            val titleId = backStackEntry.arguments?.getString("titleId")?.toIntOrNull()
            if (titleId != null) {
                DetailsScreen(
                    titleId = titleId,
                    sessionManager = sessionManager,
                    onBackClick = { navController.popBackStack() },
                    onEpisodeClick = { episodeId ->
                        navController.navigate(Screen.Player.createRoute(episodeId))
                    }
                )
            }
        }

        composable(Screen.Player.route) { backStackEntry ->
            val episodeId = backStackEntry.arguments?.getString("episodeId")?.toIntOrNull()
            if (episodeId != null) {
                PlayerScreen(
                    episodeId = episodeId,
                    sessionManager = sessionManager,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
