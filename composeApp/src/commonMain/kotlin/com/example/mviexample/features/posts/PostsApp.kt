package com.example.mviexample.features.posts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mviexample.designsystem.components.ConfirmDialog
import com.example.mviexample.features.posts.PostsContract.PostsEffect
import com.example.mviexample.features.posts.PostsContract.PostsIntent
import com.example.mviexample.features.posts.PostsContract.PostsState
import com.example.mviexample.shared.AppGraph

private sealed interface AppScreen {
    data object List : AppScreen
    data object Detail : AppScreen
    data object Editor : AppScreen
}

private fun currentScreen(state: PostsState): AppScreen = when {
    state.isEditorOpen -> AppScreen.Editor
    state.selectedPost != null -> AppScreen.Detail
    else -> AppScreen.List
}

data class PostsActions(
    val onRefresh: () -> Unit = {},
    val onQueryChange: (String) -> Unit = {},
    val onFilterChange: (PostsContract.PostFilter) -> Unit = {},
    val onOpenCreate: () -> Unit = {},
    val onOpenDetail: (com.example.mviexample.shared.data.model.Post) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onRequestDelete: (com.example.mviexample.shared.data.model.Post) -> Unit = {},
    val onOpenEdit: (com.example.mviexample.shared.data.model.Post) -> Unit = {},
    val onCloseDetail: () -> Unit = {},
    val onCloseEditor: () -> Unit = {},
    val onSave: (String, String, String?) -> Unit = { _, _, _ -> },
)

@Composable
fun PostsApp(
    viewModel: PostsViewModel = viewModel { PostsViewModel(AppGraph.container.postsRepository) },
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PostsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val actions = PostsActions(
        onRefresh = { viewModel.onIntent(PostsIntent.RefreshPosts) },
        onQueryChange = { viewModel.onIntent(PostsIntent.UpdateQuery(it)) },
        onFilterChange = { viewModel.onIntent(PostsIntent.SelectFilter(it)) },
        onOpenCreate = { viewModel.onIntent(PostsIntent.OpenCreate) },
        onOpenDetail = { viewModel.onIntent(PostsIntent.OpenDetail(it)) },
        onRetry = { viewModel.onIntent(PostsIntent.Retry) },
        onRequestDelete = { viewModel.onIntent(PostsIntent.RequestDelete(it)) },
        onOpenEdit = { viewModel.onIntent(PostsIntent.OpenEdit(it)) },
        onCloseDetail = { viewModel.onIntent(PostsIntent.CloseDetail) },
        onCloseEditor = { viewModel.onIntent(PostsIntent.CloseEditor) },
        onSave = { title, body, imageUrl ->
            viewModel.onIntent(PostsIntent.SavePost(title, body, imageUrl))
        },
    )

    state.deleteTarget?.let { target ->
        ConfirmDialog(
            title = "Delete post?",
            message = "\u201C${target.title.take(60)}${if (target.title.length > 60) "…" else ""}\u201D will be permanently removed.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            destructive = true,
            onConfirm = { viewModel.onIntent(PostsIntent.ConfirmDelete) },
            onDismiss = { viewModel.onIntent(PostsIntent.DismissDelete) },
        )
    }

    AnimatedContent(
        targetState = currentScreen(state),
        transitionSpec = {
            when {
                targetState is AppScreen.Detail || targetState is AppScreen.Editor ->
                    (slideInHorizontally(tween(260)) { it / 4 } + fadeIn(tween(220)))
                        .togetherWith(slideOutHorizontally(tween(260)) { -it / 4 } + fadeOut(tween(220)))

                else ->
                    (slideInHorizontally(tween(260)) { -it / 4 } + fadeIn(tween(220)))
                        .togetherWith(slideOutHorizontally(tween(260)) { it / 4 } + fadeOut(tween(220)))
            }
        },
        modifier = Modifier,
        label = "screenTransition",
    ) { screen ->
        when (screen) {
            AppScreen.List -> PostsListScreen(
                state = state,
                actions = actions,
                snackbarHostState = snackbarHostState,
            )

            AppScreen.Detail -> PostDetailScreen(
                state = state,
                actions = actions,
                snackbarHostState = snackbarHostState,
            )

            AppScreen.Editor -> PostEditorScreen(
                state = state,
                actions = actions,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}
