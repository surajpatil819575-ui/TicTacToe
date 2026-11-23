package com.example.tictactoe.core.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tictactoe.core.AppViewModel
import com.example.tictactoe.core.game.GameMode
import com.example.tictactoe.feature.auth.LoginScreen
import com.example.tictactoe.feature.dashboard.DashboardScreen
import com.example.tictactoe.feature.profile.ProfileScreen
import com.example.tictactoe.games.tictactoe.TicTacToeScreen

@Composable
fun AppNavHost(appVm: AppViewModel, dark: Boolean) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onContinue = {
                    nav.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }   // back dabane par login pe wapas nahi
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                dark = dark,
                onToggleTheme = { appVm.toggleTheme(dark) },
                onOpenProfile = { nav.navigate(Routes.PROFILE) },
                onPlay = { gameId, mode -> nav.navigate(Routes.game(gameId, mode)) }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(onBack = { nav.popBackStack() })
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) { entry ->
            val gameId = entry.arguments?.getString("gameId")
            val mode = GameMode.valueOf(
                entry.arguments?.getString("mode") ?: GameMode.LOCAL_2P.name
            )
            when (gameId) {
                "tictactoe" -> TicTacToeScreen(
                    mode = mode,
                    dark = dark,
                    onToggleTheme = { appVm.toggleTheme(dark) },
                    onExit = { nav.popBackStack() }
                )
                else -> Text("Coming soon")
            }
        }
    }
}