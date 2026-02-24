package com.example.logistic_app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logistic_app.data.model.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var messagesListener: ListenerRegistration? = null
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private var currentDispatchId: String? = null

    fun startListening(dispatchId: String) {
        if (dispatchId.isBlank()) {
            Log.e("ChatViewModel", "Cannot start listening: dispatchId is blank")
            return
        }
        
        if (currentDispatchId == dispatchId) return
        
        stopListening()
        currentDispatchId = dispatchId
        Log.d("ChatViewModel", "Starting listener for dispatch document ID: $dispatchId")
        
        messagesListener = db.collection("dispatches")
            .document(dispatchId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Firestore Listen Error: ${error.message}", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e("ChatViewModel", "Error deserializing message: ${doc.id}", e)
                            null
                        }
                    }
                    Log.d("ChatViewModel", "Messages updated. Count: ${msgs.size}")
                    _messages.value = msgs
                }
            }
    }

    fun sendMessage(dispatchId: String, senderId: String, senderName: String, text: String) {
        if (text.isBlank() || dispatchId.isBlank()) {
            Log.e("ChatViewModel", "Send failed: text or dispatchId is blank")
            return
        }
        
        // We use a Map to ensure proper serialization and use server-side timestamp
        val messageData = hashMapOf(
            "senderId" to senderId,
            "senderName" to senderName,
            "text" to text,
            "timestamp" to FieldValue.serverTimestamp(),
            "isAdmin" to false
        )
        
        Log.d("ChatViewModel", "Attempting to send message to: dispatches/$dispatchId/messages")
        
        viewModelScope.launch {
            try {
                db.collection("dispatches")
                    .document(dispatchId)
                    .collection("messages")
                    .add(messageData)
                    .addOnSuccessListener { docRef ->
                        Log.d("ChatViewModel", "SUCCESS: Message added with ID: ${docRef.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatViewModel", "FAILURE: Could not add message: ${e.message}", e)
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "EXCEPTION during send: ${e.message}", e)
            }
        }
    }

    fun stopListening() {
        messagesListener?.remove()
        messagesListener = null
        currentDispatchId = null
        _messages.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
