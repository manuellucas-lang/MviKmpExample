package com.example.mviexample.shared.data.network

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient

expect fun apiBaseUrl(): String
