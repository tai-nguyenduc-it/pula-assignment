package com.example.pulaassignment.surveydetail

sealed interface SurveyDetailIntent {
    data object Load : SurveyDetailIntent
}
