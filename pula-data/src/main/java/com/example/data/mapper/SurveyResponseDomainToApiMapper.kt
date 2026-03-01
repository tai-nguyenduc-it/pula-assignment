package com.example.data.mapper

import com.example.datasource.remote.model.SurveyUploadRequestApiModel
import com.example.datasource.remote.model.SurveyUploadRequestApiModel.QuestionAnswer
import com.example.datasource.remote.model.SurveyUploadRequestApiModel.RepeatingSection
import com.example.domain.model.SurveyResponseDomainModel

fun SurveyResponseDomainModel.toUploadRequestBody() = SurveyUploadRequestApiModel(
    id = id,
    farmerId = farmerId,
    surveyId = surveyId,
    answers = answers.map { QuestionAnswer(it.questionId, it.value) },
    repeatingSections = repeatingSections.map { section ->
        RepeatingSection(
            sectionId = section.sectionId,
            answers = section.answers.map { QuestionAnswer(it.questionId, it.value) }
        )
    },
    attachmentPaths = attachmentPaths,
    createdAtMillis = createdAtMillis
)
