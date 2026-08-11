package com.example.mviexample.shared.data.network

import kotlinx.serialization.Serializable

@Serializable
data class CreateOperacionRequest(
    val titulo: String,
    val descripcion: String,
    val imagenUrl: String? = null,
    val tipo: String? = null,
    val autor: String? = null,
)

@Serializable
data class UpdateOperacionRequest(
    val titulo: String,
    val descripcion: String,
    val imagenUrl: String? = null,
    val tipo: String? = null,
    val autor: String? = null,
)
