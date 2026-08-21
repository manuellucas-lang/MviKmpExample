package com.example.mviexample.shared.data

import com.example.mviexample.shared.data.model.Operacion
import com.example.mviexample.shared.data.network.CreateOperacionRequest
import com.example.mviexample.shared.data.network.OperacionDto
import com.example.mviexample.shared.data.network.OperacionesApi
import com.example.mviexample.shared.data.network.UpdateOperacionRequest
import com.example.mviexample.shared.database.AppDatabase
import com.example.mviexample.shared.database.Operaciones
import com.example.mviexample.shared.util.currentTimeMillis

class OperacionesRepositoryImpl(
    private val api: OperacionesApi,
    private val database: AppDatabase,
) : OperacionesRepository {

    private val queries = database.operacionQueries
    private val metaQueries = database.metaQueries

    override suspend fun getOperaciones(forceRefresh: Boolean): OperacionesResult {
        resetLegacySavedFlagsIfNeeded()
        val cached = queries.selectAll().executeAsList().map { it.toModel() }
        val lastSync = metaQueries.selectById(KEY_LAST_SYNC).executeAsOneOrNull()?.value_?.toLongOrNull()
        val isFresh = lastSync != null && currentTimeMillis() - lastSync < CACHE_TTL_MILLIS
        if (!forceRefresh && cached.isNotEmpty() && isFresh) {
            return OperacionesResult(cached, fromCache = true)
        }
        return try {
            val remote = api.getOperaciones().map { it.toModel() }
            queries.transaction {
                remote.forEach { operacion -> operacion.upsert() }
                metaQueries.upsert(KEY_LAST_SYNC, currentTimeMillis().toString())
            }
            val all = queries.selectAll().executeAsList().map { it.toModel() }
            OperacionesResult(all, fromCache = false)
        } catch (e: Exception) {
            if (cached.isNotEmpty()) {
                OperacionesResult(cached, fromCache = true)
            } else {
                throw e
            }
        }
    }

    override suspend fun crearOperacion(
        titulo: String,
        descripcion: String,
        imagenUrl: String?,
        tipo: String?,
        autor: String?,
    ): Operacion {
        val remote = api.createOperacion(
            CreateOperacionRequest(
                titulo = titulo,
                descripcion = descripcion,
                imagenUrl = imagenUrl.normalized(),
                tipo = tipo,
                autor = autor ?: "You",
            ),
        )
        val operacion = remote.toModel().copy(autor = autor ?: "You")
        operacion.upsert()
        queries.setPropia(propia = 1L, id = operacion.id)
        return operacion.copy(propia = true)
    }

    override suspend fun actualizarOperacion(
        id: Long,
        titulo: String,
        descripcion: String,
        imagenUrl: String?,
        tipo: String?,
        autor: String?,
    ): Operacion {
        api.updateOperacion(
            id,
            UpdateOperacionRequest(
                titulo = titulo,
                descripcion = descripcion,
                imagenUrl = imagenUrl.normalized(),
                tipo = tipo,
                autor = autor ?: "You",
            ),
        )
        val existing = queries.selectById(id).executeAsOneOrNull()
        val operacion = Operacion(
            id = id,
            titulo = titulo,
            descripcion = descripcion,
            imagenUrl = imagenUrl.normalized(),
            tipo = tipo,
            autor = autor ?: existing?.autor ?: "You",
            propia = existing?.propia == 1L,
            guardada = existing?.guardada == 1L,
        )
        operacion.upsert()
        return operacion
    }

    override suspend fun borrarOperacion(id: Long) {
        runCatching { api.deleteOperacion(id) }
        queries.deleteById(id)
    }

    override suspend fun setOperacionGuardada(id: Long, guardada: Boolean) {
        if (guardada) {
            api.comprarOperacion(id)
        }
        queries.setGuardada(guardada = if (guardada) 1L else 0L, id = id)
    }

    private fun resetLegacySavedFlagsIfNeeded() {
        val migrated = metaQueries.selectById(KEY_GPAY_MIGRATION).executeAsOneOrNull()?.value_
        if (migrated == null) {
            queries.resetAllGuardadas()
            metaQueries.upsert(KEY_GPAY_MIGRATION, currentTimeMillis().toString())
        }
    }

    private fun Operacion.upsert() {
        queries.insertOrReplace(
            id = id,
            titulo = titulo,
            descripcion = descripcion,
            imagen_url = imagenUrl,
            tipo = tipo,
            autor = autor,
            propia = if (propia) 1L else 0L,
            guardada = if (guardada) 1L else 0L,
        )
    }
}

private fun String?.normalized(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

private const val CACHE_TTL_MILLIS = 60L * 60L * 1000L
private const val KEY_LAST_SYNC = "last_sync_at"
private const val KEY_GPAY_MIGRATION = "gpay_purchases_reset_v1"

private fun Operaciones.toModel() =
    Operacion(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        imagenUrl = imagen_url,
        tipo = tipo,
        autor = autor,
        propia = propia == 1L,
        guardada = guardada == 1L,
    )

private fun OperacionDto.toModel() =
    Operacion(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        imagenUrl = imagenUrl.normalized(),
        tipo = tipo,
        autor = autor,
        guardada = guardada,
    )
