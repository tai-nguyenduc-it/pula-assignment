package com.example.pulaassignment.home

sealed interface HomeIntent {
    data object GoToSurveyList : HomeIntent
    data object GoToSyncDashboard : HomeIntent
}
