package com.example.pulaassignment.syncdashboard

import com.example.domain.model.SyncResultDomainModel
import com.example.domain.model.SyncStateDomainModel

data class SyncDashboardState(
    val pendingCount: Int = 0,
    val syncedCount: Int = 0,
    val failedCount: Int = 0,
    val syncState: SyncStateDomainModel = SyncStateDomainModel.Idle,
    val lastSyncResult: SyncResultDomainModel? = null
)
