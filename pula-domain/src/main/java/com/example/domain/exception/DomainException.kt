package com.example.domain.exception

open class DomainException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

fun DomainException.isRetryable(): Boolean = when (this) {
    is NetworkUnavailableDomainException -> true
    is TimeoutDomainException -> true
    is ServerRejectedDomainException -> code >= 500
    is UnknownSyncDomainException -> true
    else -> false
}
