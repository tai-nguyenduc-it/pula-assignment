package com.example.domain.usecase

import com.example.domain.exception.DomainException
import com.example.domain.exception.UnknownSyncDomainException
import com.example.domain.exception.isRetryable
import com.example.domain.model.SurveyResponseDomainModel
import com.example.domain.model.SyncResultDomainModel
import com.example.domain.model.SyncStateDomainModel
import com.example.domain.model.SyncStatusDomainModel
import com.example.domain.repository.SurveyRepository
import com.example.domain.repository.SurveyResponseUploadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class SyncPendingUseCase(
    private val repository: SurveyRepository,
    private val uploadRepository: SurveyResponseUploadRepository,
    private val dispatcher: CoroutineContext,
    private val config: SyncConfig = SyncConfig()
) {

    data class SyncConfig(
        val stopAfterConsecutiveNetworkFailures: Int = 2
    )

    private val mutex = Mutex()
    private val _state = MutableStateFlow<SyncStateDomainModel>(SyncStateDomainModel.Idle)
    val state: StateFlow<SyncStateDomainModel> = _state.asStateFlow()

    suspend operator fun invoke(): SyncResultDomainModel = withContext(dispatcher) {
        mutex.withLock {
            val pending = repository.getPendingResponsesOnce()
            if (pending.isEmpty()) {
                val result = SyncResultDomainModel(emptyList(), emptyList(), null)
                _state.value = SyncStateDomainModel.Result(result)
                return@withLock result
            }
            _state.value = SyncStateDomainModel.Syncing(pending.size, 0)
            runSync(pending)
        }
    }

    private suspend fun runSync(pending: List<SurveyResponseDomainModel>): SyncResultDomainModel {
        val syncedIds = mutableListOf<String>()
        val failed = mutableListOf<SyncResultDomainModel.FailedResponse>()
        var consecutiveNetworkFailures = 0
        var stoppedReason: SyncResultDomainModel.StoppedReason? = null

        for ((index, response) in pending.withIndex()) {
            _state.value = SyncStateDomainModel.Syncing(pending.size, index)
            val result = uploadRepository.uploadResponse(response)
            result.fold(
                onSuccess = {
                    repository.updateSyncStatus(response.id, SyncStatusDomainModel.Synced)
                    syncedIds.add(response.id)
                    consecutiveNetworkFailures = 0
                },
                onFailure = { t ->
                    val failure = t as? DomainException ?: UnknownSyncDomainException(t)
                    repository.updateSyncStatus(response.id, SyncStatusDomainModel.Failed)
                    failed.add(SyncResultDomainModel.FailedResponse(response.id, failure))
                    if (failure.isRetryable()) {
                        consecutiveNetworkFailures++
                        if (consecutiveNetworkFailures >= config.stopAfterConsecutiveNetworkFailures) {
                            stoppedReason = SyncResultDomainModel.StoppedReason.NetworkDown
                            _state.value = SyncStateDomainModel.Result(
                                SyncResultDomainModel(
                                    syncedIds.toList(),
                                    failed.toList(),
                                    stoppedReason
                                )
                            )
                            return SyncResultDomainModel(
                                syncedIds.toList(),
                                failed.toList(),
                                stoppedReason
                            )
                        }
                    } else {
                        consecutiveNetworkFailures = 0
                    }
                }
            )
        }

        val result = SyncResultDomainModel(syncedIds.toList(), failed.toList(), stoppedReason)
        _state.value = SyncStateDomainModel.Result(result)
        return result
    }
}
