package com.example.mviexample.shared.data

import com.example.mviexample.shared.data.model.Post

data class PostsResult(
    val posts: List<Post>,
    val fromCache: Boolean,
)

interface PostsRepository {
    suspend fun getPosts(forceRefresh: Boolean): PostsResult

    suspend fun createPost(
        title: String,
        body: String,
        imageUrl: String?,
    ): Post

    suspend fun updatePost(
        id: Long,
        title: String,
        body: String,
        imageUrl: String?,
    ): Post

    suspend fun deletePost(id: Long)
}
