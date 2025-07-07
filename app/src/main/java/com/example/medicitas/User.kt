package com.example.medicitas

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val dni: String = "",
    val role: String = "user", // "admin", "doctor", "user"
    val specialty: String = "", // Solo para doctores
    val avatarUrl: String = ""
)