package com.example.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "survey_response")
data class SurveyDatabaseModel(
    @PrimaryKey val id: String,
    val farmerId: String,
    val surveyId: String,
    val answersJson: String,
    val repeatingSectionsJson: String,
    val attachmentPathsJson: String,
    val syncStatus: String,             // Pending | Synced | Failed
    val createdAtMillis: Long,
    val retryCount: Int = 0,
    val lastAttemptAtMillis: Long? = null
)
