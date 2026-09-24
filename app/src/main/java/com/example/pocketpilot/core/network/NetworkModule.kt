package com.example.pocketpilot.core.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Manual composition root for the network stack. Kept framework-free so the
 * same factories can back a Hilt module later without changing callers.
 */
object NetworkModule {

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    fun provideLoggingInterceptor(isDebug: Boolean): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (isDebug) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor, authInterceptor: AuthHeaderInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT_SECONDS, NetworkConfig.TIMEOUT_UNIT)
            .readTimeout(NetworkConfig.READ_TIMEOUT_SECONDS, NetworkConfig.TIMEOUT_UNIT)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT_SECONDS, NetworkConfig.TIMEOUT_UNIT)
            .callTimeout(NetworkConfig.CALL_TIMEOUT_SECONDS, NetworkConfig.TIMEOUT_UNIT)
            .retryOnConnectionFailure(true)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    fun provideRetrofit(client: OkHttpClient, baseUrl: String = NetworkConfig.BASE_URL): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
