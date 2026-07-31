package com.example.mviexample

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.example.mviexample.data.image.createImageHttpClient
import com.example.mviexample.designsystem.theme.MviTheme
import com.example.mviexample.features.posts.PostsApp

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(httpClient = createImageHttpClient()))
            }
            .build()
    }
    MviTheme {
        PostsApp()
    }
}
