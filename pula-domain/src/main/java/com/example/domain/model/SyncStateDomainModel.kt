package com.example.domain.model

sealed class SyncStateDomainModel {
    data object Idle : SyncStateDomainModel()
    data class Syncing(val total: Int, val current: Int) : SyncStateDomainModel()
    data class Result(val result: SyncResultDomainModel) : SyncStateDomainModel()
}
