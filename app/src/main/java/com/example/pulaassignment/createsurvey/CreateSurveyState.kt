package com.example.pulaassignment.createsurvey

data class FarmRow(
    val cropType: String = "",
    val area: String = "",
    val yield: String = ""
)

data class CreateSurveyState(
    val farmerName: String = "",
    val numberOfFarms: Int = 1,
    val farmRows: List<FarmRow> = listOf(FarmRow()),
    val attachmentPaths: List<String> = emptyList(),
    val isSubmitting: Boolean = false
)
