package com.example.logistic_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logistic_app.ui.components.MapPlaceholder
import com.example.logistic_app.ui.theme.NavyBlue
import com.example.logistic_app.ui.theme.SurfaceWhite
import com.example.logistic_app.ui.theme.TextPrimary
import com.example.logistic_app.ui.viewmodel.AuthViewModel

@Composable
fun MapFullScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val activeDispatch by authViewModel.activeDispatch.collectAsState()
    var snapTrigger by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        MapPlaceholder(
            modifier = Modifier.fillMaxSize(),
            text = activeDispatch?.location?.label ?: "Destination",
            latitude = activeDispatch?.location?.lat ?: 14.5995,
            longitude = activeDispatch?.location?.lng ?: 120.9842,
            showUserLocation = true,
            snapToUserLocation = snapTrigger,
            onMapClick = null // No-op on full screen click
        )

        // Top Bar Overlay
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            color = SurfaceWhite.copy(alpha = 0.9f),
            shape = CircleShape,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = activeDispatch?.location?.label ?: "Full Map View",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { snapTrigger++ },
                    modifier = Modifier.background(NavyBlue.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Rounded.MyLocation, contentDescription = "Locate Me", tint = NavyBlue)
                }
            }
        }
    }
}
