package com.example.logistic_app.data.model

import com.google.firebase.Timestamp

data class EmergencyReport(
    val id: String = "",
    val dispatchId: String = "",
    val type: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val location: EmergencyLocation = EmergencyLocation(),
    val reportedBy: String = "",
    val timestamp: Timestamp? = null
)

data class EmergencyLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val label: String = ""
)
