package com.example.mviexample.shared.data.network

import kotlinx.serialization.Serializable

@Serializable
data class NewPostRequest(
    val title: String,
    val description: String,
)

@Serializable
data class UpdatePostRequest(
    val title: String,
    val description: String,
)
