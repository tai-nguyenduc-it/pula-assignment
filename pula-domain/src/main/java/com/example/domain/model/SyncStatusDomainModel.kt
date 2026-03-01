package com.example.domain.model

sealed class SyncStatusDomainModel {
    data object Pending : SyncStatusDomainModel()
    data object Syncing : SyncStatusDomainModel()
    data object Synced : SyncStatusDomainModel()
    data object Failed : SyncStatusDomainModel()
}
