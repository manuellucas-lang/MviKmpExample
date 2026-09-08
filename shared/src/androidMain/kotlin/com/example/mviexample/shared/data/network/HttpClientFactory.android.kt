package com.example.mviexample.shared.data.network

import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun createHttpClient(): HttpClient =
    HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
            connectTimeoutMillis = 10_000
        }
    }

/**
 * Server base URL for Android:
 *
 * - Emulator: the host machine is reachable at `10.0.2.2`.
 * - Physical device (USB): use `adb reverse tcp:8080 tcp:8080`, which forwards
 *   the phone's `localhost:8080` to the host server over USB. Re-run the reverse
 *   rule whenever the device disconnects/reconnects.
 */
actual fun apiBaseUrl(): String =
    if (isRunningOnEmulator()) {
        "http://10.0.2.2:8080"
    } else {
        "http://localhost:8080"
    }

private fun isRunningOnEmulator(): Boolean =
    Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.startsWith("unknown") ||
        Build.MODEL.contains("google_sdk") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for") ||
        Build.MANUFACTURER.contains("Genymotion") ||
        Build.HARDWARE.contains("goldfish") ||
        Build.HARDWARE.contains("ranchu") ||
        Build.PRODUCT.contains("sdk_gphone")
