package com.AnkiAppAndroid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.AnkiAppAndroid.ui.screens.CardsScreen
import com.AnkiAppAndroid.ui.screens.EditBaralhoScreen
import com.AnkiAppAndroid.ui.screens.HomeScreen
import com.AnkiAppAndroid.ui.screens.LocationScreen
import com.AnkiAppAndroid.ui.viewmodel.BaralhoViewModel
import com.AnkiAppAndroid.ui.viewmodel.LocalViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    baralhoViewModel: BaralhoViewModel,
    localViewModel: LocalViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = baralhoViewModel,
                onBaralhoClick = { baralhoId ->
                    navController.navigate(Screen.CardsScreen.createRoute(baralhoId))
                },
                onEditClick = { baralhoId ->
                    navController.navigate(Screen.EditBaralhoScreen.createRoute(baralhoId))
                },
                onLocationClick = {
                    navController.navigate(Screen.LocationScreen.route)
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
                viewModel = baralhoViewModel
            )
        }

        composable(
            route = Screen.EditBaralhoScreen.route,
            arguments = listOf(
                navArgument("baralhoId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val baralhoId = backStackEntry.arguments?.getLong("baralhoId") ?: 0L
            EditBaralhoScreen(
                baralhoId = baralhoId,
                navController = navController,
                viewModel = baralhoViewModel
            )
        }

        composable(route = Screen.LocationScreen.route) {
            LocationScreen(
                navController = navController,
                viewModel = localViewModel
            )
        }
    }
}