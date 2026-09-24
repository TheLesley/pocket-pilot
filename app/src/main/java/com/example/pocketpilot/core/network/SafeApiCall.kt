package com.example.pocketpilot.core.network

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import okio.IOException
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Executes a Retrofit-suspending call, translating raw exceptions into the
 * [NetworkException] hierarchy. Coroutine cancellation is preserved.
 */
suspend inline fun <T> safeApiCall(crossinline call: suspend () -> T): T = try {
    call()
} catch (ce: CancellationException) {
    throw ce
} catch (t: Throwable) {
    throw t.toNetworkException()
}

fun Throwable.toNetworkException(): NetworkException = when (this) {
    is NetworkException -> this
    is SocketTimeoutException -> NetworkException.Timeout(this)
    is UnknownHostException -> NetworkException.NoConnectivity(this)
    is IOException -> NetworkException.NoConnectivity(this)
    is SerializationException -> NetworkException.Serialization(this)
    is HttpException -> toHttpNetworkException()
    else -> NetworkException.Unknown(this)
}

private fun HttpException.toHttpNetworkException(): NetworkException {
    val body: String? = runCatching { response()?.errorBody()?.string() }.getOrNull()
    return when (val status = code()) {
        401 -> NetworkException.Unauthorized(body, this)
        in 400..499 -> NetworkException.ClientError(status, body, this)
        in 500..599 -> NetworkException.ServerError(status, body, this)
        else -> NetworkException.Unknown(this)
    }
}
