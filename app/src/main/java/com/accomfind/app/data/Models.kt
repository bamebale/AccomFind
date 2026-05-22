package com.accomfind.app.data

data class Accommodation(
    val id: Int = 0,
    val title: String,
    val price: Double,
    val location: String,
    val type: String,
    val amenities: String,
    val availabilityDate: String,
    val deposit: Double,
    val imageResName: String,
    val status: String = "Available",
    val distance: String = ""
)

data class User(
    val id: Int = 0,
    val username: String,
    val password: String,
    val fullName: String,
    val studentId: String
)

data class Reservation(
    val id: Int = 0,
    val accomId: Int,
    val userId: Int,
    val referenceNumber: String,
    val amountPaid: Double,
    val reservedDate: String
)

data class ChatMessage(
    val id: Int = 0,
    val userId: Int,
    val accomId: Int,
    val sender: String,
    val content: String,
    val timestamp: String
)
