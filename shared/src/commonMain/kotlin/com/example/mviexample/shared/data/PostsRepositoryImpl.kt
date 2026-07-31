package com.example.mviexample.shared.data

import com.example.mviexample.shared.data.model.Post
import com.example.mviexample.shared.data.network.PostDto
import com.example.mviexample.shared.data.network.PostsApi
import com.example.mviexample.shared.database.AppDatabase
import com.example.mviexample.shared.database.Posts

class PostsRepositoryImpl(
    private val api: PostsApi,
    private val database: AppDatabase,
) : PostsRepository {

    private val queries = database.postQueries

    override suspend fun getPosts(forceRefresh: Boolean): PostsResult {
        val cached = queries.selectAll().executeAsList()
        if (!forceRefresh && cached.isNotEmpty()) {
            return PostsResult(cached.map { it.toModel() }, fromCache = true)
        }
        return try {
            val remote = api.getPosts()
            queries.transaction {
                queries.deleteAll()
                remote.forEach { post ->
                    queries.insertOrReplace(post.id, post.userId, post.title, post.body)
                }
            }
            PostsResult(remote.map { it.toModel() }, fromCache = false)
        } catch (e: Exception) {
            if (cached.isNotEmpty()) {
                PostsResult(cached.map { it.toModel() }, fromCache = true)
            } else {
                throw e
            }
        }
    }
}

private fun Posts.toModel() =
    Post(id = id, userId = userId, title = title, body = body)

private fun PostDto.toModel() =
    Post(id = id, userId = userId, title = title, body = body)
