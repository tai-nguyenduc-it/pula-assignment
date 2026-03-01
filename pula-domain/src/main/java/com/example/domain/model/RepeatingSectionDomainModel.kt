package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RepeatingSectionDomainModel(
    val sectionId: String,
    val answers: List<QuestionAnswerDomainModel>
)
