package com.example.domain.usecase

import com.example.domain.model.SurveyResponseDomainModel
import com.example.domain.repository.SurveyRepository
import kotlinx.coroutines.flow.Flow

class GetPendingResponsesUseCase(
    private val repository: SurveyRepository
) {
    operator fun invoke(): Flow<List<SurveyResponseDomainModel>> = repository.getPendingResponses()
}
