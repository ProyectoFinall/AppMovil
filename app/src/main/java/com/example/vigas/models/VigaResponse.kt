package com.example.vigas.models

data class VigaResponse(
    val clvViga: Int,
    val clvObra: Int,
    val pesoViga: Int,
    val largoViga: Int,
    val material: String,
    val fechaViga: String,
    val numEmpleado: Int,
    val clvDefecto: Int?,
    val clvProceso: Int?
)