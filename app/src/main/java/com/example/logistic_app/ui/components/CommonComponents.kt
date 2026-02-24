package com.example.logistic_app.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.logistic_app.R
import com.example.logistic_app.ui.theme.*

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "App Logo",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
fun MapPlaceholder(modifier: Modifier = Modifier, text: String = "Map View Placeholder") {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFECEFF1))
            .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Icon(
                Icons.Rounded.Map,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun PhotoUploadBox(
    modifier: Modifier = Modifier, 
    label: String = "Upload Photo",
    selectedImageUri: Uri? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
            .border(2.dp, if (selectedImageUri != null) NavyBlue else Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (selectedImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(selectedImageUri),
                contentDescription = "Selected Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Overlay to show it's still clickable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AddAPhoto, contentDescription = null, tint = Color.White)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = NavyBlue.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = NavyBlue
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(label, color = TextPrimary, fontSize = 14.sp)
                Text("Tap to capture or upload", color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}
