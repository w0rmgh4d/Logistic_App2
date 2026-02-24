package com.example.logistic_app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logistic_app.ui.components.MapPlaceholder
import com.example.logistic_app.ui.theme.NavyBlue
import com.example.logistic_app.ui.theme.SurfaceWhite
import com.example.logistic_app.ui.theme.TextPrimary
import com.example.logistic_app.ui.viewmodel.AuthViewModel
import com.example.logistic_app.ui.viewmodel.EmergencyViewModel
import org.osmdroid.util.GeoPoint

@Composable
fun MapFullScreen(
    authViewModel: AuthViewModel,
    emergencyViewModel: EmergencyViewModel,
    isPicking: Boolean = false,
    onBack: () -> Unit
) {
    val activeDispatch by authViewModel.activeDispatch.collectAsState()
    var snapTrigger by remember { mutableIntStateOf(0) }
    
    var pickedLocation by remember { 
        mutableStateOf(GeoPoint(emergencyViewModel.selectedLat, emergencyViewModel.selectedLng)) 
    }

    val defaultLat = 14.5995
    val defaultLng = 120.9842

    Box(modifier = Modifier.fillMaxSize()) {
        MapPlaceholder(
            modifier = Modifier.fillMaxSize(),
            text = if (isPicking) "" else (activeDispatch?.location?.label ?: "Destination"),
            latitude = if (isPicking) pickedLocation.latitude else (activeDispatch?.location?.lat ?: defaultLat),
            longitude = if (isPicking) pickedLocation.longitude else (activeDispatch?.location?.lng ?: defaultLng),
            showUserLocation = true,
            snapToUserLocation = snapTrigger,
            useRedMarker = !isPicking,
            onMapClick = null,
            onCenterChanged = if (isPicking) { newCenter ->
                pickedLocation = newCenter
            } else null
        )

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
                    text = if (isPicking) "Adjust Emergency Pinpoint" else (activeDispatch?.location?.label ?: "Map View"),
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

        if (isPicking) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).offset(y = (-24).dp),
                        colorFilter = ColorFilter.tint(Color.Red)
                    )
                }
            }

            Button(
                onClick = {
                    emergencyViewModel.onLocationConfirmed(
                        pickedLocation.latitude,
                        pickedLocation.longitude,
                        "Custom Emergency Location"
                    )
                    onBack()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .height(56.dp)
                    .width(220.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CONFIRM THIS POINT", fontWeight = FontWeight.Bold)
            }
            
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Move the map to align pinpoint",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
