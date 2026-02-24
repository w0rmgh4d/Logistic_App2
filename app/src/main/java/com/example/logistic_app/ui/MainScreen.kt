package com.example.logistic_app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.logistic_app.navigation.NavGraph
import com.example.logistic_app.navigation.Screen
import com.example.logistic_app.ui.theme.EmergencyRed
import com.example.logistic_app.ui.theme.LightGrayBackground
import com.example.logistic_app.ui.theme.NavyBlue
import com.example.logistic_app.ui.viewmodel.AuthViewModel
import com.example.logistic_app.ui.viewmodel.EmergencyViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val authViewModel: AuthViewModel = viewModel()
    val emergencyViewModel: EmergencyViewModel = viewModel()

    val user by authViewModel.user.collectAsState()
    val activeDispatch by authViewModel.activeDispatch.collectAsState()

    // Show bottom bar only on main screens and if user is logged in
    val isMainScreen = (currentRoute == Screen.Dispatch.route || currentRoute == Screen.Profile.route) && user != null
    
    // SOS button only appears on main screens when there is an active dispatch and it is accepted (not Pending)
    val showSosButton = isMainScreen && activeDispatch != null && activeDispatch?.status != "Pending"

    Scaffold(
        containerColor = LightGrayBackground,
        bottomBar = {
            if (isMainScreen) {
                Surface(
                    color = NavyBlue,
                    tonalElevation = 0.dp
                ) {
                    BottomAppBar(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NavigationItem(
                                icon = Icons.Rounded.LocalShipping,
                                label = "Dispatch",
                                isSelected = currentRoute == Screen.Dispatch.route,
                                onClick = { 
                                    if (currentRoute != Screen.Dispatch.route) {
                                        navController.navigate(Screen.Dispatch.route) {
                                            popUpTo(Screen.Dispatch.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(64.dp))

                            NavigationItem(
                                icon = Icons.Rounded.Person,
                                label = "Profile",
                                isSelected = currentRoute == Screen.Profile.route,
                                onClick = { 
                                    if (currentRoute != Screen.Profile.route) {
                                        navController.navigate(Screen.Profile.route) {
                                            popUpTo(Screen.Dispatch.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (showSosButton) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.Emergency.route) },
                    containerColor = EmergencyRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                    modifier = Modifier
                        .size(72.dp)
                        .offset(y = 54.dp)
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = "Emergency",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            authViewModel = authViewModel,
            emergencyViewModel = emergencyViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun NavigationItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(26.dp)
        )
        Text(
            label,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
