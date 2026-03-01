package com.example.data.exception

class UnknownDataException(cause: Throwable?) : DataException(cause?.message, cause)
