package com.example.domain.usecase

import com.example.domain.model.SurveyResponseDomainModel
import com.example.domain.repository.SurveyRepository

class SubmitSurveyUseCase(
    private val repository: SurveyRepository
) {
    suspend operator fun invoke(response: SurveyResponseDomainModel) {
        repository.saveResponse(response)
    }
}
