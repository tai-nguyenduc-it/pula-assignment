package com.example.datasource.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SurveyUploadRequestApiModel(
    val id: String,
    val farmerId: String,
    val surveyId: String,
    val answers: List<QuestionAnswer>,
    val repeatingSections: List<RepeatingSection>,
    val attachmentPaths: List<String>,
    val createdAtMillis: Long
) {
    @Serializable
    data class QuestionAnswer(val questionId: String, val value: String)

    @Serializable
    data class RepeatingSection(val sectionId: String, val answers: List<QuestionAnswer>)
}
