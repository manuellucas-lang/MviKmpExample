package com.example.mviexample.features.posts

import com.example.mviexample.features.posts.PostsContract.PostFilter
import com.example.mviexample.features.posts.PostsContract.PostsIntent
import com.example.mviexample.shared.data.PostsRepository
import com.example.mviexample.shared.data.PostsResult
import com.example.mviexample.shared.data.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PostsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val samplePost = Post(id = 1, userId = 1, title = "Title", body = "Body", authorName = "Leanne")

    private fun createViewModel(
        result: PostsResult = PostsResult(emptyList(), fromCache = false),
        onCreate: (Post) -> Post = { it },
    ) = PostsViewModel(FakePostsRepository(result, onCreate))

    @Test
    fun initialState_usesDefaults() {
        val viewModel = createViewModel()
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(emptyList<Post>(), viewModel.state.value.posts)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isEditorOpen)
        assertNull(viewModel.state.value.selectedPost)
        assertNull(viewModel.state.value.deleteTarget)
    }

    @Test
    fun loadPosts_success_updatesState() = runTest(dispatcher) {
        val viewModel = createViewModel(PostsResult(listOf(samplePost), fromCache = false))

        advanceUntilIdle()

        assertEquals(listOf(samplePost), viewModel.state.value.posts)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun loadPosts_fromCache_emitsMessage() = runTest(dispatcher) {
        val viewModel = createViewModel(PostsResult(listOf(samplePost), fromCache = true))

        advanceUntilIdle()

        assertEquals(listOf(samplePost), viewModel.state.value.posts)
        assertTrue(viewModel.effects.first() is PostsContract.PostsEffect.ShowMessage)
    }

    @Test
    fun loadPosts_failure_setsErrorAndEmitsEffect() = runTest(dispatcher) {
        val viewModel = PostsViewModel(FailingPostsRepository())

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.effects.first() is PostsContract.PostsEffect.ShowMessage)
    }

    @Test
    fun search_queryFiltersVisiblePosts() = runTest(dispatcher) {
        val alpha = Post(1, 1, "Alpha insight", "Content about alpha", authorName = "Leanne")
        val beta = Post(2, 1, "Beta insight", "Content about beta", authorName = "Leanne")
        val viewModel = createViewModel(PostsResult(listOf(alpha, beta), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(PostsIntent.UpdateQuery("alpha"))
        advanceUntilIdle()

        assertEquals(listOf(alpha), viewModel.state.value.visiblePosts)
    }

    @Test
    fun filter_mine_showsOnlyMyPosts() = runTest(dispatcher) {
        val remote = Post(1, 1, "Remote", "Content", authorName = "Leanne", mine = false)
        val mine = Post(101, 1, "Mine", "Content", authorName = "You", mine = true)
        val viewModel = createViewModel(PostsResult(listOf(remote, mine), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(PostsIntent.SelectFilter(PostFilter.Mine))
        advanceUntilIdle()

        assertEquals(listOf(mine), viewModel.state.value.visiblePosts)
    }

    @Test
    fun createPost_addsPostAndClosesEditor() = runTest(dispatcher) {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.onIntent(PostsIntent.OpenCreate)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isEditorOpen)

        viewModel.onIntent(PostsIntent.SavePost("Hello world", "A brand new insight", "https://example.com/img.png"))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isEditorOpen)
        val created = viewModel.state.value.posts.first()
        assertEquals("Hello world", created.title)
        assertEquals("A brand new insight", created.body)
        assertTrue(created.mine)
    }

    @Test
    fun createPost_withBlankTitle_rejectsWithMessage() = runTest(dispatcher) {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.onIntent(PostsIntent.OpenCreate)
        advanceUntilIdle()

        viewModel.onIntent(PostsIntent.SavePost("   ", "Body text here", null))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isEditorOpen)
        assertTrue(viewModel.effects.first() is PostsContract.PostsEffect.ShowMessage)
    }

    @Test
    fun updatePost_updatesPostAndSelection() = runTest(dispatcher) {
        val viewModel = createViewModel(PostsResult(listOf(samplePost), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(PostsIntent.OpenDetail(samplePost))
        advanceUntilIdle()
        viewModel.onIntent(PostsIntent.OpenEdit(samplePost))
        advanceUntilIdle()

        viewModel.onIntent(PostsIntent.SavePost("Updated title", "Updated body", null))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isEditorOpen)
        assertEquals("Updated title", viewModel.state.value.posts.first().title)
        assertEquals("Updated title", viewModel.state.value.selectedPost?.title)
    }

    @Test
    fun deletePost_removesPostAndClearsSelection() = runTest(dispatcher) {
        val viewModel = createViewModel(PostsResult(listOf(samplePost), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(PostsIntent.OpenDetail(samplePost))
        advanceUntilIdle()

        viewModel.onIntent(PostsIntent.RequestDelete(samplePost))
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.deleteTarget)

        viewModel.onIntent(PostsIntent.ConfirmDelete)
        advanceUntilIdle()

        assertEquals(emptyList<Post>(), viewModel.state.value.posts)
        assertNull(viewModel.state.value.selectedPost)
        assertNull(viewModel.state.value.deleteTarget)
    }

    @Test
    fun dismissDelete_keepsPost() = runTest(dispatcher) {
        val viewModel = createViewModel(PostsResult(listOf(samplePost), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(PostsIntent.RequestDelete(samplePost))
        advanceUntilIdle()
        viewModel.onIntent(PostsIntent.DismissDelete)
        advanceUntilIdle()

        assertEquals(listOf(samplePost), viewModel.state.value.posts)
        assertNull(viewModel.state.value.deleteTarget)
    }
}

private class FakePostsRepository(
    private val result: PostsResult,
    private val onCreate: (Post) -> Post = { it },
) : PostsRepository {
    override suspend fun getPosts(forceRefresh: Boolean): PostsResult = result

    override suspend fun createPost(title: String, body: String, imageUrl: String?): Post =
        onCreate(
            Post(
                id = 101,
                userId = 1,
                title = title,
                body = body,
                imageUrl = imageUrl,
                authorName = "You",
                mine = true,
            ),
        )

    override suspend fun updatePost(id: Long, title: String, body: String, imageUrl: String?): Post =
        Post(id = id, userId = 1, title = title, body = body, imageUrl = imageUrl, authorName = "You", mine = true)

    override suspend fun deletePost(id: Long) = Unit
}

private class FailingPostsRepository : PostsRepository {
    override suspend fun getPosts(forceRefresh: Boolean): PostsResult =
        throw RuntimeException("network down")

    override suspend fun createPost(title: String, body: String, imageUrl: String?) =
        throw RuntimeException("network down")

    override suspend fun updatePost(id: Long, title: String, body: String, imageUrl: String?) =
        throw RuntimeException("network down")

    override suspend fun deletePost(id: Long) =
        throw RuntimeException("network down")
}
