package com.example.myapplication

data class Receta(
    val idReceta: String = "",
    val idPaciente: String = "",
    val nombrePaciente: String = "",    // ← Añadido
    val idDoctor: String = "",
    val nombreDoctor: String = "",      // ← Añadido
    val fecha: String = "",
    val diagnostico: String = "",
    val medicamentos: List<String> = listOf(),
    val recomendaciones: String = ""
)

