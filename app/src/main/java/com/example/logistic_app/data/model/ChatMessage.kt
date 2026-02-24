package com.example.logistic_app.data.model

import com.google.firebase.Timestamp

/**
 * ChatMessage model for Firestore.
 * 
 * WEB INTEGRATION NOTES:
 * 1. Collection Path: dispatches/{dispatchId}/messages
 * 2. The web admin panel should listen to this sub-collection for real-time updates.
 * 3. When the web admin sends a message, set 'senderId' to 'admin' or the admin's UID.
 * 4. Use 'serverTimestamp()' for the 'timestamp' field on the web side.
 */
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val timestamp: Timestamp? = null,
    val isAdmin: Boolean = false
)
