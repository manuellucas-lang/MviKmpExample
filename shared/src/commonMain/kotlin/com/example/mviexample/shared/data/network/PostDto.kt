package com.example.mviexample.shared.data.network

import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val id: Long,
    val title: String,
    val description: String,
    val thumbnail: String? = null,
    val brand: String? = null,
    val category: String? = null,
)

@Serializable
data class PostsResponse(
    val products: List<PostDto>,
)
