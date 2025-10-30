package ch.fhnw.osmdemo.viewmodel

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*

actual fun createHttpClient(): HttpClient = HttpClient(Android) {
    engine {
        // Set connection timeout
        connectTimeout = 30_000 // 30 seconds

        // Set socket timeout
        socketTimeout = 30_000 // 30 seconds
    }

    // Add timeout plugin for request-level control
    install(HttpTimeout) {
        requestTimeoutMillis = 2000
        connectTimeoutMillis = 2000
        socketTimeoutMillis  = 2000
    }

    // Add default request configuration
    install(DefaultRequest) {
        // OSM services require a User-Agent header
        headers.append("User-Agent", "OSMDemo/1.0 (Android)")
        headers.append("Accept", "*/*")
        headers.append("Connection", "keep-alive")
    }

}