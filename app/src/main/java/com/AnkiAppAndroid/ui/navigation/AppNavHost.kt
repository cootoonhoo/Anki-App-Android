package com.AnkiAppAndroid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.AnkiAppAndroid.ui.screens.CardsScreen
import com.AnkiAppAndroid.ui.screens.HomeScreen
import com.AnkiAppAndroid.ui.viewmodel.BaralhoViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: BaralhoViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onBaralhoClick = { baralhoId ->
                    navController.navigate(Screen.CardsScreen.createRoute(baralhoId))
                }
            )
        }

        composable(
            route = Screen.CardsScreen.route,
            arguments = listOf(
                navArgument("baralhoId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val baralhoId = backStackEntry.arguments?.getLong("baralhoId") ?: 0L
            CardsScreen(
                baralhoId = baralhoId,
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}