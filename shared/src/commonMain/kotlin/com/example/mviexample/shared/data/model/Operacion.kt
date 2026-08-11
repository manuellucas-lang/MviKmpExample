package com.example.mviexample.shared.data.model

data class Operacion(
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val imagenUrl: String? = null,
    val tipo: String? = null,
    val autor: String? = null,
    val propia: Boolean = false,
    val guardada: Boolean = false,
)
