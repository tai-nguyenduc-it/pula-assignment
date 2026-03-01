package com.example.pulaassignment.surveylist

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.model.SurveyResponseDomainModel
import com.example.domain.model.SyncStatusDomainModel
import com.example.pulaassignment.ui.PulaTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyListScreen(
    modifier: Modifier = Modifier,
    viewModel: SurveyListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var surveyToDelete by remember { mutableStateOf<SurveyResponseDomainModel?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SurveyListEffect.NavigateToCreateSurvey -> onNavigateToCreate()
                is SurveyListEffect.NavigateToDetail -> onNavigateToDetail(effect.surveyId)
                is SurveyListEffect.ShowToast -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { PulaTopAppBar(title = "Survey List", onBackClick = onNavigateBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { viewModel.dispatch(SurveyListIntent.CreateSurvey) }) {
                    Text("Create Survey")
                }
                Button(onClick = { viewModel.dispatch(SurveyListIntent.GenerateSurveys) }) {
                    Text("Generate Survey (10)")
                }
            }
            if (state.isLoading) {
                Text("Loading...", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("# ID", style = MaterialTheme.typography.labelMedium)
                            Text("Created", style = MaterialTheme.typography.labelMedium)
                            Text("Status", style = MaterialTheme.typography.labelMedium)
                            Text("", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    items(state.surveys, key = { it.id }) { survey ->
                        SurveyRow(
                            survey = survey,
                            onClick = { viewModel.dispatch(SurveyListIntent.OpenDetail(survey)) },
                            onDelete = { surveyToDelete = survey }
                        )
                    }
                }
            }
        }

        surveyToDelete?.let { survey ->
            AlertDialog(
                onDismissRequest = { surveyToDelete = null },
                title = { Text("Delete survey?") },
                text = { Text("Delete #${survey.id}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dispatch(SurveyListIntent.DeleteSurvey(survey))
                            surveyToDelete = null
                        }
                    ) { Text("Delete") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { surveyToDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun SurveyRow(
    survey: SurveyResponseDomainModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("#${survey.id.takeLast(8)}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    formatTime(survey.createdAtMillis),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                syncStatusLabel(survey),
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onDelete) {
                Text("🗑", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
}

private fun syncStatusLabel(survey: SurveyResponseDomainModel): String = when (survey.syncStatus) {
    SyncStatusDomainModel.Pending -> "PENDING"
    SyncStatusDomainModel.Synced -> "SUCCESS"
    SyncStatusDomainModel.Failed -> "FAILED_RETRYABLE (${survey.retryCount})"
    SyncStatusDomainModel.Syncing -> "SYNCING"
}
