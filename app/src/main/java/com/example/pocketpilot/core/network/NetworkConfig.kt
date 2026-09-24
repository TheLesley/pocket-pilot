package com.example.pocketpilot.core.network

import java.util.concurrent.TimeUnit

object NetworkConfig {
    const val BASE_URL: String = "https://api.pocketpilot.app/"

    const val CONNECT_TIMEOUT_SECONDS: Long = 15L
    const val READ_TIMEOUT_SECONDS: Long = 20L
    const val WRITE_TIMEOUT_SECONDS: Long = 20L
    const val CALL_TIMEOUT_SECONDS: Long = 30L

    val TIMEOUT_UNIT: TimeUnit = TimeUnit.SECONDS
}
