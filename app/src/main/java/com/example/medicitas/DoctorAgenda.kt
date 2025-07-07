package com.example.medicitas

data class DoctorAgenda(
    val doctorId: String = "",
    val specialty: String = "",
    val availableSlots: List<Long> = emptyList() // Almacenamos timestamps de los slots disponibles
)