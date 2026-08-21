package com.example.mviexample.shared.data.network

import kotlinx.serialization.Serializable

@Serializable
data class OperacionDto(
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val imagenUrl: String? = null,
    val tipo: String? = null,
    val autor: String? = null,
    val fechaCreacion: Long = 0L,
    val guardada: Boolean = false,
)
