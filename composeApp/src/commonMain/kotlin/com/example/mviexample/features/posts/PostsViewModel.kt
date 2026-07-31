package com.example.mviexample.features.posts

import com.example.mviexample.mvi.MviViewModel
import com.example.mviexample.shared.data.PostsRepository

class PostsViewModel(
    private val repository: PostsRepository,
) : MviViewModel<PostsContract.PostsState, PostsContract.PostsIntent, PostsContract.PostsEffect>(
    initialState = PostsContract.PostsState(),
) {

    init {
        onIntent(PostsContract.PostsIntent.LoadPosts)
    }

    override suspend fun handleIntent(intent: PostsContract.PostsIntent) {
        when (intent) {
            is PostsContract.PostsIntent.LoadPosts -> loadPosts(forceRefresh = false)
            is PostsContract.PostsIntent.RefreshPosts -> loadPosts(forceRefresh = true)
        }
    }

    private suspend fun loadPosts(forceRefresh: Boolean) {
        setState { it.copy(isLoading = true, error = null) }
        try {
            val result = repository.getPosts(forceRefresh)
            setState {
                it.copy(
                    isLoading = false,
                    posts = result.posts,
                    error = null,
                )
            }
            if (result.fromCache) {
                emitEffect(PostsContract.PostsEffect.ShowMessage("Offline — showing cached posts"))
            }
        } catch (e: Exception) {
            setState {
                it.copy(
                    isLoading = false,
                    error = "Couldn't load posts. Check your connection.",
                )
            }
            emitEffect(PostsContract.PostsEffect.ShowMessage("Network error: ${e.message ?: "unknown"}"))
        }
    }
}
