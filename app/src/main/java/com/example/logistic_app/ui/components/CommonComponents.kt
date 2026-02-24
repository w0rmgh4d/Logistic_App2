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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.rememberAsyncImagePainter
import com.example.logistic_app.R
import com.example.logistic_app.ui.theme.*
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

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
fun MapPlaceholder(
    modifier: Modifier = Modifier,
    text: String = "Map View Placeholder",
    latitude: Double = 14.5995, // Default to Manila
    longitude: Double = 120.9842,
    showUserLocation: Boolean = false,
    snapToUserLocation: Int = 0,
    onMapClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Set up location overlay
    val locationOverlay = remember(mapView) {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            setDrawAccuracyEnabled(true)
        }
    }

    // Set up compass overlay
    val compassOverlay = remember(mapView) {
        CompassOverlay(context, InternalCompassOrientationProvider(context), mapView).apply {
            enableCompass()
        }
    }

    // Set up rotation overlay
    val rotationOverlay = remember(mapView) {
        RotationGestureOverlay(mapView).apply {
            isEnabled = true
        }
    }

    // Set up click listener overlay
    val eventsOverlay = remember(onMapClick) {
        MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                onMapClick?.invoke()
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        })
    }

    // Marker for destination
    val destinationMarker = remember(mapView) {
        Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            // Use default marker icon or a specific one if available
        }
    }

    // Snap to user location trigger
    LaunchedEffect(snapToUserLocation) {
        if (snapToUserLocation > 0 && showUserLocation) {
            val myLocation = locationOverlay.myLocation
            if (myLocation != null) {
                mapView.controller.animateTo(myLocation)
                mapView.controller.setZoom(18.0)
            } else {
                // If location not yet found, wait for first fix
                locationOverlay.runOnFirstFix {
                    val location = locationOverlay.myLocation
                    if (location != null) {
                        mapView.post {
                            mapView.controller.animateTo(location)
                            mapView.controller.setZoom(18.0)
                        }
                    }
                }
            }
        }
    }

    // Handle Lifecycle events for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    if (showUserLocation) {
                        locationOverlay.enableMyLocation()
                    }
                    compassOverlay.enableCompass()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapView.onPause()
                    locationOverlay.disableMyLocation()
                    compassOverlay.disableCompass()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFECEFF1))
            .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(16.dp))
            .let { 
                if (onMapClick != null) it.clickable { onMapClick() } else it
            },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = {
                mapView.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(latitude, longitude))

                    overlays.add(compassOverlay)
                    overlays.add(rotationOverlay)

                    if (showUserLocation) {
                        overlays.add(locationOverlay)
                    }

                    if (onMapClick != null) {
                        overlays.add(eventsOverlay)
                    }
                    
                    // Add the destination marker
                    overlays.add(destinationMarker)
                }
            },
            update = { view ->
                // Update marker position and text
                destinationMarker.position = GeoPoint(latitude, longitude)
                destinationMarker.title = text

                if (snapToUserLocation == 0) {
                    view.controller.setCenter(GeoPoint(latitude, longitude))
                }

                if (showUserLocation && !view.overlays.contains(locationOverlay)) {
                    view.overlays.add(locationOverlay)
                    locationOverlay.enableMyLocation()
                } else if (!showUserLocation && view.overlays.contains(locationOverlay)) {
                    view.overlays.remove(locationOverlay)
                    locationOverlay.disableMyLocation()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
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
