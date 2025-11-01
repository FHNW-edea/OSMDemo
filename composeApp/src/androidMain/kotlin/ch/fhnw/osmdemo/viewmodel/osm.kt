package ch.fhnw.osmdemo.viewmodel

import ch.fhnw.osmdemo.appContext
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import okio.Path
import okio.Path.Companion.toPath

actual fun platformCacheDir(): Path =
    appContext.cacheDir
        .resolve("tilecache")
        .absolutePath
        .toPath()

actual fun createHttpClient(): HttpClient = HttpClient(Android) {
    engine {
        // Set connection timeout
        connectTimeout = 30_000 // 30 seconds

        // Set socket timeout
        socketTimeout = 30_000 // 30 seconds
    }

    // Add timeout plugin for request-level control
    install(HttpTimeout) {
        requestTimeoutMillis = 5000
        connectTimeoutMillis = 5000
        socketTimeoutMillis  = 5000
    }

    // Add default request configuration
    install(DefaultRequest) {
        // OSM services require a User-Agent header
        headers.append("User-Agent", "OSMDemo/1.0 (Android)")
        headers.append("Accept", "*/*")
        headers.append("Connection", "keep-alive")
    }
}