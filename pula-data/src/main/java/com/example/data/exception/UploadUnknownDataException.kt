package com.example.data.exception

class UploadUnknownDataException(cause: Throwable?) : DataException(cause?.message, cause)
