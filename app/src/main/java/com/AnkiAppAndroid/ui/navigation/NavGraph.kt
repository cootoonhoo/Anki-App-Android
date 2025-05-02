package com.AnkiAppAndroid.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CardsScreen : Screen("cards/{baralhoId}") {
        fun createRoute(baralhoId: Long): String = "cards/$baralhoId"
    }
}