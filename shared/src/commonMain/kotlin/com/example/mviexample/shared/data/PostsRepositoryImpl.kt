package com.example.mviexample.shared.data

import com.example.mviexample.shared.data.model.Post
import com.example.mviexample.shared.data.network.NewPostRequest
import com.example.mviexample.shared.data.network.PostDto
import com.example.mviexample.shared.data.network.PostsApi
import com.example.mviexample.shared.data.network.UpdatePostRequest
import com.example.mviexample.shared.database.AppDatabase
import com.example.mviexample.shared.database.Posts
import com.example.mviexample.shared.util.currentTimeMillis

class PostsRepositoryImpl(
    private val api: PostsApi,
    private val database: AppDatabase,
) : PostsRepository {

    private val queries = database.postQueries
    private val metaQueries = database.metaQueries

    override suspend fun getPosts(forceRefresh: Boolean): PostsResult {
        val cached = queries.selectAll().executeAsList().map { it.toModel() }
        val lastSync = metaQueries.selectById(KEY_LAST_SYNC).executeAsOneOrNull()?.value_?.toLongOrNull()
        val isFresh = lastSync != null && currentTimeMillis() - lastSync < CACHE_TTL_MILLIS
        if (!forceRefresh && cached.isNotEmpty() && isFresh) {
            return PostsResult(cached, fromCache = true)
        }
        return try {
            val remote = api.getPosts().map { it.toModel() }
            queries.transaction {
                remote.forEach { post -> post.upsert() }
                metaQueries.upsert(KEY_LAST_SYNC, currentTimeMillis().toString())
            }
            val remoteIds = remote.mapTo(mutableSetOf()) { it.id }
            val mine = queries.selectAll().executeAsList()
                .map { it.toModel() }
                .filter { it.mine && it.id !in remoteIds }
            PostsResult((mine + remote).sortedByDescending { it.id }, fromCache = false)
        } catch (e: Exception) {
            if (cached.isNotEmpty()) {
                PostsResult(cached, fromCache = true)
            } else {
                throw e
            }
        }
    }

    override suspend fun createPost(title: String, body: String, imageUrl: String?): Post {
        val remote = api.createPost(NewPostRequest(title = title, description = body))
        val post = Post(
            id = remote.id,
            userId = 1L,
            title = title,
            body = body,
            imageUrl = imageUrl.normalized(),
            authorName = "You",
            mine = true,
        )
        post.upsert()
        return post
    }

    override suspend fun updatePost(id: Long, title: String, body: String, imageUrl: String?): Post {
        api.updatePost(id, UpdatePostRequest(title = title, description = body))
        val existing = queries.selectById(id).executeAsOneOrNull()
        val post = Post(
            id = id,
            userId = existing?.userId ?: 1L,
            title = title,
            body = body,
            imageUrl = imageUrl.normalized(),
            authorName = existing?.author_name ?: "You",
            mine = existing?.is_mine == 1L,
        )
        post.upsert()
        return post
    }

    override suspend fun deletePost(id: Long) {
        runCatching { api.deletePost(id) }
        queries.deleteById(id)
    }

    private fun Post.upsert() {
        queries.insertOrReplace(
            id = id,
            userId = userId,
            title = title,
            body = body,
            image_url = imageUrl,
            author_name = authorName,
            is_mine = if (mine) 1L else 0L,
        )
    }
}

private fun String?.normalized(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

private const val CACHE_TTL_MILLIS = 60L * 60L * 1000L
private const val KEY_LAST_SYNC = "last_sync_at"

private fun Posts.toModel() =
    Post(
        id = id,
        userId = userId,
        title = title,
        body = body,
        imageUrl = image_url,
        authorName = author_name,
        mine = is_mine == 1L,
    )

private fun PostDto.toModel() =
    Post(
        id = id,
        userId = 1L,
        title = title,
        body = description,
        imageUrl = thumbnail.normalized(),
        authorName = brand?.takeIf { it.isNotBlank() } ?: category?.replaceFirstChar { it.titlecase() },
        mine = false,
    )
