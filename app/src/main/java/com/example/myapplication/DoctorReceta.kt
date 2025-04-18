package com.example.myapplication

data class DoctorReceta(
    val idReceta: String = "",
    val idPaciente: String = "",
    val fecha: String = "",
    val diagnostico: String = "",
    val medicamentos: List<String> = listOf(),
    val recomendaciones: String = ""
)
