package com.example.pulaassignment.navigation

object Routes {
    const val HOME = "home"
    const val SURVEY_LIST = "survey_list"
    const val CREATE_SURVEY = "create_survey"
    const val SURVEY_DETAIL = "survey_detail/{surveyId}"
    const val SYNC_DASHBOARD = "sync_dashboard"

    fun surveyDetail(surveyId: String) = "survey_detail/$surveyId"
}
