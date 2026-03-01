package com.example.data.mapper

import com.example.data.exception.DataException
import com.example.data.exception.UploadNetworkDataException
import com.example.data.exception.UploadTimeoutDataException
import com.example.data.exception.UploadUnknownDataException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toDataException(): DataException = when (this) {
    is UnknownHostException -> UploadNetworkDataException()
    is SocketTimeoutException -> UploadTimeoutDataException()
    else -> UploadUnknownDataException(this)
}
