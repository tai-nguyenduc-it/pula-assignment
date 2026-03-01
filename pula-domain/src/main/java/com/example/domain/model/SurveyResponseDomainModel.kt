package com.example.domain.model

data class SurveyResponseDomainModel(
    val id: String,
    val farmerId: String,
    val surveyId: String,
    val answers: List<QuestionAnswerDomainModel>,
    val repeatingSections: List<RepeatingSectionDomainModel>,
    val attachmentPaths: List<String>,
    val syncStatus: SyncStatusDomainModel,
    val createdAtMillis: Long
)
