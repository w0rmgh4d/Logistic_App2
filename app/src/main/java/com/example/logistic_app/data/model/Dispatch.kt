package com.example.logistic_app.data.model

import com.google.firebase.Timestamp

data class Dispatch(
    val id: String = "",
    val dispatchId: String = "",
    val location: DispatchLocation = DispatchLocation(),
    val officer: String = "",
    val personnels: String = "",
    val truck: String = "",
    val supplies: List<SupplyItem> = emptyList(),
    val othersNote: String = "",
    val status: String = "Pending",
    val createdAt: Timestamp? = null,
    val dispatchChat: List<ChatMessage> = emptyList(),
    val lastChatAt: Timestamp? = null,
    // WEB ADMIN LIVE TRACKING: This field holds the real-time coordinates of the personnel
    val CurrentLocation: PersonnelLocation? = null
)

data class DispatchLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val label: String = ""
)

data class PersonnelLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val updatedAt: Timestamp? = null
)

data class SupplyItem(
    val category: String = "",
    val item: String = "",
    val quantity: Long = 0L
)
