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
import com.example.mviexample.features.auth.AuthRepository
import com.example.mviexample.features.auth.AuthRoot

@Composable
fun App(authRepository: AuthRepository) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(httpClient = createImageHttpClient()))
            }
            .build()
    }
    var darkTheme by rememberSaveable { mutableStateOf(true) }
    MviTheme(darkTheme = darkTheme) {
        AuthRoot(
            authRepository = authRepository,
            darkTheme = darkTheme,
            onToggleTheme = { darkTheme = !darkTheme },
        )
    }
}
