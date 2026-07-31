package com.example.mviexample.shared.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class PostsApi(
    private val client: HttpClient = createHttpClient(),
) {

    suspend fun getPosts(): List<PostDto> =
        client.get("$BASE_URL/posts").body()

    private companion object {
        const val BASE_URL = "https://jsonplaceholder.typicode.com"
    }
}
