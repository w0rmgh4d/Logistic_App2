package com.example.logistic_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.logistic_app.ui.screens.*
import com.example.logistic_app.ui.viewmodel.AuthViewModel
import com.example.logistic_app.ui.viewmodel.EmergencyViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    emergencyViewModel: EmergencyViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dispatch.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dispatch.route) {
            DispatchScreen(
                authViewModel = authViewModel,
                onConfirmDelivery = {
                    navController.navigate(Screen.DeliveryConfirmation.route)
                },
                onContactSupport = {
                    navController.navigate(Screen.Chat.route)
                }
            )
        }
        composable(Screen.DeliveryConfirmation.route) {
            DeliveryConfirmationScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSubmit = {
                    navController.navigate(Screen.Dispatch.route) {
                        popUpTo(Screen.Dispatch.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Emergency.route) {
            EmergencyScreen(
                authViewModel = authViewModel,
                emergencyViewModel = emergencyViewModel,
                onBack = { navController.popBackStack() },
                onForwardToChat = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Emergency.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Chat.route) {
            ChatScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
