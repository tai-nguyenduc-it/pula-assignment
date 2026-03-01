package com.example.pulaassignment.surveylist

import com.example.domain.model.SurveyResponseDomainModel

data class SurveyListState(
    val surveys: List<SurveyResponseDomainModel> = emptyList(),
    val isLoading: Boolean = false
)
