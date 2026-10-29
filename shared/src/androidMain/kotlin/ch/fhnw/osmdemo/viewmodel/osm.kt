package ch.fhnw.osmdemo.viewmodel

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import okio.Path
import okio.Path.Companion.toPath

actual fun platformCacheDir(): Path {
    val userHome = System.getProperty("user.home")
    return "$userHome/.tilecache".toPath()
}

actual fun createHttpClient(): HttpClient = HttpClient(Android) {
    engine {
        connectTimeout = 30_000
        socketTimeout = 30_000
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 5000
        connectTimeoutMillis = 5000
        socketTimeoutMillis = 5000
    }

    install(DefaultRequest) {
        headers.append("User-Agent", "OSMDemo/1.0 (Android)")
        headers.append("Accept", "*/*")
        headers.append("Connection", "keep-alive")
    }
}
