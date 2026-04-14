package yug.ramoliya.ojtapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import yug.ramoliya.ojtapp.ui.StudentAppViewModel

@Composable
fun MainShellScreen(
    vm: StudentAppViewModel,
    onLogout: () -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                    label = { Text("Assess") },
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("History") },
                )
            }
        },
    ) { padding ->
        when (tab) {
            0 -> ProfileTab(vm, padding, onLogout)
            1 -> AssessTab(vm, padding)
            2 -> HistoryTab(vm, padding)
        }
    }
}

@Composable
private fun ProfileTab(
    vm: StudentAppViewModel,
    padding: PaddingValues,
    onLogout: () -> Unit,
) {
    val profile by vm.profile.collectAsState()
    val health by vm.healthText.collectAsState()
    val busy by vm.busy.collectAsState()

    LaunchedEffect(Unit) { vm.refreshProfile() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall)
        if (busy && profile == null) CircularProgressIndicator()
        profile?.let { p ->
            Text("Name: ${p.name}")
            Text("Email: ${p.email}")
            Text("Role: ${p.role}")
            Text("Id: ${p.id}")
        }
        Button(onClick = { vm.refreshProfile() }, enabled = !busy) { Text("Refresh profile") }
        Button(onClick = { vm.pingHealth() }, enabled = !busy) { Text("Ping /health") }
        health?.let { Text("Health: $it") }
        TextButton(onClick = { vm.logout(onLogout) }) { Text("Log out") }
    }
}

@Composable
private fun AssessTab(vm: StudentAppViewModel, padding: PaddingValues) {
    var json by remember { mutableStateOf("") }
    val busy by vm.busy.collectAsState()
    val last by vm.lastSubmit.collectAsState()

    LaunchedEffect(Unit) {
        if (json.isBlank()) json = vm.loadSampleJson()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Questionnaire (JSON)", style = MaterialTheme.typography.titleMedium)
        Text(
            "Paste the indicator map only (not wrapped in \"indicators\"). Keys match backend /ml/indicators.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = json,
            onValueChange = { json = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp),
            minLines = 10,
        )
        Button(
            onClick = { json = vm.loadSampleJson() },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Load sample") }
        Button(
            onClick = { vm.submitIndicatorsJson(json) },
            enabled = !busy && json.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Submit to /student/indicators")
            }
        }
        last?.let { r ->
            Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Last result", style = MaterialTheme.typography.titleSmall)
                    Text("Risk: ${r.riskLevel} (p=${r.probability})")
                    Text("Anxiety: ${r.anxietyScore} — ${r.anxietyLabel}")
                    Text("Stress: ${r.stressScore} — ${r.stressLabel}")
                    Text("Depression: ${r.depressionScore} — ${r.depressionLabel}")
                }
            }
        }
    }
}

@Composable
private fun HistoryTab(vm: StudentAppViewModel, padding: PaddingValues) {
    val items by vm.history.collectAsState()
    val busy by vm.busy.collectAsState()

    LaunchedEffect(Unit) { vm.refreshHistory("weekly") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
    ) {
        Text("History (/student/history)", style = MaterialTheme.typography.titleMedium)
        Button(
            onClick = { vm.refreshHistory("weekly") },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Refresh") }
        if (busy && items.isEmpty()) CircularProgressIndicator()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            items(items) { row ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${row.createdAt} — ${row.riskLevel}", style = MaterialTheme.typography.titleSmall)
                        Text("A ${row.anxietyScore} / S ${row.stressScore} / D ${row.depressionScore}")
                    }
                }
            }
        }
    }
}
