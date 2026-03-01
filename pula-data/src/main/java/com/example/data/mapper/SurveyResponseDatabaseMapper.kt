package com.example.data.mapper

import com.example.datasource.local.model.SurveyDatabaseModel
import com.example.domain.model.QuestionAnswerDomainModel
import com.example.domain.model.RepeatingSectionDomainModel
import com.example.domain.model.SurveyResponseDomainModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun SurveyResponseDomainModel.toDatabase() = SurveyDatabaseModel(
    id = id,
    farmerId = farmerId,
    surveyId = surveyId,
    answersJson = json.encodeToString(answers),
    repeatingSectionsJson = json.encodeToString(repeatingSections),
    attachmentPathsJson = json.encodeToString(attachmentPaths),
    syncStatus = syncStatus.toDatabaseStatus(),
    createdAtMillis = createdAtMillis,
    retryCount = retryCount,
    lastAttemptAtMillis = lastAttemptAtMillis
)

fun SurveyDatabaseModel.toDomain() = SurveyResponseDomainModel(
    id = id,
    farmerId = farmerId,
    surveyId = surveyId,
    answers = json.decodeFromString<List<QuestionAnswerDomainModel>>(answersJson),
    repeatingSections = json.decodeFromString<List<RepeatingSectionDomainModel>>(
        repeatingSectionsJson
    ),
    attachmentPaths = json.decodeFromString<List<String>>(attachmentPathsJson),
    syncStatus = syncStatus.toSyncStatusDomainModel(),
    createdAtMillis = createdAtMillis,
    retryCount = retryCount,
    lastAttemptAtMillis = lastAttemptAtMillis
)