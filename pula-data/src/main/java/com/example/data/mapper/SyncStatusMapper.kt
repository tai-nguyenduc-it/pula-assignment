package com.example.data.mapper

import com.example.domain.model.SyncStatusDomainModel
import com.example.domain.model.SyncStatusDomainModel.Failed
import com.example.domain.model.SyncStatusDomainModel.Pending
import com.example.domain.model.SyncStatusDomainModel.Synced
import com.example.domain.model.SyncStatusDomainModel.Syncing

fun SyncStatusDomainModel.toDatabaseStatus(): String = when (this) {
    is Pending -> "Pending"
    is Syncing -> "Syncing"
    is Synced -> "Synced"
    is Failed -> "Failed"
}

fun String.toSyncStatusDomainModel(): SyncStatusDomainModel = when (this) {
    "Pending" -> Pending
    "Syncing" -> Syncing
    "Synced" -> Synced
    "Failed" -> Failed
    else -> Pending
}
