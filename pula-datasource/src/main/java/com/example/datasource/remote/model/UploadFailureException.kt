package com.example.datasource.remote.model

sealed class UploadFailureException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    data object NetworkUnavailable : UploadFailureException("Network unavailable")
    data object Timeout : UploadFailureException("Timeout")
    data class ServerError(val code: Int, val serverMessage: String?) : UploadFailureException("Server error: $code")
    data class Unknown(override val cause: Throwable?) : UploadFailureException(cause?.message, cause)
}