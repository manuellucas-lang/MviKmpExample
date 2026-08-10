package com.example.mviexample.shared.data.model

data class Post(
    val id: Long,
    val userId: Long,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val authorName: String? = null,
    val mine: Boolean = false,
    val saved: Boolean = false,
)
