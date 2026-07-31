package com.example.mviexample.data.image

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createImageHttpClient(): HttpClient =
    HttpClient(Darwin) {
        followRedirects = true
    }
