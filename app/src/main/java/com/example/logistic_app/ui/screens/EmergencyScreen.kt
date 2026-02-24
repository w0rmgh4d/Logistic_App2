package com.example.logistic_app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logistic_app.ui.components.MapPlaceholder
import com.example.logistic_app.ui.components.PhotoUploadBox
import com.example.logistic_app.ui.theme.*
import com.example.logistic_app.ui.viewmodel.AuthViewModel
import com.example.logistic_app.ui.viewmodel.EmergencyStep
import com.example.logistic_app.ui.viewmodel.EmergencyViewModel

@Composable
fun EmergencyScreen(
    authViewModel: AuthViewModel,
    emergencyViewModel: EmergencyViewModel,
    onBack: () -> Unit,
    onForwardToChat: () -> Unit
) {
    val activeDispatch by authViewModel.activeDispatch.collectAsState()
    val personnel by authViewModel.personnel.collectAsState()
    val user by authViewModel.user.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        emergencyViewModel.onImageSelected(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
    ) {
        AnimatedContent(
            targetState = emergencyViewModel.currentStep,
            transitionSpec = {
                if (targetState == EmergencyStep.SUCCESS) {
                    fadeIn() togetherWith fadeOut()
                } else {
                    (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                }
            },
            label = "EmergencyStepAnimation"
        ) { step ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (step) {
                    EmergencyStep.SELECTION -> {
                        EmergencySelection(
                            onSelect = { emergencyViewModel.onTypeSelected(it) },
                            onClose = onBack
                        )
                    }
                    EmergencyStep.DETAILS -> {
                        EmergencyDetails(
                            type = emergencyViewModel.selectedType,
                            description = emergencyViewModel.description,
                            onDescriptionChange = { emergencyViewModel.onDescriptionChange(it) },
                            onReport = { 
                                val dispatchId = activeDispatch?.id ?: ""
                                val userId = user?.uid ?: ""
                                val userName = personnel?.fullName ?: user?.displayName ?: "Personnel"
                                
                                emergencyViewModel.transmitEmergency(
                                    dispatchId = dispatchId,
                                    userId = userId,
                                    userName = userName
                                )
                            },
                            onBack = { emergencyViewModel.onBack() },
                            isLoading = emergencyViewModel.isLoading,
                            errorMessage = emergencyViewModel.error,
                            selectedImageUri = emergencyViewModel.selectedImageUri,
                            onUploadClick = { launcher.launch("image/*") }
                        )
                    }
                    EmergencyStep.SUCCESS -> {
                        EmergencySuccess(
                            onDone = {
                                emergencyViewModel.reset()
                                onBack()
                            },
                            onChatWithSupport = {
                                emergencyViewModel.reset()
                                onForwardToChat()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencySelection(onSelect: (String) -> Unit, onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "EMERGENCY REPORT",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Select the situation that best describes your status",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        EmergencyOption(
            title = "Vehicle Breakdown",
            subtitle = "Mechanical failure or accident",
            icon = Icons.Rounded.ReportProblem,
            color = StopOverYellow,
            onClick = { onSelect("Vehicle Breakdown") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        EmergencyOption(
            title = "Non-Military Encounter",
            subtitle = "Checkpoints or unauthorized stops",
            icon = Icons.Rounded.Warning,
            color = ReportedOrange,
            onClick = { onSelect("Non-Military Encounter") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        EmergencyOption(
            title = "T.I.C.",
            subtitle = "Troops In Contact / Active Combat",
            icon = Icons.Rounded.Warning,
            color = EmergencyRed,
            onClick = { onSelect("T.I.C.") }
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(64.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun EmergencyDetails(
    type: String,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onReport: () -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    selectedImageUri: android.net.Uri?,
    onUploadClick: () -> Unit
) {
    val isTic = type == "T.I.C."
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = !isLoading) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                "Report Details",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    type.uppercase(),
                    color = EmergencyRed,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Detected Location (Auto-Detect)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                MapPlaceholder(modifier = Modifier.height(120.dp), text = "Emergency Coordinates: 9.7489° N, 118.7471° E")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    if (isTic) "Situation Description (Optional)" else "Situation Description", 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    placeholder = { Text("Describe the emergency...", color = TextDisabled) },
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NavyBlue,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    if (isTic) "Visual Evidence (Optional)" else "Visual Evidence", 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                PhotoUploadBox(
                    modifier = Modifier.height(120.dp),
                    selectedImageUri = selectedImageUri,
                    onClick = onUploadClick
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onReport, 
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), 
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("TRANSMIT EMERGENCY SIGNAL", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun EmergencySuccess(onDone: () -> Unit, onChatWithSupport: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = DeliveredGreen.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.CheckCircle, 
                    contentDescription = null, 
                    tint = DeliveredGreen, 
                    modifier = Modifier.size(80.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Signal Transmitted", 
            fontSize = 28.sp, 
            fontWeight = FontWeight.Black, 
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            "Response team has been notified.\nMaintain radio silence if possible.", 
            color = Color.White.copy(alpha = 0.7f), 
            modifier = Modifier.padding(top = 12.dp),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onChatWithSupport, 
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), 
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue, contentColor = Color.White)
        ) {
            Text("CHAT WITH SUPPORT", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onDone, 
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), 
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent, contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Text("RETURN TO DASHBOARD", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmergencyOption(
    title: String, 
    subtitle: String,
    icon: ImageVector,
    color: Color, 
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.Warning, contentDescription = null, tint = color.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
        }
    }
}
