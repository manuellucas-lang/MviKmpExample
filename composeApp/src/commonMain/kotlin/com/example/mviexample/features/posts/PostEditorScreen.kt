package com.example.mviexample.features.posts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mviexample.designsystem.components.AppButton
import com.example.mviexample.designsystem.components.AppTextField
import com.example.mviexample.designsystem.components.BrandTopBar
import com.example.mviexample.features.posts.components.PostCard
import com.example.mviexample.shared.data.model.Post

@Composable
fun PostEditorScreen(
    state: PostsContract.PostsState,
    actions: PostsActions,
    snackbarHostState: SnackbarHostState,
) {
    val editing = state.editorPost

    var title by rememberSaveable(editing?.id) { mutableStateOf(editing?.title ?: "") }
    var body by rememberSaveable(editing?.id) { mutableStateOf(editing?.body ?: "") }
    var imageUrl by rememberSaveable(editing?.id) { mutableStateOf(editing?.imageUrl ?: "") }

    val previewPost = editing?.copy(
        title = title.ifBlank { editing.title },
        body = body.ifBlank { editing.body },
        imageUrl = imageUrl.ifBlank { null },
    ) ?: Post(
        id = 0,
        userId = 1,
        title = title.ifBlank { "Post title" },
        body = body.ifBlank { "Your content preview will appear here." },
        imageUrl = imageUrl.ifBlank { null },
        authorName = "You",
        mine = true,
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BrandTopBar(
                title = if (editing != null) "Edit post" else "New post",
                subtitle = if (editing != null) "Update your insight" else "Share with your team",
                onBack = actions.onCloseEditor,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                Text(
                    text = "CONTENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(10.dp))
                AppTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "Title",
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3,
                    supportingText = "Give your insight a clear headline",
                )
                Spacer(Modifier.height(18.dp))
                AppTextField(
                    value = body,
                    onValueChange = { body = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "Content",
                    singleLine = false,
                    minLines = 6,
                    maxLines = 12,
                    supportingText = "At least 3 characters",
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "PHOTO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(10.dp))
                AppTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "Image URL (optional)",
                    placeholder = "https://…",
                    supportingText = "Paste a link to attach an image to your post",
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "PREVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(10.dp))
                PostCard(
                    post = previewPost,
                    onClick = {},
                    onEdit = {},
                    onDelete = {},
                )
                Spacer(Modifier.height(8.dp))
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    AppButton(
                        text = if (editing != null) "Save changes" else "Publish post",
                        onClick = {
                            actions.onSave(title, body, imageUrl.ifBlank { null })
                        },
                        isLoading = state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
            }
        }
    }
}
