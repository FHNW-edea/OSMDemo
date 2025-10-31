package ch.fhnw.osmdemo.viewmodel

import java.nio.file.Files
import java.nio.file.Paths
import io.ktor.client.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.cache.storage.*
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager
import org.apache.hc.core5.util.TimeValue

actual fun createHttpClient(): HttpClient = HttpClient(Apache5) {
    install(HttpCache) {
        // Persistent disk cache
        val cacheDir = Files.createDirectories(Paths.get("build/tiles-cache")).toFile()

        publicStorage(FileStorage(cacheDir))
    }

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
        requestTimeoutMillis = 2000
        connectTimeoutMillis = 2000
        socketTimeoutMillis  = 2000
    }

    // Add default request configuration
    install(DefaultRequest) {
        // OSM services require a User-Agent header
        headers.append("User-Agent", "PoiCh/1.0 (Desktop Application)")
        headers.append("Accept", "*/*")
    }
}