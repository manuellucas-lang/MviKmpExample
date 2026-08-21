package com.example.mviexample.shared.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class OperacionesApi(
    private val client: HttpClient = createHttpClient(),
) {

    suspend fun getOperaciones(): List<OperacionDto> =
        client.get("$BASE_URL/operaciones").body()

    suspend fun createOperacion(request: CreateOperacionRequest): OperacionDto =
        client.post("$BASE_URL/operaciones") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateOperacion(id: Long, request: UpdateOperacionRequest): OperacionDto =
        client.put("$BASE_URL/operaciones/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun comprarOperacion(id: Long): OperacionDto =
        client.post("$BASE_URL/operaciones/$id/purchase").body()

    suspend fun deleteOperacion(id: Long) {
        client.delete("$BASE_URL/operaciones/$id")
    }

    private companion object {
        val BASE_URL = apiBaseUrl()
    }
}
