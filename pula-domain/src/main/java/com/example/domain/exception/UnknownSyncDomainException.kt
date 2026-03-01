package com.example.domain.exception

class UnknownSyncDomainException(cause: Throwable?) : DomainException(cause?.message, cause)
