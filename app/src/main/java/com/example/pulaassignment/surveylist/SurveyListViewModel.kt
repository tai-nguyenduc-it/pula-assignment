package com.example.pulaassignment.surveylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.QuestionAnswerDomainModel
import com.example.domain.model.SurveyResponseDomainModel
import com.example.domain.model.SyncStatusDomainModel
import com.example.domain.usecase.DeleteResponseUseCase
import com.example.domain.usecase.GetAllResponsesUseCase
import com.example.domain.usecase.SubmitSurveyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SurveyListViewModel @Inject constructor(
    private val getAllResponsesUseCase: GetAllResponsesUseCase,
    private val deleteResponseUseCase: DeleteResponseUseCase,
    private val submitSurveyUseCase: SubmitSurveyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SurveyListState())
    val state: StateFlow<SurveyListState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SurveyListEffect>(replay = 0, extraBufferCapacity = 8)
    val effect: SharedFlow<SurveyListEffect> = _effect.asSharedFlow()

    init {
        dispatch(SurveyListIntent.LoadList)
    }

    fun dispatch(intent: SurveyListIntent) {
        viewModelScope.launch {
            when (intent) {
                SurveyListIntent.LoadList -> loadList()
                SurveyListIntent.CreateSurvey -> _effect.emit(SurveyListEffect.NavigateToCreateSurvey)
                SurveyListIntent.GenerateSurveys -> generateSurveys()
                is SurveyListIntent.OpenDetail -> _effect.emit(
                    SurveyListEffect.NavigateToDetail(
                        intent.survey.id
                    )
                )

                is SurveyListIntent.DeleteSurvey -> deleteSurvey(intent.survey)
            }
        }
    }

    private fun loadList() {
        viewModelScope.launch {
            getAllResponsesUseCase()
                .catch { _state.update { it.copy(isLoading = false) } }
                .collect { list ->
                    _state.update { it.copy(surveys = list, isLoading = false) }
                }
        }
        _state.update { it.copy(isLoading = true) }
    }

    private suspend fun generateSurveys() {
        _state.update { it.copy(isLoading = true) }
        withContext(Dispatchers.Default) {
            repeat(10) { i ->
                val id = "gen-${UUID.randomUUID()}"
                submitSurveyUseCase(
                    SurveyResponseDomainModel(
                        id = id,
                        farmerId = "farmer-$i",
                        surveyId = "survey-1",
                        answers = listOf(
                            QuestionAnswerDomainModel("farmer_name", "Generated Farmer $i")
                        ),
                        repeatingSections = emptyList(),
                        attachmentPaths = emptyList(),
                        syncStatus = SyncStatusDomainModel.Pending,
                        createdAtMillis = System.currentTimeMillis()
                    )
                )
            }
        }
        _state.update { it.copy(isLoading = false) }
        _effect.emit(SurveyListEffect.ShowToast("Generated 10 surveys"))
        loadList()
    }

    private suspend fun deleteSurvey(survey: SurveyResponseDomainModel) {
        deleteResponseUseCase(survey.id)
        _effect.emit(SurveyListEffect.ShowToast("Deleted"))
        loadList()
    }
}

sealed interface SurveyListEffect {
    data object NavigateToCreateSurvey : SurveyListEffect
    data class NavigateToDetail(val surveyId: String) : SurveyListEffect
    data class ShowToast(val message: String) : SurveyListEffect
}
