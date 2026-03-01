package com.example.pulaassignment.syncdashboard

sealed interface SyncDashboardIntent {
    data object StartSync : SyncDashboardIntent
}
