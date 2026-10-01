package com.example.pocketpilot.core.network

import java.util.concurrent.TimeUnit

object NetworkConfig {
    const val BASE_URL: String = "https://api.pocketpilot.app/"

    /**
     * Overrideable pointer at the live backend. `null` means the app is running
     * without a configured remote — every request served locally, no sync UI
     * to surface. Retrofit still needs a base URL to build its client, so
     * [BASE_URL] stands in as the placeholder even when this is null.
     */
    val remoteBackendUrl: String? = null

    val hasRemoteBackend: Boolean
        get() = !remoteBackendUrl.isNullOrBlank()

    const val CONNECT_TIMEOUT_SECONDS: Long = 15L
    const val READ_TIMEOUT_SECONDS: Long = 20L
    const val WRITE_TIMEOUT_SECONDS: Long = 20L
    const val CALL_TIMEOUT_SECONDS: Long = 30L

    val TIMEOUT_UNIT: TimeUnit = TimeUnit.SECONDS
}
