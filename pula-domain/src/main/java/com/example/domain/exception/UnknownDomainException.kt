package com.example.domain.exception

class UnknownDomainException(cause: Throwable?) : DomainException(cause?.message, cause)
