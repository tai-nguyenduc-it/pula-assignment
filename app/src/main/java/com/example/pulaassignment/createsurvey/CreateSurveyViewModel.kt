package com.example.pulaassignment.createsurvey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.QuestionAnswerDomainModel
import com.example.domain.model.RepeatingSectionDomainModel
import com.example.domain.model.SurveyResponseDomainModel
import com.example.domain.model.SyncStatusDomainModel
import com.example.domain.usecase.SubmitSurveyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateSurveyViewModel @Inject constructor(
    private val submitSurveyUseCase: SubmitSurveyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateSurveyState())
    val state: StateFlow<CreateSurveyState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CreateSurveyEffect>(replay = 0, extraBufferCapacity = 2)
    val effect: SharedFlow<CreateSurveyEffect> = _effect.asSharedFlow()

    fun dispatch(intent: CreateSurveyIntent) {
        viewModelScope.launch {
            when (intent) {
                is CreateSurveyIntent.SetFarmerName -> _state.update {
                    it.copy(farmerName = intent.value)
                }
                is CreateSurveyIntent.SetNumberOfFarms -> {
                    val n = intent.value.coerceIn(1, 50).takeIf { it > 0 } ?: 1
                    _state.update { state ->
                        val current = state.farmRows
                        val newRows = if (n > current.size) {
                            current + List(n - current.size) { FarmRow() }
                        } else current.take(n)
                        state.copy(numberOfFarms = n, farmRows = newRows)
                    }
                }
                is CreateSurveyIntent.SetFarmCrop -> _state.update { state ->
                    val rows = state.farmRows.toMutableList()
                    if (intent.index in rows.indices) {
                        rows[intent.index] = rows[intent.index].copy(cropType = intent.value)
                        state.copy(farmRows = rows)
                    } else state
                }
                is CreateSurveyIntent.SetFarmArea -> _state.update { state ->
                    val rows = state.farmRows.toMutableList()
                    if (intent.index in rows.indices) {
                        rows[intent.index] = rows[intent.index].copy(area = intent.value)
                        state.copy(farmRows = rows)
                    } else state
                }
                is CreateSurveyIntent.SetFarmYield -> _state.update { state ->
                    val rows = state.farmRows.toMutableList()
                    if (intent.index in rows.indices) {
                        rows[intent.index] = rows[intent.index].copy(yield = intent.value)
                        state.copy(farmRows = rows)
                    } else state
                }
                CreateSurveyIntent.AddPhoto -> _state.update { state ->
                    state.copy(
                        attachmentPaths = state.attachmentPaths + "file:///photo_${state.attachmentPaths.size + 1}"
                    )
                }
                is CreateSurveyIntent.RemovePhoto -> _state.update { state ->
                    state.copy(
                        attachmentPaths = state.attachmentPaths.filterIndexed { i, _ -> i != intent.index }
                    )
                }
                CreateSurveyIntent.Submit -> submit()
            }
        }
    }

    private suspend fun submit() {
        val state = _state.value
        if (state.farmerName.isBlank()) {
            _effect.emit(CreateSurveyEffect.ShowError("Enter farmer name"))
            return
        }
        _state.update { it.copy(isSubmitting = true) }
        val id = "survey-${UUID.randomUUID()}"
        val answers = listOf(
            QuestionAnswerDomainModel("farmer_name", state.farmerName),
            QuestionAnswerDomainModel("number_of_farms", state.numberOfFarms.toString())
        )
        val repeatingSections = state.farmRows.mapIndexed { index, row ->
            RepeatingSectionDomainModel(
                sectionId = "farm_$index",
                answers = listOf(
                    QuestionAnswerDomainModel("crop_type", row.cropType),
                    QuestionAnswerDomainModel("area", row.area),
                    QuestionAnswerDomainModel("yield", row.yield)
                )
            )
        }
        val response = SurveyResponseDomainModel(
            id = id,
            farmerId = "local",
            surveyId = "survey-1",
            answers = answers,
            repeatingSections = repeatingSections,
            attachmentPaths = state.attachmentPaths,
            syncStatus = SyncStatusDomainModel.Pending,
            createdAtMillis = System.currentTimeMillis()
        )
        submitSurveyUseCase(response)
        _state.update { it.copy(isSubmitting = false) }
        _effect.emit(CreateSurveyEffect.NavigateBack)
    }
}

sealed interface CreateSurveyEffect {
    data class ShowError(val message: String) : CreateSurveyEffect
    data object NavigateBack : CreateSurveyEffect
}
