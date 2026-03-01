package com.example.pulaassignment.surveydetail

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.model.SurveyResponseDomainModel
import com.example.pulaassignment.ui.PulaTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: SurveyDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SurveyDetailEffect.ShowToast -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { PulaTopAppBar(title = "Survey Detail", onBackClick = onNavigateBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                Text("Loading...", modifier = Modifier.padding(16.dp))
                return@Column
            }
            val survey = state.survey
            if (survey == null) {
                Text("Survey not found", modifier = Modifier.padding(16.dp))
                return@Column
            }

            Text(
                "Survey #${survey.id.takeLast(8)}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Sync status: ${survey.syncStatus::class.simpleName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Retry count: ${survey.retryCount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
            survey.lastAttemptAtMillis?.let { ts ->
                Text(
                    "Last attempt: ${formatTime(ts)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    OutlinedButton(
                onClick = { viewModel.setJsonExpanded(!state.jsonExpanded) },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(if (state.jsonExpanded) "Collapse JSON" else "Expand JSON payload")
            }
            if (state.jsonExpanded) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = jsonPayload(survey),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Text(
                "Attachments (${survey.attachmentPaths.size})",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp)
            )
            survey.attachmentPaths.forEachIndexed { i, path ->
                Text("  ${i + 1}. $path", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(millis))

private fun jsonPayload(survey: SurveyResponseDomainModel): String =
    buildString {
        append("{\n")
        append("  \"id\": \"${survey.id}\",\n")
        append("  \"farmerId\": \"${survey.farmerId}\",\n")
        append("  \"surveyId\": \"${survey.surveyId}\",\n")
        append("  \"answers\": ${survey.answers},\n")
        append("  \"repeatingSections\": ${survey.repeatingSections},\n")
        append("  \"attachmentPaths\": ${survey.attachmentPaths},\n")
        append("  \"syncStatus\": \"${survey.syncStatus::class.simpleName}\",\n")
        append("  \"createdAtMillis\": ${survey.createdAtMillis},\n")
        append("  \"retryCount\": ${survey.retryCount}\n")
        append("}")
    }
