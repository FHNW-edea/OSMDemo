package ch.fhnw.osmdemo.viewmodel

import kotlinx.cinterop.ExperimentalForeignApi
import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.*
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURLRequestReloadIgnoringLocalCacheData
import platform.Foundation.NSUserDomainMask
import platform.Foundation.setHTTPShouldHandleCookies
import platform.Foundation.setHTTPShouldUsePipelining

actual fun platformCacheDir(): Path {
    val paths = NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory,
            NSUserDomainMask,
            true)

    val cacheDir = paths.first() as String

    return "$cacheDir/tilecache".toPath()
}

@OptIn(ExperimentalForeignApi::class)
actual fun createHttpClient(): HttpClient = HttpClient(Darwin) {
    engine {
        configureRequest {
            // Set timeouts
            setTimeoutInterval(30.0) // 30 seconds timeout

            // Allow cellular access
            setAllowsCellularAccess(true)

            // Configure cache policy
            setCachePolicy(NSURLRequestReloadIgnoringLocalCacheData)

            // Set HTTP should handle cookies
            setHTTPShouldHandleCookies(true)

            // Set HTTP should use pipelining
            setHTTPShouldUsePipelining(true)
        }

        configureSession {
            // Configure connection pooling
            setHTTPMaximumConnectionsPerHost(20)

            // Set timeout for requests
            setTimeoutIntervalForRequest(30.0)

            // Set timeout for resources
            setTimeoutIntervalForResource(60.0)
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
        headers.append("User-Agent", "OSMDemo/1.0  (iOS Application)")
        headers.append("Accept", "*/*")
        headers.append("Connection", "keep-alive")
    }

}