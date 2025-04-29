package com.example.myapplication

import com.google.firebase.Timestamp

enum class AppointmentAction { CONFIRM, CANCEL }

data class Appointment(
    val id: String = "",
    val patientName: String = "",
    val doctorName: String = "",
    val dateTime: Timestamp = Timestamp.now(),
    val status: String = ""
)
