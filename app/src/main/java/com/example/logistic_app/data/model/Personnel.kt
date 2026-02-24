package com.example.logistic_app.data.model

data class Personnel(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val middleInitial: String = "",
    val rank: String = "",
    val position: String = "",
    val contactNo: String = "",
    val email: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val role: String = "officer"
) {
    val fullName: String
        get() = if (middleInitial.isNotBlank()) "$firstName $middleInitial. $lastName" else "$firstName $lastName"
    
    val initials: String
        get() = "${firstName.take(1)}${lastName.take(1)}".uppercase()
}
