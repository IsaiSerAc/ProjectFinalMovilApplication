package com.example.myapplication

data class Cita(
    val id: String = "",
    val idDoctor: String = "",
    val idPaciente: String = "",
    val pacienteNombre: String = "",   // ← aquí lo añades
    val fecha: String = "",
    val hora: String = "",
    val motivo: String = "",
    var estado: String = ""            // "pendiente", "atendida", etc.
)
