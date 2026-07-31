package com.example.mviexample.shared.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class PostsApi(
    private val client: HttpClient = createHttpClient(),
) {

    suspend fun getPosts(): List<PostDto> =
        client
            .get("$BASE_URL/products") {
                parameter("limit", 0)
                parameter("select", "id,title,description,thumbnail,brand,category")
            }
            .body<PostsResponse>()
            .products

    suspend fun createPost(request: NewPostRequest): PostDto =
        client.post("$BASE_URL/products/add") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updatePost(id: Long, request: UpdatePostRequest): PostDto =
        client.put("$BASE_URL/products/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deletePost(id: Long) {
        client.delete("$BASE_URL/products/$id")
    }

    private companion object {
        const val BASE_URL = "https://dummyjson.com"
    }
}
