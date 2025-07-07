package com.example.medicitas

data class Reservation(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val dni: String = "",
    val phone: String = "",
    val doctorId: String = "",
    val specialist: String = "",
    val reason: String = "",
    val dateTime: Long = 0L,
    val status: String = "pending" // pending, confirmed, cancelled
)