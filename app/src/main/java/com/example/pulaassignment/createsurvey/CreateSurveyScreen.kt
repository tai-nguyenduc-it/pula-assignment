package com.example.pulaassignment.createsurvey

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pulaassignment.ui.PulaTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSurveyScreen(
    viewModel: CreateSurveyViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CreateSurveyEffect.ShowError -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT
                ).show()

                CreateSurveyEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { PulaTopAppBar(title = "Create Survey", onBackClick = onNavigateBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.farmerName,
                onValueChange = { viewModel.dispatch(CreateSurveyIntent.SetFarmerName(it)) },
                label = { Text("Farmer name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = state.numberOfFarms.toString(),
                onValueChange = {
                    viewModel.dispatch(
                        CreateSurveyIntent.SetNumberOfFarms(
                            it.toIntOrNull() ?: 1
                        )
                    )
                },
                label = { Text("Number of farms") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Text(
                "Farms (repeating)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp)
            )
            state.farmRows.forEachIndexed { index, row ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text("Farm ${index + 1}", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = row.cropType,
                            onValueChange = {
                                viewModel.dispatch(
                                    CreateSurveyIntent.SetFarmCrop(
                                        index,
                                        it
                                    )
                                )
                            },
                            label = { Text("Crop") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = row.area,
                            onValueChange = {
                                viewModel.dispatch(
                                    CreateSurveyIntent.SetFarmArea(
                                        index,
                                        it
                                    )
                                )
                            },
                            label = { Text("Area") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = row.yield,
                            onValueChange = {
                                viewModel.dispatch(
                                    CreateSurveyIntent.SetFarmYield(
                                        index,
                                        it
                                    )
                                )
                            },
                            label = { Text("Yield") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Text(
                "Attachments",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp)
            )
            Button(
                onClick = { viewModel.dispatch(CreateSurveyIntent.AddPhoto) },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("Add photo")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.attachmentPaths.forEachIndexed { i, path ->
                    Column(
                        modifier = Modifier.padding(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(path.takeLast(12), style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { viewModel.dispatch(CreateSurveyIntent.RemovePhoto(i)) }
                        ) { Text("×") }
                    }
                }
            }

            Button(
                onClick = { viewModel.dispatch(CreateSurveyIntent.Submit) },
                modifier = Modifier.padding(top = 24.dp),
                enabled = !state.isSubmitting
            ) {
                Text(if (state.isSubmitting) "Submitting..." else "Submit")
            }
        }
    }
}
