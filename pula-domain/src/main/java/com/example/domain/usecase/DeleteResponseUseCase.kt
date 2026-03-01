package com.example.domain.usecase

import com.example.domain.repository.SurveyRepository

class DeleteResponseUseCase(
    private val repository: SurveyRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteResponse(id)
    }
}
