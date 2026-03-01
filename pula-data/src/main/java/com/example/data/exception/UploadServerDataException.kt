package com.example.data.exception

class UploadServerDataException(
    val code: Int,
    val serverMessage: String?
) : DataException("Server error: $code, message: $serverMessage")
