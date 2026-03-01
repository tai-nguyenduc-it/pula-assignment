package com.example.pulaassignment.createsurvey

sealed interface CreateSurveyIntent {
    data class SetFarmerName(val value: String) : CreateSurveyIntent
    data class SetNumberOfFarms(val value: Int) : CreateSurveyIntent
    data class SetFarmCrop(val index: Int, val value: String) : CreateSurveyIntent
    data class SetFarmArea(val index: Int, val value: String) : CreateSurveyIntent
    data class SetFarmYield(val index: Int, val value: String) : CreateSurveyIntent
    data object AddPhoto : CreateSurveyIntent
    data class RemovePhoto(val index: Int) : CreateSurveyIntent
    data object Submit : CreateSurveyIntent
}
