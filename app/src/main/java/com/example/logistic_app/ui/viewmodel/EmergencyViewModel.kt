package com.example.logistic_app.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logistic_app.data.model.EmergencyLocation
import com.example.logistic_app.data.model.EmergencyReport
import com.example.logistic_app.utils.CloudinaryHelper
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class EmergencyStep { SELECTION, DETAILS, SUCCESS }

class EmergencyViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var currentStep by mutableStateOf(EmergencyStep.SELECTION)
        private set

    var selectedType by mutableStateOf("")
        private set

    var description by mutableStateOf("")
        private set

    var selectedImageUri by mutableStateOf<Uri?>(null)
    
    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun onTypeSelected(type: String) {
        selectedType = type
        currentStep = EmergencyStep.DETAILS
    }

    fun onDescriptionChange(newDescription: String) {
        description = newDescription
    }

    fun onImageSelected(uri: Uri?) {
        selectedImageUri = uri
    }

    fun onBack() {
        if (currentStep == EmergencyStep.DETAILS) {
            currentStep = EmergencyStep.SELECTION
            error = null
        }
    }

    fun transmitEmergency(dispatchId: String, userId: String, userName: String) {
        // Validation logic
        if (selectedType != "T.I.C.") {
            if (description.isBlank()) {
                error = "Description is required for this emergency type."
                return
            }
            if (selectedImageUri == null) {
                error = "Photo evidence is required for this emergency type."
                return
            }
        }

        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                // 1. Upload Image if exists
                var uploadedImageUrl = ""
                selectedImageUri?.let {
                    uploadedImageUrl = CloudinaryHelper.uploadImage(it)
                }

                // 2. Mock GPS Data
                // TODO: Integrate FusedLocationProviderClient here to get real-time GPS
                val mockLat = 9.7489
                val mockLng = 118.7471
                val mockLabel = "PPC-NORTH-HQ"

                val report = EmergencyReport(
                    dispatchId = dispatchId,
                    type = selectedType,
                    description = description,
                    imageUrl = uploadedImageUrl,
                    location = EmergencyLocation(mockLat, mockLng, mockLabel),
                    reportedBy = userName,
                    timestamp = Timestamp.now()
                )

                // 3. Save to EmergencyReports collection
                db.collection("EmergencyReports").add(report).await()

                // 4. Send summary to Support Chat
                val summaryText = """
                    🚨 EMERGENCY REPORTED: $selectedType
                    Location: $mockLabel ($mockLat, $mockLng)
                    Description: ${if (description.isBlank()) "No details provided." else description}
                """.trimIndent()

                val chatMessageData = hashMapOf(
                    "senderId" to userId,
                    "senderName" to userName,
                    "text" to summaryText,
                    "imageUrl" to uploadedImageUrl, // Attach the same image to the chat
                    "timestamp" to FieldValue.serverTimestamp(),
                    "isAdmin" to false
                )

                db.collection("dispatches")
                    .document(dispatchId)
                    .collection("messages")
                    .add(chatMessageData)
                    .await()

                currentStep = EmergencyStep.SUCCESS
            } catch (e: Exception) {
                Log.e("EmergencyViewModel", "Failed to transmit emergency", e)
                error = "Transmission failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun reset() {
        currentStep = EmergencyStep.SELECTION
        selectedType = ""
        description = ""
        selectedImageUri = null
        error = null
        isLoading = false
    }
}
