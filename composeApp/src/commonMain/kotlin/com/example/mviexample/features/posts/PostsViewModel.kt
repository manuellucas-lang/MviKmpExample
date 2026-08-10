package com.example.mviexample.features.posts

import com.example.mviexample.features.posts.PostsContract.PostsEffect
import com.example.mviexample.features.posts.PostsContract.PostsIntent
import com.example.mviexample.features.posts.PostsContract.PostsState
import com.example.mviexample.mvi.MviViewModel
import com.example.mviexample.shared.data.PostsRepository
import com.example.mviexample.shared.data.model.Post

class PostsViewModel(
    private val repository: PostsRepository,
) : MviViewModel<PostsState, PostsIntent, PostsEffect>(
    initialState = PostsState(),
) {

    init {
        onIntent(PostsIntent.LoadPosts)
    }

    override suspend fun handleIntent(intent: PostsIntent) {
        when (intent) {
            is PostsIntent.LoadPosts -> loadPosts(forceRefresh = false)
            is PostsIntent.RefreshPosts -> loadPosts(forceRefresh = true)
            is PostsIntent.UpdateQuery -> setState { it.copy(query = intent.query) }
            is PostsIntent.SelectFilter -> setState { it.copy(filter = intent.filter) }
            is PostsIntent.SelectTab -> setState { it.copy(tab = intent.tab) }
            is PostsIntent.OpenCreate -> setState {
                it.copy(isEditorOpen = true, editorPost = null)
            }
            is PostsIntent.OpenDetail -> setState { it.copy(selectedPost = intent.post) }
            is PostsIntent.CloseDetail -> setState { it.copy(selectedPost = null) }
            is PostsIntent.OpenEdit -> setState {
                it.copy(isEditorOpen = true, editorPost = intent.post, isSaving = false)
            }
            is PostsIntent.CloseEditor -> setState {
                it.copy(isEditorOpen = false, editorPost = null, isSaving = false)
            }
            is PostsIntent.SavePost -> savePost(intent.title, intent.body, intent.imageUrl)
            is PostsIntent.ToggleSavePost -> toggleSave(intent.post)
            is PostsIntent.RequestDelete -> setState { it.copy(deleteTarget = intent.post) }
            is PostsIntent.ConfirmDelete -> deleteCurrentTarget()
            is PostsIntent.DismissDelete -> setState { it.copy(deleteTarget = null) }
            is PostsIntent.Retry -> loadPosts(forceRefresh = false)
        }
    }

    private suspend fun loadPosts(forceRefresh: Boolean) {
        setState {
            it.copy(
                isLoading = if (forceRefresh) it.isLoading else true,
                isRefreshing = if (forceRefresh) true else it.isRefreshing,
                error = null,
            )
        }
        try {
            val result = repository.getPosts(forceRefresh)
            setState {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    posts = result.posts,
                    error = null,
                )
            }
            if (result.fromCache) {
                emitEffect(PostsEffect.ShowMessage("Offline — showing cached posts"))
            }
        } catch (e: Exception) {
            setState {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Couldn't load posts. Check your connection.",
                )
            }
            emitEffect(PostsEffect.ShowMessage("Network error: ${e.message ?: "unknown"}"))
        }
    }

    private suspend fun savePost(rawTitle: String, rawBody: String, imageUrl: String?) {
        val title = rawTitle.trim()
        val body = rawBody.trim()
        if (title.isEmpty()) {
            emitEffect(PostsEffect.ShowMessage("Title is required"))
            return
        }
        if (body.length < 3) {
            emitEffect(PostsEffect.ShowMessage("Content is too short"))
            return
        }
        val editing = state.value.editorPost
        setState { it.copy(isSaving = true) }
        try {
            if (editing == null) {
                val created = repository.createPost(title, body, imageUrl)
                setState { current ->
                    current.copy(
                        posts = (listOf(created) + current.posts.filterNot { it.id == created.id })
                            .sortedByDescending { it.id },
                        isEditorOpen = false,
                        isSaving = false,
                    )
                }
                emitEffect(PostsEffect.ShowMessage("Post created"))
            } else {
                val updated = repository.updatePost(editing.id, title, body, imageUrl)
                setState { current ->
                    current.copy(
                        posts = current.posts.map { if (it.id == updated.id) updated else it },
                        selectedPost = if (current.selectedPost?.id == updated.id) updated else current.selectedPost,
                        isEditorOpen = false,
                        isSaving = false,
                    )
                }
                emitEffect(PostsEffect.ShowMessage("Post updated"))
            }
        } catch (e: Exception) {
            setState { it.copy(isSaving = false) }
            emitEffect(PostsEffect.ShowMessage("Save failed: ${e.message ?: "network error"}"))
        }
    }

    private suspend fun toggleSave(post: Post) {
        val newSaved = !post.saved
        try {
            repository.setPostSaved(post.id, newSaved)
            setState { current ->
                current.copy(
                    posts = current.posts.map { if (it.id == post.id) it.copy(saved = newSaved) else it },
                    selectedPost = if (current.selectedPost?.id == post.id) {
                        current.selectedPost?.copy(saved = newSaved)
                    } else {
                        current.selectedPost
                    },
                )
            }
            emitEffect(
                PostsEffect.ShowMessage(
                    if (newSaved) "Post saved" else "Post removed from saved",
                ),
            )
        } catch (e: Exception) {
            emitEffect(PostsEffect.ShowMessage("Couldn't update saved post: ${e.message ?: "unknown error"}"))
        }
    }

    private suspend fun deleteCurrentTarget() {
        val target = state.value.deleteTarget ?: return
        setState { it.copy(isDeleting = true) }
        try {
            repository.deletePost(target.id)
            setState { current ->
                current.copy(
                    posts = current.posts.filterNot { it.id == target.id },
                    selectedPost = if (current.selectedPost?.id == target.id) null else current.selectedPost,
                    deleteTarget = null,
                    isDeleting = false,
                )
            }
            emitEffect(PostsEffect.ShowMessage("Post deleted"))
        } catch (e: Exception) {
            setState { it.copy(isDeleting = false, deleteTarget = null) }
            emitEffect(PostsEffect.ShowMessage("Delete failed: ${e.message ?: "network error"}"))
        }
    }
}
