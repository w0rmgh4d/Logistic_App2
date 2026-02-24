package com.example.logistic_app.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logistic_app.data.model.Dispatch
import com.example.logistic_app.data.model.Personnel
import com.example.logistic_app.utils.CloudinaryHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var dispatchListener: ListenerRegistration? = null

    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _personnel = MutableStateFlow<Personnel?>(null)
    val personnel: StateFlow<Personnel?> = _personnel.asStateFlow()

    private val _activeDispatch = MutableStateFlow<Dispatch?>(null)
    val activeDispatch: StateFlow<Dispatch?> = _activeDispatch.asStateFlow()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        viewModelScope.launch {
            _user.collect { currentUser ->
                if (currentUser != null) {
                    fetchPersonnelData(currentUser.email ?: "")
                } else {
                    clearSession()
                }
            }
        }

        viewModelScope.launch {
            _personnel.collect { p ->
                if (p != null) {
                    startDispatchListener(p)
                }
            }
        }
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _user.value = auth.currentUser
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Authentication failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchPersonnelData(email: String) {
        viewModelScope.launch {
            try {
                var snapshot = db.collection("personnelAccount")
                    .whereEqualTo("email", email)
                    .get()
                    .await()
                
                if (snapshot.isEmpty) {
                    snapshot = db.collection("personnelAccount")
                        .whereEqualTo("email", email.lowercase())
                        .get()
                        .await()
                }

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents.first()
                    _personnel.value = doc.toObject(Personnel::class.java)?.copy(id = doc.id)
                } else {
                    _error.value = "Personnel account not found"
                }
            } catch (e: Exception) {
                _error.value = "Error loading personnel: ${e.message}"
            }
        }
    }

    private fun startDispatchListener(p: Personnel) {
        dispatchListener?.remove()
        val personnelName = "[${p.rank}] ${p.lastName}, ${p.firstName}"
        
        dispatchListener = db.collection("dispatches")
            .whereEqualTo("personnels", personnelName)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _error.value = "Dispatch Listener Error: ${error.message}"
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    val activeOnes = snapshot.documents
                        .mapNotNull { it.toObject(Dispatch::class.java)?.copy(id = it.id) }
                        .filter { it.status != "Delivered" }
                        .sortedByDescending { it.createdAt }
                    
                    _activeDispatch.value = activeOnes.firstOrNull()
                } else {
                    _activeDispatch.value = null
                }
            }
    }

    fun updateDispatchStatus(status: String) {
        val dispatchId = _activeDispatch.value?.id ?: return
        viewModelScope.launch {
            try {
                db.collection("dispatches").document(dispatchId)
                    .update("status", status)
                    .await()
            } catch (e: Exception) {
                _error.value = "Failed to update status: ${e.message}"
            }
        }
    }

    fun confirmDelivery(
        receiverName: String,
        arrivalTime: String,
        imageUri: Uri?,
        onSuccess: () -> Unit
    ) {
        val dispatchId = _activeDispatch.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                var imageUrl = ""
                if (imageUri != null) {
                    imageUrl = CloudinaryHelper.uploadImage(imageUri)
                }

                val updates = hashMapOf(
                    "status" to "Delivered",
                    "receiverName" to receiverName,
                    "arrivalTime" to arrivalTime,
                    "proofOfDelivery" to imageUrl
                )

                db.collection("dispatches").document(dispatchId)
                    .update(updates as Map<String, Any>)
                    .await()
                
                _activeDispatch.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Delivery confirmation failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun clearSession() {
        dispatchListener?.remove()
        _personnel.value = null
        _activeDispatch.value = null
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
    }

    override fun onCleared() {
        super.onCleared()
        dispatchListener?.remove()
    }
}
