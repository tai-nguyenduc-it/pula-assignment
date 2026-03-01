package com.example.domain.exception

class ServerRejectedDomainException(
    val code: Int,
    val serverMessage: String?
) : DomainException("Server error: $code, message: $serverMessage")
