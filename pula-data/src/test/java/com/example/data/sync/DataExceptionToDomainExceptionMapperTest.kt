package com.example.data.sync

import com.example.data.exception.UploadNetworkDataException
import com.example.data.exception.UploadServerDataException
import com.example.data.exception.UploadTimeoutDataException
import com.example.data.exception.UploadUnknownDataException
import com.example.data.mapper.toDomainException
import com.example.domain.exception.NetworkUnavailableDomainException
import com.example.domain.exception.ServerRejectedDomainException
import com.example.domain.exception.TimeoutDomainException
import com.example.domain.exception.UnknownSyncDomainException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class DataExceptionToDomainExceptionMapperTest {

    // Mock data
    private val mockServerCode = 500
    private val mockServerMessage = "error"
    private val mockCauseMessage = "test"

    @Test
    fun `UploadNetworkDataException maps to NetworkUnavailableDomainException`() {
        // Given
        val dataException = UploadNetworkDataException()

        // When
        val result = dataException.toDomainException()

        // Then
        assertEquals(NetworkUnavailableDomainException::class.java, result::class.java)
    }

    @Test
    fun `UploadTimeoutDataException maps to TimeoutDomainException`() {
        // Given
        val dataException = UploadTimeoutDataException()

        // When
        val result = dataException.toDomainException()

        // Then
        assertEquals(TimeoutDomainException::class.java, result::class.java)
    }

    @Test
    fun `UploadServerDataException maps to ServerRejectedDomainException`() {
        // Given
        val dataException = UploadServerDataException(mockServerCode, mockServerMessage)

        // When
        val result = dataException.toDomainException()

        // Then
        assertEquals(ServerRejectedDomainException::class.java, result::class.java)
        assertEquals(mockServerCode, (result as ServerRejectedDomainException).code)
        assertEquals(mockServerMessage, result.serverMessage)
    }

    @Test
    fun `UploadUnknownDataException maps to UnknownSyncDomainException`() {
        // Given
        val cause = RuntimeException(mockCauseMessage)
        val dataException = UploadUnknownDataException(cause)

        // When
        val result = dataException.toDomainException()

        // Then
        assertEquals(UnknownSyncDomainException::class.java, result::class.java)
        assertEquals(cause, (result as UnknownSyncDomainException).cause)
    }

    @Test
    fun `Throwable toDomainException - UnknownHostException maps to NetworkUnavailableDomainException`() {
        // Given
        val throwable = UnknownHostException()

        // When
        val result = throwable.toDomainException()

        // Then
        assertEquals(NetworkUnavailableDomainException::class.java, result::class.java)
    }

    @Test
    fun `Throwable toDomainException - SocketTimeoutException maps to TimeoutDomainException`() {
        // Given
        val throwable = SocketTimeoutException()

        // When
        val result = throwable.toDomainException()

        // Then
        assertEquals(TimeoutDomainException::class.java, result::class.java)
    }

    @Test
    fun `Throwable toDomainException - unknown maps to UnknownSyncDomainException`() {
        // Given
        val cause = RuntimeException(mockCauseMessage)

        // When
        val result = cause.toDomainException()

        // Then
        assertEquals(UnknownSyncDomainException::class.java, result::class.java)
    }
}
