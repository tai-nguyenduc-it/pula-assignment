package com.example.domain.usecase

import com.example.domain.model.SurveyResponseDomainModel
import com.example.domain.repository.SurveyRepository

class GetResponseByIdUseCase(
    private val repository: SurveyRepository
) {
    suspend operator fun invoke(id: String): SurveyResponseDomainModel? = repository.getResponseById(id)
}
