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
    
    private var currentPath: String? = null

    /**
     * Updated to handle both Array fields (Dispatches) and Sub-collections (Emergencies)
     */
    fun startListening(parentCollection: String, docId: String, subCollection: String = "messages") {
        if (docId.isBlank()) return
        
        val fullPath = "$parentCollection/$docId/$subCollection"
        if (currentPath == fullPath) return
        
        stopListening()
        currentPath = fullPath
        
        Log.d("ChatViewModel", "Listening to: $fullPath")

        if (parentCollection == "dispatches") {
            // WEB COMPATIBILITY: Use 'dispatchChat' array field for dispatches
            messagesListener = db.collection("dispatches")
                .document(docId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && snapshot.exists()) {
                        val rawList = snapshot.get("dispatchChat") as? List<Map<String, Any>>
                        val msgs = rawList?.mapIndexed { index, map ->
                            ChatMessage(
                                id = "${snapshot.id}_$index",
                                senderId = map["senderId"] as? String ?: "",
                                senderName = map["senderName"] as? String ?: "",
                                text = map["text"] as? String ?: "",
                                timestamp = map["timestamp"] as? Timestamp,
                                isAdmin = map["isAdmin"] as? Boolean ?: false
                            )
                        }?.sortedBy { it.timestamp } ?: emptyList()
                        _messages.value = msgs
                    }
                }
        } else {
            // Standard sub-collection for EmergencyReports or others
            messagesListener = db.collection(parentCollection)
                .document(docId)
                .collection(subCollection)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val msgs = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                        }
                        _messages.value = msgs
                    }
                }
        }
    }

    fun sendMessage(parentCollection: String, docId: String, senderId: String, senderName: String, text: String, subCollection: String = "messages") {
        if (text.isBlank() || docId.isBlank()) return
        
        val messageData = hashMapOf(
            "senderId" to senderId,
            "senderName" to senderName,
            "text" to text.trim(),
            "timestamp" to Timestamp.now(),
            "isAdmin" to false
        )
        
        viewModelScope.launch {
            try {
                if (parentCollection == "dispatches") {
                    // WEB COMPATIBILITY: Use arrayUnion on 'dispatchChat'
                    db.collection("dispatches")
                        .document(docId)
                        .update(
                            "dispatchChat", FieldValue.arrayUnion(messageData),
                            "lastChatAt", FieldValue.serverTimestamp()
                        )
                } else {
                    // Standard sub-collection add
                    db.collection(parentCollection)
                        .document(docId)
                        .collection(subCollection)
                        .add(messageData)
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message: ${e.message}")
            }
        }
    }

    fun stopListening() {
        messagesListener?.remove()
        messagesListener = null
        currentPath = null
        _messages.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
