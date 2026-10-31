package com.example.serviciosocialapp.model

data class RegistroHora(
    val _id: String?,
    val fecha: String,
    val cantidadHoras: Int,
    val descripcionActividad: String,
    val institucion: String,
    val estudianteId: String
)