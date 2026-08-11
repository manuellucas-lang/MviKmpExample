package com.example.mviexample.shared.data

import com.example.mviexample.shared.data.model.Operacion

data class OperacionesResult(
    val operaciones: List<Operacion>,
    val fromCache: Boolean,
)

interface OperacionesRepository {
    suspend fun getOperaciones(forceRefresh: Boolean): OperacionesResult

    suspend fun crearOperacion(
        titulo: String,
        descripcion: String,
        imagenUrl: String?,
        tipo: String?,
        autor: String?,
    ): Operacion

    suspend fun actualizarOperacion(
        id: Long,
        titulo: String,
        descripcion: String,
        imagenUrl: String?,
        tipo: String?,
        autor: String?,
    ): Operacion

    suspend fun borrarOperacion(id: Long)

    suspend fun setOperacionGuardada(id: Long, guardada: Boolean)
}
