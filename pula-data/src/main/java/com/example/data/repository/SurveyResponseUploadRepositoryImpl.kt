package com.example.data.repository

import com.example.data.mapper.toUploadRequestBody
import com.example.data.mapper.toDomainException
import com.example.datasource.remote.SurveyService
import com.example.domain.model.SurveyResponseDomainModel
import com.example.domain.repository.SurveyResponseUploadRepository

class SurveyResponseUploadRepositoryImpl(
    private val uploadApi: SurveyService
) : SurveyResponseUploadRepository {

    override suspend fun uploadResponse(response: SurveyResponseDomainModel): Result<Unit> {
        val apiPayload = response.toUploadRequestBody()
        return uploadApi.upload(apiPayload).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { e -> Result.failure(e.toDomainException()) }
        )
    }
}
