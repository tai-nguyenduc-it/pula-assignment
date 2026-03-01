package com.example.data.mapper

import com.example.data.exception.DataException
import com.example.data.exception.UploadNetworkDataException
import com.example.data.exception.UploadServerDataException
import com.example.data.exception.UploadTimeoutDataException
import com.example.data.exception.UploadUnknownDataException
import com.example.datasource.remote.model.UploadFailureException
import com.example.domain.exception.DomainException
import com.example.domain.exception.NetworkUnavailableDomainException
import com.example.domain.exception.ServerRejectedDomainException
import com.example.domain.exception.TimeoutDomainException
import com.example.domain.exception.UnknownSyncDomainException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun DataException.toDomainException(): DomainException = when (this) {
    is UploadNetworkDataException -> NetworkUnavailableDomainException()
    is UploadTimeoutDataException -> TimeoutDomainException()
    is UploadServerDataException -> ServerRejectedDomainException(code, serverMessage)
    is UploadUnknownDataException -> UnknownSyncDomainException(cause)
    else -> UnknownSyncDomainException(this)
}

fun Throwable.toDomainException(): DomainException = when (this) {
    is DomainException -> this
    is DataException -> this.toDomainException()
    is UploadFailureException -> this.toDataException().toDomainException()
    is UnknownHostException -> NetworkUnavailableDomainException()
    is SocketTimeoutException -> TimeoutDomainException()
    else -> UnknownSyncDomainException(this)
}
