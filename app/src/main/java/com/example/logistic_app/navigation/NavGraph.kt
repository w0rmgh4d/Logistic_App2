package com.example.logistic_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
    val user by authViewModel.user.collectAsState()
    val activeDispatch by authViewModel.activeDispatch.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = if (user != null) Screen.Dispatch.route else Screen.Login.route,
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
                },
                onStopOver = {
                    navController.navigate(Screen.StopOver.route)
                },
                onReportDelay = {
                    navController.navigate(Screen.ReportDelay.route)
                },
                onExpandMap = {
                    navController.navigate(Screen.MapFullScreen.route + "/false")
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
        composable(Screen.StopOver.route) {
            StopOverScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSubmit = {
                    navController.navigate(Screen.Dispatch.route) {
                        popUpTo(Screen.Dispatch.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ReportDelay.route) {
            ReportDelayScreen(
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
                    navController.navigate("emergency_chat/${emergencyViewModel.lastReportId}") {
                        popUpTo(Screen.Dispatch.route) { inclusive = false }
                    }
                },
                onExpandMap = {
                    navController.navigate(Screen.MapFullScreen.route + "/true")
                }
            )
        }

        composable("emergency_chat/{reportId}") { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId")
            ChatScreen(
                authViewModel = authViewModel,
                parentCollection = "EmergencyReports",
                documentId = reportId,
                onBack = {
                    navController.navigate(Screen.Dispatch.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                authViewModel = authViewModel,
                parentCollection = "dispatches",
                documentId = activeDispatch?.id,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MapFullScreen.route + "/{isPicking}",
            arguments = listOf(navArgument("isPicking") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isPicking = backStackEntry.arguments?.getBoolean("isPicking") ?: false
            MapFullScreen(
                authViewModel = authViewModel,
                emergencyViewModel = emergencyViewModel,
                isPicking = isPicking,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
