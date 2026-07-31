package com.example.mviexample.features.posts

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mviexample.designsystem.components.AppButtonStyle
import com.example.mviexample.designsystem.components.AppTextField
import com.example.mviexample.designsystem.components.BrandHeader
import com.example.mviexample.designsystem.components.EmptyState
import com.example.mviexample.designsystem.components.ErrorState
import com.example.mviexample.designsystem.components.FilterPill
import com.example.mviexample.designsystem.components.PostCardSkeleton
import com.example.mviexample.designsystem.theme.BrandGradientEnd
import com.example.mviexample.designsystem.theme.BrandGradientStart
import com.example.mviexample.features.posts.components.PostCard
import com.example.mviexample.shared.data.model.Post

@Composable
fun PostsListScreen(
    state: PostsContract.PostsState,
    actions: PostsActions,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BrandHeader(
                title = "Discover",
                subtitle = "${state.posts.size} insights · ${state.posts.count { it.mine }} yours",
                action = {
                    val infinite = rememberInfiniteTransition(label = "refreshSpin")
                    val rotation by infinite.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(tween(700)),
                        label = "refreshAngle",
                    )
                    IconButton(onClick = actions.onRefresh, enabled = !state.isRefreshing) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.rotateIf(state.isRefreshing, rotation),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = actions.onOpenCreate,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New post") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Spacer(Modifier.height(12.dp))

            AppTextField(
                value = state.query,
                onValueChange = actions.onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = "Search posts or authors…",
                leadingIcon = Icons.Default.Search,
                trailingIcon = if (state.query.isNotEmpty()) {
                    {
                        IconButton(onClick = { actions.onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    null
                },
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterPill(
                    text = "All",
                    count = state.posts.size,
                    selected = state.filter == PostsContract.PostFilter.All,
                    onClick = { actions.onFilterChange(PostsContract.PostFilter.All) },
                )
                FilterPill(
                    text = "Mine",
                    count = state.posts.count { it.mine },
                    selected = state.filter == PostsContract.PostFilter.Mine,
                    onClick = { actions.onFilterChange(PostsContract.PostFilter.Mine) },
                )
            }

            Spacer(Modifier.height(8.dp))

            if (state.isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading && state.posts.isEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(4) {
                                PostCardSkeleton()
                            }
                        }
                    }

                    state.error != null && state.posts.isEmpty() -> {
                        ErrorState(
                            message = state.error,
                            modifier = Modifier.align(Alignment.Center),
                            onRetry = actions.onRetry,
                        )
                    }

                    state.visiblePosts.isEmpty() -> {
                        EmptyState(
                            icon = if (state.posts.isEmpty()) Icons.Default.PostAdd else Icons.Default.SearchOff,
                            title = if (state.posts.isEmpty()) "No posts yet" else "No results",
                            message = if (state.posts.isEmpty()) {
                                "Be the first to share an insight with your team."
                            } else {
                                "Nothing matches \u201C${state.query}\u201D. Try a different search or filter."
                            },
                            modifier = Modifier.align(Alignment.Center),
                            actionLabel = if (state.posts.isEmpty()) "Create your first post" else null,
                            onAction = if (state.posts.isEmpty()) actions.onOpenCreate else null,
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
                                bottom = 96.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            item(key = "hero") {
                                InsightsHero(
                                    total = state.posts.size,
                                    withPhotos = state.posts.count { it.imageUrl != null },
                                    mine = state.posts.count { it.mine },
                                )
                            }
                            items(state.visiblePosts, key = { it.id }) { post ->
                                PostCard(
                                    post = post,
                                    onClick = { actions.onOpenDetail(post) },
                                    onEdit = { actions.onOpenEdit(post) },
                                    onDelete = { actions.onRequestDelete(post) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightsHero(
    total: Int,
    withPhotos: Int,
    mine: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(listOf(BrandGradientStart, BrandGradientEnd)),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 20.dp, vertical = 22.dp),
    ) {
        Text(
            text = "INSIGHTS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "$total posts",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "$withPhotos with photos · $mine created by you",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Modifier.rotateIf(enabled: Boolean, rotation: Float): Modifier =
    if (enabled) {
        this.rotate(rotation)
    } else {
        this
    }
