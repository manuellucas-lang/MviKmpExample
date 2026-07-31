package com.example.mviexample.features.posts

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

    private val samplePost = Post(id = 1, userId = 1, title = "Title", body = "Body")

    @Test
    fun initialState_usesDefaults() {
        val viewModel = PostsViewModel(
            FakePostsRepository(PostsResult(emptyList(), fromCache = false)),
        )
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(emptyList<Post>(), viewModel.state.value.posts)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun loadPosts_success_updatesState() = runTest(dispatcher) {
        val viewModel = PostsViewModel(
            FakePostsRepository(PostsResult(listOf(samplePost), fromCache = false)),
        )

        advanceUntilIdle()

        assertEquals(listOf(samplePost), viewModel.state.value.posts)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun loadPosts_fromCache_emitsMessage() = runTest(dispatcher) {
        val viewModel = PostsViewModel(
            FakePostsRepository(PostsResult(listOf(samplePost), fromCache = true)),
        )

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
}

private class FakePostsRepository(
    private val result: PostsResult,
) : PostsRepository {
    override suspend fun getPosts(forceRefresh: Boolean): PostsResult = result
}

private class FailingPostsRepository : PostsRepository {
    override suspend fun getPosts(forceRefresh: Boolean): PostsResult =
        throw RuntimeException("network down")
}
