package com.example.pulaassignment.surveydetail

import com.example.domain.model.SurveyResponseDomainModel

data class SurveyDetailState(
    val survey: SurveyResponseDomainModel? = null,
    val isLoading: Boolean = false,
    val jsonExpanded: Boolean = false
)
