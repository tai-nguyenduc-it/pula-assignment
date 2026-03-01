package com.example.domain.repository

import com.example.domain.model.SurveyResponseDomainModel
import com.example.domain.model.SyncStatusDomainModel
import kotlinx.coroutines.flow.Flow

interface SurveyRepository {

    suspend fun saveResponse(response: SurveyResponseDomainModel)
    suspend fun getResponseById(id: String): SurveyResponseDomainModel?
    fun getAllResponses(): Flow<List<SurveyResponseDomainModel>>
    fun getPendingResponses(): Flow<List<SurveyResponseDomainModel>>
    suspend fun getPendingResponsesOnce(): List<SurveyResponseDomainModel>
    suspend fun updateSyncStatus(id: String, status: SyncStatusDomainModel)
    suspend fun recordSyncFailure(id: String, attemptAtMillis: Long)
    suspend fun deleteResponse(id: String)
    fun observePendingCount(): Flow<Int>
    fun observeSyncedCount(): Flow<Int>
    fun observeFailedCount(): Flow<Int>
}
