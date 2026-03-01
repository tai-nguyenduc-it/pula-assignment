package com.example.domain.usecase

import com.example.domain.exception.DomainException
import com.example.domain.exception.NetworkUnavailableDomainException
import com.example.domain.exception.ServerRejectedDomainException
import com.example.domain.model.SurveyResponseDomainModel
import com.example.domain.model.SyncResultDomainModel
import com.example.domain.model.SyncStatusDomainModel
import com.example.domain.repository.SurveyRepository
import com.example.domain.repository.SurveyResponseUploadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncPendingUseCaseTest {

    private lateinit var repository: SurveyRepository
    private lateinit var uploadRepository: SurveyResponseUploadRepository
    private lateinit var syncUseCase: SyncPendingUseCase

    private val testDispatcher = UnconfinedTestDispatcher()

    // Mock data
    private val mockResponseId1 = "id-1"
    private val mockResponseId2 = "id-2"
    private val mockResponseId3 = "id-3"
    private val mockFarmerId = "farmer-1"
    private val mockSurveyId = "survey-1"
    private val mockCreatedAt = 1000L

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSurveyRepository()
        uploadRepository = FakeSurveyResponseUploadRepository()
        syncUseCase = SyncPendingUseCase(repository, uploadRepository, testDispatcher)
    }

    @Test
    fun `empty queue returns empty result`() = runTest {
        // Given
        // repository has no pending responses (empty)

        // When
        val result = syncUseCase()

        // Then
        assertTrue(result.syncedIds.isEmpty())
        assertTrue(result.failed.isEmpty())
        assertNull(result.stoppedReason)
    }

    @Test
    fun `all succeed - all marked synced and returned`() = runTest {
        // Given
        val r1 = mockResponse(mockResponseId1)
        val r2 = mockResponse(mockResponseId2)
        (repository as FakeSurveyRepository).add(r1, r2)

        // When
        val result = syncUseCase()

        // Then
        assertEquals(listOf(mockResponseId1, mockResponseId2), result.syncedIds)
        assertTrue(result.failed.isEmpty())
        assertNull(result.stoppedReason)
        assertEquals(SyncStatusDomainModel.Synced, repository.getResponseById(mockResponseId1)?.syncStatus)
        assertEquals(SyncStatusDomainModel.Synced, repository.getResponseById(mockResponseId2)?.syncStatus)
    }

    @Test
    fun `partial failure - failed in result and can retry later`() = runTest {
        // Given
        val r1 = mockResponse(mockResponseId1)
        val r2 = mockResponse(mockResponseId2)
        val r3 = mockResponse(mockResponseId3)
        (repository as FakeSurveyRepository).add(r1, r2, r3)
        val serverError = ServerRejectedDomainException(500, "error")
        uploadRepository = FakeSurveyResponseUploadRepository(
            failCallIndex = 2,
            failWith = serverError
        )
        syncUseCase = SyncPendingUseCase(repository, uploadRepository, testDispatcher)

        // When
        val result = syncUseCase()

        // Then
        assertEquals(listOf(mockResponseId1, mockResponseId3), result.syncedIds)
        assertEquals(1, result.failed.size)
        assertEquals(mockResponseId2, result.failed[0].responseId)
        assertEquals(ServerRejectedDomainException::class.java, result.failed[0].failure::class.java)
        assertEquals(500, (result.failed[0].failure as ServerRejectedDomainException).code)
        assertNull(result.stoppedReason)
    }

    @Test
    fun `network failure stops early and reports stoppedReason`() = runTest {
        // Given
        val r1 = mockResponse(mockResponseId1)
        val r2 = mockResponse(mockResponseId2)
        val r3 = mockResponse(mockResponseId3)
        (repository as FakeSurveyRepository).add(r1, r2, r3)
        uploadRepository = FakeSurveyResponseUploadRepository(
            failCallIndex = 2,
            failWith = NetworkUnavailableDomainException()
        )
        syncUseCase = SyncPendingUseCase(
            repository, uploadRepository, testDispatcher,
            SyncPendingUseCase.SyncConfig(stopAfterConsecutiveNetworkFailures = 1)
        )

        // When
        val result = syncUseCase()

        // Then
        assertEquals(listOf(mockResponseId1), result.syncedIds)
        assertEquals(1, result.failed.size)
        assertEquals(SyncResultDomainModel.StoppedReason.NetworkDown, result.stoppedReason)
    }

    @Test
    fun `only one sync at a time - no duplicate work`() = runTest {
        // Given
        val r1 = mockResponse(mockResponseId1)
        (repository as FakeSurveyRepository).add(r1)
        var result1: SyncResultDomainModel? = null
        var result2: SyncResultDomainModel? = null

        // When
        coroutineScope {
            val job1 = launch {
                result1 = syncUseCase()
            }
            val job2 = launch {
                result2 = syncUseCase()
            }
            job1.join()
            job2.join()
        }

        // Then
        val synced1 = result1!!.syncedIds
        val synced2 = result2!!.syncedIds
        assertTrue(synced1.size + synced2.size <= 1, "Only one sync should process the item")
        assertEquals(listOf(mockResponseId1), synced1 + synced2)
    }

    private fun mockResponse(id: String) = SurveyResponseDomainModel(
        id = id,
        farmerId = mockFarmerId,
        surveyId = mockSurveyId,
        answers = emptyList(),
        repeatingSections = emptyList(),
        attachmentPaths = emptyList(),
        syncStatus = SyncStatusDomainModel.Pending,
        createdAtMillis = mockCreatedAt
    )

    private class FakeSurveyRepository : SurveyRepository {
        private val storage = mutableMapOf<String, SurveyResponseDomainModel>()
        private val allList = mutableListOf<SurveyResponseDomainModel>()
        private val allFlow = kotlinx.coroutines.flow.MutableStateFlow<List<SurveyResponseDomainModel>>(emptyList())
        private val pendingFlow = kotlinx.coroutines.flow.MutableStateFlow<List<SurveyResponseDomainModel>>(emptyList())

        fun add(vararg responses: SurveyResponseDomainModel) {
            responses.forEach { storage[it.id] = it }
            updateFlows()
        }

        private fun updateFlows() {
            val all = storage.values.toList()
            allList.clear()
            allList.addAll(all)
            allFlow.value = all
            pendingFlow.value = all
                .filter { it.syncStatus == SyncStatusDomainModel.Pending || it.syncStatus == SyncStatusDomainModel.Failed }
                .sortedBy { it.createdAtMillis }
        }

        override suspend fun saveResponse(response: SurveyResponseDomainModel) {
            storage[response.id] = response
            updateFlows()
        }

        override suspend fun getResponseById(id: String) = storage[id]

        override fun getAllResponses() = allFlow

        override fun getPendingResponses() = pendingFlow

        override suspend fun getPendingResponsesOnce() = pendingFlow.value

        override suspend fun updateSyncStatus(id: String, status: SyncStatusDomainModel) {
            storage[id]?.let { storage[id] = it.copy(syncStatus = status) }
            updateFlows()
        }

        override suspend fun recordSyncFailure(id: String, attemptAtMillis: Long) {
            storage[id]?.let { model ->
                storage[id] = model.copy(
                    syncStatus = SyncStatusDomainModel.Failed,
                    retryCount = model.retryCount + 1,
                    lastAttemptAtMillis = attemptAtMillis
                )
            }
            updateFlows()
        }

        override suspend fun deleteResponse(id: String) {
            storage.remove(id)
            updateFlows()
        }

        override fun observePendingCount() = pendingFlow.map { it.size }
        override fun observeSyncedCount() = allFlow.map { list -> list.count { it.syncStatus == SyncStatusDomainModel.Synced } }
        override fun observeFailedCount() = allFlow.map { list -> list.count { it.syncStatus == SyncStatusDomainModel.Failed } }
    }

    private class FakeSurveyResponseUploadRepository(
        private val failCallIndex: Int = 0,
        private val failWith: DomainException = ServerRejectedDomainException(500, "error")
    ) : SurveyResponseUploadRepository {
        private var callCount = 0

        override suspend fun uploadResponse(response: SurveyResponseDomainModel): Result<Unit> {
            callCount++
            return if (failCallIndex > 0 && callCount == failCallIndex) {
                Result.failure(failWith)
            } else {
                Result.success(Unit)
            }
        }
    }
}
