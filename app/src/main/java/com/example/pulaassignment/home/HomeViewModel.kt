package com.example.pulaassignment.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>(replay = 0, extraBufferCapacity = 1)
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    fun dispatch(intent: HomeIntent) {
        viewModelScope.launch {
            when (intent) {
                HomeIntent.GoToSurveyList -> _effect.emit(HomeEffect.NavigateToSurveyList)
                HomeIntent.GoToSyncDashboard -> _effect.emit(HomeEffect.NavigateToSyncDashboard)
            }
        }
    }
}

sealed interface HomeEffect {
    data object NavigateToSurveyList : HomeEffect
    data object NavigateToSyncDashboard : HomeEffect
}
