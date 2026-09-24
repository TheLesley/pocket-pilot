package com.example.pocketpilot.core.network

/**
 * Domain-agnostic wrappers around the failures the network layer can surface.
 * Repositories translate these into feature-specific errors so upper layers
 * never see Retrofit or OkHttp types.
 */
sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** Device is offline or the server is unreachable. */
    class NoConnectivity(cause: Throwable? = null) : NetworkException("No network connection available", cause)

    /** A timeout elapsed while connecting, reading, or writing. */
    class Timeout(cause: Throwable? = null) : NetworkException("The request timed out", cause)

    /** 4xx response returned by the server. */
    class ClientError(val code: Int, val errorBody: String?, cause: Throwable? = null) :
        NetworkException("HTTP $code: client error", cause)

    /** 401 Unauthorized — carried separately so auth flows can react. */
    class Unauthorized(val errorBody: String?, cause: Throwable? = null) : NetworkException("HTTP 401: unauthorized", cause)

    /** 5xx response returned by the server. */
    class ServerError(val code: Int, val errorBody: String?, cause: Throwable? = null) :
        NetworkException("HTTP $code: server error", cause)

    /** Response body could not be parsed. */
    class Serialization(cause: Throwable) : NetworkException("Failed to parse response", cause)

    /** Anything else that slipped past the wrappers. */
    class Unknown(cause: Throwable) : NetworkException("Unknown network failure", cause)
}
