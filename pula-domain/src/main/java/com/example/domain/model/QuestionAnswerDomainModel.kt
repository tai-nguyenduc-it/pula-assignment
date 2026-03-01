package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestionAnswerDomainModel(
    val questionId: String,
    val value: String
)
