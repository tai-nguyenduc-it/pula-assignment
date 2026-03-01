package com.example.pulaassignment.surveylist

import com.example.domain.model.SurveyResponseDomainModel

sealed interface SurveyListIntent {
    data object LoadList : SurveyListIntent
    data object CreateSurvey : SurveyListIntent
    data object GenerateSurveys : SurveyListIntent
    data class OpenDetail(val survey: SurveyResponseDomainModel) : SurveyListIntent
    data class DeleteSurvey(val survey: SurveyResponseDomainModel) : SurveyListIntent
}
