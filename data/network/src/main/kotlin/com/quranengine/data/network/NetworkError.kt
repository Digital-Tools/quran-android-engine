package com.quranengine.data.network

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class NetworkError(message: String, cause: Throwable? = null) : IOException(message, cause) {
    class Unknown(cause: Throwable? = null) : NetworkError("Unknown network error", cause)
    class NotConnectedToInternet(cause: Throwable? = null) : NetworkError("Not connected to the internet", cause)
    class ConnectionLost(cause: Throwable? = null) : NetworkError("Connection lost", cause)
    class ServerNotReachable(cause: Throwable? = null) : NetworkError("Server not reachable", cause)
    class ServerError(val errorMessage: String) : NetworkError(errorMessage)

    companion object {
        fun from(error: Throwable): NetworkError = when {
            error is NetworkError -> error
            error is UnknownHostException -> ServerNotReachable(error)
            error is ConnectException -> ServerNotReachable(error)
            error is SocketTimeoutException -> ServerNotReachable(error)
            error.message?.contains("connection", ignoreCase = true) == true -> ConnectionLost(error)
            else -> Unknown(error)
        }
    }
}
