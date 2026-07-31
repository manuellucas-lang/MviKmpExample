package com.example.mviexample

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.mviexample.features.posts.PostsScreen

@Composable
fun App() {
    MaterialTheme {
        PostsScreen()
    }
}
