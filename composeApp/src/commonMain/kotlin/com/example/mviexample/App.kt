package com.example.mviexample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    var darkTheme by rememberSaveable { mutableStateOf(true) }
    MviTheme(darkTheme = darkTheme) {
        PostsApp(
            darkTheme = darkTheme,
            onToggleTheme = { darkTheme = !darkTheme },
        )
    }
}
