package com.example.domain.usecase

import com.example.domain.repository.SurveyRepository
import kotlinx.coroutines.flow.Flow

class GetPendingCountUseCase(
    private val repository: SurveyRepository
) {
    operator fun invoke(): Flow<Int> = repository.observePendingCount()
}
