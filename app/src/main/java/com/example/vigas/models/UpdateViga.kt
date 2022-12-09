package com.example.vigas.models

data class UpdateViga(
    val clvViga: Int,
    val numEmpleado: Int,
    val clvDefecto: Int?,
    val clvProceso: Int
)