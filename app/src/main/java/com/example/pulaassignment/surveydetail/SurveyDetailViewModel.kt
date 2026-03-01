package com.example.pulaassignment.surveydetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetResponseByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getResponseByIdUseCase: GetResponseByIdUseCase
) : ViewModel() {

    private val surveyId: String = checkNotNull(savedStateHandle["surveyId"])

    private val _state = MutableStateFlow(SurveyDetailState())
    val state: StateFlow<SurveyDetailState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SurveyDetailEffect>(replay = 0, extraBufferCapacity = 4)
    val effect: SharedFlow<SurveyDetailEffect> = _effect.asSharedFlow()

    init {
        dispatch(SurveyDetailIntent.Load)
    }

    fun dispatch(intent: SurveyDetailIntent) {
        viewModelScope.launch {
            when (intent) {
                SurveyDetailIntent.Load -> load()
            }
        }
    }

    fun setJsonExpanded(expanded: Boolean) {
        _state.update { it.copy(jsonExpanded = expanded) }
    }

    private suspend fun load() {
        _state.update { it.copy(isLoading = true) }
        val survey = getResponseByIdUseCase(surveyId)
        _state.update { it.copy(survey = survey, isLoading = false) }
    }
}

sealed interface SurveyDetailEffect {
    data class ShowToast(val message: String) : SurveyDetailEffect
}
