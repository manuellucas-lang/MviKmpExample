package com.example.mviexample.features.posts

import com.example.mviexample.mvi.MviContract
import com.example.mviexample.shared.data.model.Post

object PostsContract : MviContract {

    enum class PostFilter {
        All,
        Mine,
    }

    data class PostsState(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val posts: List<Post> = emptyList(),
        val query: String = "",
        val filter: PostFilter = PostFilter.All,
        val selectedPost: Post? = null,
        val editorPost: Post? = null,
        val isEditorOpen: Boolean = false,
        val isSaving: Boolean = false,
        val deleteTarget: Post? = null,
        val isDeleting: Boolean = false,
        val error: String? = null,
    ) : MviContract.UiState {

        val visiblePosts: List<Post>
            get() = posts.filter(::matches).filter { post ->
                when (filter) {
                    PostFilter.All -> true
                    PostFilter.Mine -> post.mine
                }
            }

        private fun matches(post: Post): Boolean {
            val q = query.trim()
            if (q.isEmpty()) return true
            return post.title.contains(q, ignoreCase = true) ||
                post.body.contains(q, ignoreCase = true) ||
                post.authorName?.contains(q, ignoreCase = true) == true
        }
    }

    sealed interface PostsIntent : MviContract.UiIntent {
        data object LoadPosts : PostsIntent
        data object RefreshPosts : PostsIntent
        data class UpdateQuery(val query: String) : PostsIntent
        data class SelectFilter(val filter: PostFilter) : PostsIntent
        data object OpenCreate : PostsIntent
        data class OpenDetail(val post: Post) : PostsIntent
        data object CloseDetail : PostsIntent
        data class OpenEdit(val post: Post) : PostsIntent
        data object CloseEditor : PostsIntent
        data class SavePost(val title: String, val body: String, val imageUrl: String?) : PostsIntent
        data class RequestDelete(val post: Post) : PostsIntent
        data object ConfirmDelete : PostsIntent
        data object DismissDelete : PostsIntent
        data object Retry : PostsIntent
    }

    sealed interface PostsEffect : MviContract.UiEffect {
        data class ShowMessage(val message: String) : PostsEffect
    }
}
