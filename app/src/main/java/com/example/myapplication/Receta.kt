package com.example.myapplication


data class Receta(
    val idReceta: String = "",
    val idPaciente: String = "",
    val idDoctor: String = "",
    val fecha: String = "",
    val diagnostico: String = "",
    val medicamentos: List<String> = listOf(),
    val recomendaciones: String = ""
)
