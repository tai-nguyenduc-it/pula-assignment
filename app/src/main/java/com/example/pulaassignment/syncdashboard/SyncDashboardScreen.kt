package com.example.pulaassignment.syncdashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.model.SyncStateDomainModel
import com.example.pulaassignment.ui.PulaTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: SyncDashboardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { PulaTopAppBar(title = "Sync Dashboard", onBackClick = onNavigateBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Total pending: ${state.pendingCount}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Total success: ${state.syncedCount}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Total failed: ${state.failedCount}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Current sync state: ${syncStateLabel(state.syncState)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (state.syncState is SyncStateDomainModel.Syncing) {
                        val s = state.syncState as SyncStateDomainModel.Syncing
                        Text(
                            "Uploading ${s.current + 1} of ${s.total}...",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            state.lastSyncResult?.let { result ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Last sync result", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Synced: ${result.syncedIds.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Failed: ${result.failed.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        result.stoppedReason?.let { reason ->
                            Text("Stopped: $reason", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.dispatch(SyncDashboardIntent.StartSync) },
                modifier = Modifier.padding(top = 16.dp),
                enabled = state.syncState is SyncStateDomainModel.Idle || state.syncState is SyncStateDomainModel.Result
            ) {
                Text("Start Sync")
            }
        }
    }
}

private fun syncStateLabel(syncState: SyncStateDomainModel): String = when (syncState) {
    is SyncStateDomainModel.Idle -> "Idle"
    is SyncStateDomainModel.Syncing -> "Running"
    is SyncStateDomainModel.Result -> "Stopped"
}
