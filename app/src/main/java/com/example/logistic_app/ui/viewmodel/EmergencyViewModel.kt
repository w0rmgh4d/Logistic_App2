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

    var lastReportId by mutableStateOf<String?>(null)
        private set

    // New location state
    var selectedLat by mutableStateOf(9.7489) // Default mock
    var selectedLng by mutableStateOf(118.7471) // Default mock
    var selectedLabel by mutableStateOf("Detected Location")

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

    fun onLocationConfirmed(lat: Double, lng: Double, label: String) {
        selectedLat = lat
        selectedLng = lng
        selectedLabel = label
    }

    fun onBack() {
        if (currentStep == EmergencyStep.DETAILS) {
            currentStep = EmergencyStep.SELECTION
            error = null
        }
    }

    /**
     * TRANSMIT EMERGENCY
     */
    fun transmitEmergency(dispatchId: String, userId: String, userName: String) {
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
                var uploadedImageUrl = ""
                selectedImageUri?.let {
                    uploadedImageUrl = CloudinaryHelper.uploadImage(it)
                }

                val report = EmergencyReport(
                    dispatchId = dispatchId,
                    type = selectedType,
                    description = description,
                    imageUrl = uploadedImageUrl,
                    location = EmergencyLocation(selectedLat, selectedLng, selectedLabel),
                    reportedBy = userName,
                    timestamp = Timestamp.now()
                )

                val reportDoc = db.collection("EmergencyReports").add(report).await()
                lastReportId = reportDoc.id

                val alertText = "🚨 EMERGENCY SIGNAL: $selectedType\nDescription: $description"
                val alertMessage = hashMapOf(
                    "senderId" to userId,
                    "senderName" to userName,
                    "text" to alertText,
                    "imageUrl" to uploadedImageUrl,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "isAdmin" to false
                )

                db.collection("dispatches")
                    .document(dispatchId)
                    .collection("messages")
                    .add(alertMessage)

                currentStep = EmergencyStep.SUCCESS
            } catch (e: Exception) {
                Log.e("EmergencyViewModel", "Failed to transmit emergency", e)
                error = "Transmission failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Check if the last report is resolved and clear lastReportId if it is.
     * This allows the user to create a new report instead of being locked in the old chat.
     */
    fun checkAndClearResolvedReport(onChecked: (Boolean) -> Unit) {
        val reportId = lastReportId ?: run {
            onChecked(false)
            return
        }

        viewModelScope.launch {
            try {
                val doc = db.collection("EmergencyReports").document(reportId).get().await()
                val status = doc.getString("status")?.lowercase() ?: "active"
                if (status == "resolved") {
                    lastReportId = null
                    onChecked(true)
                } else {
                    onChecked(false)
                }
            } catch (e: Exception) {
                onChecked(false)
            }
        }
    }

    /**
     * Resets the ViewModel to the selection screen.
     */
    fun resetToSelection() {
        currentStep = EmergencyStep.SELECTION
        selectedType = ""
        description = ""
        selectedImageUri = null
        error = null
        isLoading = false
    }

    fun clearLastReport() {
        lastReportId = null
    }

    fun reset() {
        currentStep = EmergencyStep.SELECTION
        selectedType = ""
        description = ""
        selectedImageUri = null
        error = null
        isLoading = false
        lastReportId = null
        selectedLat = 9.7489
        selectedLng = 118.7471
        selectedLabel = "Detected Location"
    }
}
