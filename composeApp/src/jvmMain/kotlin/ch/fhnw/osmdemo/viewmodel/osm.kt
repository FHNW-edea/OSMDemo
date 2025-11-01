package ch.fhnw.osmdemo.viewmodel

import io.ktor.client.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.*
import okio.Path
import okio.Path.Companion.toPath
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager
import org.apache.hc.core5.util.TimeValue

actual fun platformCacheDir(): Path {
    val userHome = System.getProperty("user.home")

    return "$userHome/.tilecache".toPath()
}

actual fun createHttpClient(): HttpClient = HttpClient(Apache5) {
     engine {
        connectTimeout           = 3_000 // 3 seconds
        socketTimeout            = 3_000
        connectionRequestTimeout = 3_000

        // Configure async connection pooling
        customizeClient {
            val connectionManager = PoolingAsyncClientConnectionManager().apply {
                maxTotal           = 100
                defaultMaxPerRoute = 8
            }

            setConnectionManager(connectionManager)
            evictIdleConnections(TimeValue.ofSeconds(3))
        }
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
        headers.append("User-Agent", "PoiCh/1.0 (Desktop Application)")
        headers.append("Accept", "*/*")
    }
}