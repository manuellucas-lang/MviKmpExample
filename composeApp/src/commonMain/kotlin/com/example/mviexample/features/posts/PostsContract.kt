package com.example.mviexample.features.posts

import com.example.mviexample.mvi.MviContract
import com.example.mviexample.shared.data.model.Post

object PostsContract : MviContract {

    data class PostsState(
        val isLoading: Boolean = false,
        val posts: List<Post> = emptyList(),
        val error: String? = null,
    ) : MviContract.UiState

    sealed interface PostsIntent : MviContract.UiIntent {
        data object LoadPosts : PostsIntent
        data object RefreshPosts : PostsIntent
    }

    sealed interface PostsEffect : MviContract.UiEffect {
        data class ShowMessage(val message: String) : PostsEffect
    }
}
