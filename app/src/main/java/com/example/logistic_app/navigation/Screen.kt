package com.example.logistic_app.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dispatch : Screen("dispatch")
    object DeliveryConfirmation : Screen("delivery_confirmation")
    object Profile : Screen("profile")
    object Emergency : Screen("emergency")
    object Chat : Screen("chat")
}
