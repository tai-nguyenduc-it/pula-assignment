package com.example.data.mapper

import com.example.data.exception.DataException
import com.example.data.exception.UploadNetworkDataException
import com.example.data.exception.UploadServerDataException
import com.example.data.exception.UploadTimeoutDataException
import com.example.data.exception.UploadUnknownDataException
import com.example.datasource.remote.model.UploadFailureException

fun UploadFailureException.toDataException(): DataException = when (this) {
    is UploadFailureException.NetworkUnavailable -> UploadNetworkDataException()
    is UploadFailureException.Timeout -> UploadTimeoutDataException()
    is UploadFailureException.ServerError -> UploadServerDataException(code, serverMessage)
    is UploadFailureException.Unknown -> UploadUnknownDataException(cause)
}
