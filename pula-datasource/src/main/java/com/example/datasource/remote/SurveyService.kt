package com.example.datasource.remote

import com.example.datasource.remote.model.SurveyUploadRequestApiModel

interface SurveyService {
    suspend fun upload(payload: SurveyUploadRequestApiModel): Result<Unit>
}
