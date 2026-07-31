package com.example.mviexample.shared.data.network

import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val id: Long,
    val userId: Long,
    val title: String,
    val body: String,
)
