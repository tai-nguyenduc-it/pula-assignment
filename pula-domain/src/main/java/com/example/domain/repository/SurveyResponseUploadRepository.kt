package com.example.domain.repository

import com.example.domain.model.SurveyResponseDomainModel

interface SurveyResponseUploadRepository {

    suspend fun uploadResponse(response: SurveyResponseDomainModel): Result<Unit>
}
