package ch.fhnw.osmdemo.viewmodel

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.*
import platform.Foundation.NSURLRequestReloadIgnoringLocalCacheData
import platform.Foundation.setHTTPShouldHandleCookies
import platform.Foundation.setHTTPShouldUsePipelining

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
        requestTimeoutMillis  = 2000
        connectTimeoutMillis = 2000
        socketTimeoutMillis  = 2000
    }

    // Add default request configuration
    install(DefaultRequest) {
        // OSM services require a User-Agent header
        headers.append("User-Agent", "OSMDemo/1.0  (iOS Application)")
        headers.append("Accept", "*/*")
        headers.append("Connection", "keep-alive")
    }

}