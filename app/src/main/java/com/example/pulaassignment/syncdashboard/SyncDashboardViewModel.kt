package com.example.pulaassignment.syncdashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.SyncStateDomainModel
import com.example.domain.repository.SurveyRepository
import com.example.domain.usecase.SyncPendingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncDashboardViewModel @Inject constructor(
    private val syncPendingUseCase: SyncPendingUseCase,
    private val surveyRepository: SurveyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SyncDashboardState())
    val state: StateFlow<SyncDashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                surveyRepository.observePendingCount(),
                surveyRepository.observeSyncedCount(),
                surveyRepository.observeFailedCount(),
                syncPendingUseCase.state
            ) { pending, synced, failed, syncState ->
                val lastResult = when (syncState) {
                    is SyncStateDomainModel.Result -> syncState.result
                    else -> _state.value.lastSyncResult
                }
                SyncDashboardState(
                    pendingCount = pending,
                    syncedCount = synced,
                    failedCount = failed,
                    syncState = syncState,
                    lastSyncResult = lastResult
                )
            }.collect { _state.value = it }
        }
    }

    fun dispatch(intent: SyncDashboardIntent) {
        viewModelScope.launch {
            when (intent) {
                SyncDashboardIntent.StartSync -> syncPendingUseCase()
            }
        }
    }
}
