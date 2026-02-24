package com.example.logistic_app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.logistic_app.data.model.Dispatch
import com.example.logistic_app.ui.components.MapPlaceholder
import com.example.logistic_app.ui.theme.*
import com.example.logistic_app.ui.viewmodel.AuthViewModel

@Composable
fun DispatchScreen(
    authViewModel: AuthViewModel,
    onConfirmDelivery: () -> Unit,
    onContactSupport: () -> Unit,
    onStopOver: () -> Unit,
    onReportDelay: () -> Unit,
    onExpandMap: () -> Unit
) {
    val user by authViewModel.user.collectAsState()
    val personnel by authViewModel.personnel.collectAsState()
    val activeDispatch by authViewModel.activeDispatch.collectAsState()
    val context = LocalContext.current
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var snapTrigger by remember { mutableIntStateOf(0) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasLocationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    val displayName = personnel?.fullName ?: user?.displayName ?: user?.email?.substringBefore("@")?.split(".", "_", "-")?.joinToString(" ") { 
        it.replaceFirstChar { char -> char.uppercase() } 
    } ?: "User"
    
    val initials = personnel?.initials ?: displayName.split(" ").filter { it.isNotEmpty() }.take(2).map { it.first().uppercase() }.joinToString("")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGrayBackground)
            .padding(16.dp)
    ) {
        // User Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            color = Color.Transparent
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NavyBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Text(
                        "${personnel?.rank ?: "Active Driver"} • ${personnel?.username ?: user?.uid?.take(7)?.uppercase() ?: "DRV-001"}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                
                if (activeDispatch != null && activeDispatch?.status != "Pending") {
                    IconButton(
                        onClick = onContactSupport,
                        modifier = Modifier
                            .background(NavyBlue.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = "Support", tint = NavyBlue)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(
                    onClick = { snapTrigger++ },
                    modifier = Modifier
                        .background(NavyBlue.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Rounded.MyLocation, contentDescription = "Locate Me", tint = NavyBlue)
                }
            }
        }

        if (activeDispatch == null) {
            Text(
                "NO ACTIVE DISPATCH",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
            MapPlaceholder(
                modifier = Modifier.weight(1f),
                text = "Waiting for admin dispatch...",
                showUserLocation = hasLocationPermission,
                snapToUserLocation = snapTrigger,
                onMapClick = onExpandMap
            )
        } else if (activeDispatch?.status == "Pending") {
            DispatchConfirmation(
                dispatch = activeDispatch!!,
                onAccept = { authViewModel.updateDispatchStatus("Ongoing") }
            )
        } else {
            ActiveDispatchContent(
                dispatch = activeDispatch!!,
                onConfirmDelivery = onConfirmDelivery,
                onStopOver = onStopOver,
                onReportDelay = onReportDelay,
                onResumeDispatch = { authViewModel.resumeDispatch() },
                hasLocationPermission = hasLocationPermission,
                snapTrigger = snapTrigger,
                onExpandMap = onExpandMap
            )
        }
    }
}

@Composable
fun DispatchConfirmation(
    dispatch: Dispatch,
    onAccept: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = OngoingBlueLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.LocalShipping, contentDescription = null, tint = OngoingBlue, modifier = Modifier.size(32.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "New Dispatch Assigned",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Text(
                    "Dispatch ID: ${dispatch.dispatchId}",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoRow(label = "Destination", value = dispatch.location.label)
                    InfoRow(label = "Truck", value = dispatch.truck)
                    InfoRow(label = "Supplies", value = "${dispatch.supplies.size} items listed")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ACCEPT DISPATCH", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.End)
    }
}

@Composable
fun ColumnScope.ActiveDispatchContent(
    dispatch: Dispatch,
    onConfirmDelivery: () -> Unit,
    onStopOver: () -> Unit,
    onReportDelay: () -> Unit,
    onResumeDispatch: () -> Unit,
    hasLocationPermission: Boolean,
    snapTrigger: Int,
    onExpandMap: () -> Unit
) {
    val isPaused = dispatch.status == "Stop Over" || dispatch.status == "Delayed"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = when(dispatch.status) {
                            "Stop Over" -> StopOverYellowLight
                            "Delayed" -> ReportedOrangeLight
                            else -> OngoingBlueLight
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.LocalShipping,
                                contentDescription = null,
                                tint = when(dispatch.status) {
                                    "Stop Over" -> StopOverYellow
                                    "Delayed" -> ReportedOrange
                                    else -> OngoingBlue
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Current Dispatch",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }
                Surface(
                    color = when(dispatch.status) {
                        "Ongoing" -> OngoingBlue
                        "Stop Over" -> StopOverYellow
                        "Delayed" -> ReportedOrange
                        else -> ReportedOrange
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        dispatch.status.uppercase(),
                        color = if (dispatch.status == "Stop Over") Color.Black else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Dispatch ID", color = TextSecondary, fontSize = 12.sp)
                    Text(dispatch.dispatchId, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Truck Unit", color = TextSecondary, fontSize = 12.sp)
                    Text(dispatch.truck, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // RESUME BUTTON (Appears when status is Stop Over or Delayed)
            if (isPaused) {
                Button(
                    onClick = onResumeDispatch,
                    modifier = Modifier.fillMaxWidth().height(44.dp).padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeliveredGreen)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESUME DISPATCH", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EnhancedActionChip(
                    label = "Delivered",
                    color = DeliveredGreen,
                    bgColor = DeliveredGreenLight,
                    onClick = onConfirmDelivery,
                    modifier = Modifier.weight(1f),
                    enabled = !isPaused // Disable delivery if paused
                )
                EnhancedActionChip(
                    label = "Stop Over",
                    color = StopOverYellow,
                    bgColor = StopOverYellowLight,
                    onClick = onStopOver,
                    modifier = Modifier.weight(1f),
                    enabled = !isPaused // Disable Stop Over if already paused
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            EnhancedActionChip(
                label = "Report Delay",
                color = ReportedOrange,
                bgColor = ReportedOrangeLight,
                onClick = onReportDelay,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isPaused // Disable Delay if already paused
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Navigation,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = TextSecondary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("TARGET: ${dispatch.location.label.uppercase()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
    }

    MapPlaceholder(
        modifier = Modifier.weight(1f),
        text = dispatch.location.label,
        latitude = dispatch.location.lat,
        longitude = dispatch.location.lng,
        showUserLocation = hasLocationPermission,
        snapToUserLocation = snapTrigger,
        onMapClick = onExpandMap
    )
}

@Composable
fun EnhancedActionChip(
    label: String,
    color: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true // Added enabled parameter
) {
    Surface(
        onClick = { if (enabled) onClick() },
        color = if (enabled) bgColor else Color.LightGray.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(44.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (enabled) color else Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) color else Color.Gray
                )
            }
        }
    }
}
