package com.example.domain.model

import com.example.domain.exception.DomainException

data class SyncResultDomainModel(
    val syncedIds: List<String>,
    val failed: List<FailedResponse>,
    val stoppedReason: StoppedReason? = null
) {
    data class FailedResponse(
        val responseId: String,
        val failure: DomainException
    )

    sealed class StoppedReason {
        data object NetworkDown : StoppedReason()
        data object TooManyFailures : StoppedReason()
    }
}
