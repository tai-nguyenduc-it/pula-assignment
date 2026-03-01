package com.example.data.repository

import com.example.data.mapper.toDatabase
import com.example.data.mapper.toDatabaseStatus
import com.example.data.mapper.toDomain
import com.example.datasource.local.SurveyDao
import com.example.domain.model.SurveyResponseDomainModel
import com.example.domain.model.SyncStatusDomainModel
import com.example.domain.repository.SurveyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SurveyRepositoryImpl(
    private val dao: SurveyDao
) : SurveyRepository {

    override suspend fun saveResponse(response: SurveyResponseDomainModel) {
        dao.insert(response.toDatabase())
    }

    override suspend fun getResponseById(id: String): SurveyResponseDomainModel? =
        dao.getById(id)?.toDomain()

    override fun getAllResponses(): Flow<List<SurveyResponseDomainModel>> =
        dao.getAllFlow().map { list -> list.map { it.toDomain() } }

    override fun getPendingResponses(): Flow<List<SurveyResponseDomainModel>> =
        dao.getPendingFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun getPendingResponsesOnce(): List<SurveyResponseDomainModel> =
        dao.getPendingOnce().map { it.toDomain() }

    override suspend fun updateSyncStatus(id: String, status: SyncStatusDomainModel) {
        dao.updateStatus(id, status.toDatabaseStatus())
    }

    override suspend fun recordSyncFailure(id: String, attemptAtMillis: Long) {
        dao.recordFailure(id, attemptAtMillis)
    }

    override suspend fun deleteResponse(id: String) {
        dao.deleteById(id)
    }

    override fun observePendingCount(): Flow<Int> = dao.observePendingCount()
    override fun observeSyncedCount(): Flow<Int> = dao.observeSyncedCount()
    override fun observeFailedCount(): Flow<Int> = dao.observeFailedCount()
}
