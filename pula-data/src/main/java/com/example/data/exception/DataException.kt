package com.example.data.exception

open class DataException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)
