package yug.ramoliya.ojtapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import yug.ramoliya.ojtapp.data.model.StudentIndicatorResponse
import yug.ramoliya.ojtapp.ui.StudentAppViewModel
import yug.ramoliya.ojtapp.ui.theme.BrandPurple
import yug.ramoliya.ojtapp.ui.theme.BrandTeal
import yug.ramoliya.ojtapp.ui.theme.LightBackground
import yug.ramoliya.ojtapp.ui.theme.LightSurface
import yug.ramoliya.ojtapp.ui.theme.LightTextDim
import yug.ramoliya.ojtapp.ui.theme.LightTextMain
import yug.ramoliya.ojtapp.ui.theme.LightTextMuted

@Composable
fun MainShellScreen(
    vm: StudentAppViewModel,
    onLogout: () -> Unit,
    onStartAssessment: () -> Unit,
    onHistoryItemSelected: () -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = LightBackground,
        bottomBar = {
            NavigationBar(containerColor = LightSurface) {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = BrandPurple,
                        selectedTextColor   = BrandPurple,
                        unselectedIconColor = LightTextMuted,
                        unselectedTextColor = LightTextMuted,
                        indicatorColor      = BrandPurple.copy(alpha = 0.12f),
                    ),
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Outlined.Assignment, contentDescription = null) },
                    label = { Text("Assess") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = BrandPurple,
                        selectedTextColor   = BrandPurple,
                        unselectedIconColor = LightTextMuted,
                        unselectedTextColor = LightTextMuted,
                        indicatorColor      = BrandPurple.copy(alpha = 0.12f),
                    ),
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = BrandPurple,
                        selectedTextColor   = BrandPurple,
                        unselectedIconColor = LightTextMuted,
                        unselectedTextColor = LightTextMuted,
                        indicatorColor      = BrandPurple.copy(alpha = 0.12f),
                    ),
                )
            }
        },
    ) { padding ->
        when (tab) {
            0 -> ProfileTab(vm, padding, onLogout)
            1 -> AssessTab(vm, padding, onStartAssessment)
            2 -> HistoryTab(vm, padding, onHistoryItemSelected)
        }
    }
}

// ──────────────────────────── Profile tab ─────────────────────────────── //

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
            .background(LightBackground)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "My Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = LightTextMain,
        )

        // Avatar + info card
        Card(
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (busy && profile == null) {
                    CircularProgressIndicator(color = BrandPurple, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                profile?.let { p ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(BrandPurple, Color(0xFF48CAE4)))),
                        ) {
                            Text(
                                p.name.first().uppercaseChar().toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                        Column {
                            Text(p.name, fontWeight = FontWeight.Bold, color = LightTextMain, fontSize = 18.sp)
                            Text(p.email, color = LightTextMuted, fontSize = 13.sp)
                            Text("Role: ${p.role} · ID: ${p.id}", color = LightTextDim, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { vm.refreshProfile() },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LightSurface),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = BrandPurple)
            Spacer(Modifier.width(8.dp))
            Text("Refresh Profile", color = LightTextMain)
        }

        Button(
            onClick = { vm.pingHealth() },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LightSurface),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Default.Analytics, contentDescription = null, tint = BrandTeal)
            Spacer(Modifier.width(8.dp))
            Text("Ping Server Health", color = LightTextMain)
        }

        health?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandTeal.copy(alpha = 0.10f)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("  $it", color = BrandTeal, modifier = Modifier.padding(12.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { vm.logout(onLogout) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Log Out", color = Color(0xFFEF5350), fontWeight = FontWeight.Medium)
        }
    }
}

// ──────────────────────────── Assess tab ─────────────────────────────────//

@Composable
private fun AssessTab(
    vm: StudentAppViewModel,
    padding: PaddingValues,
    onStartAssessment: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(LightBackground)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(20.dp))

        // Hero illustration area
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(BrandPurple.copy(alpha = 0.15f), Color.Transparent))),
        ) {
            Icon(
                Icons.Default.Analytics,
                contentDescription = null,
                tint = BrandPurple,
                modifier = Modifier.size(72.dp),
            )
        }

        Text(
            "Mental Health Assessment",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = LightTextMain,
        )
        Text(
            "Answer a short questionnaire covering anxiety, stress, and depression to receive a personalised risk prediction powered by AI.",
            style = MaterialTheme.typography.bodyMedium,
            color = LightTextMuted,
            lineHeight = 22.sp,
        )

        // Feature pills
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("🧠 Anxiety", "⚡ Stress", "💜 Depression").forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(LightSurface)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Text(tag, fontSize = 12.sp, color = LightTextMuted)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onStartAssessment,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Start Assessment", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }

        // Info footer card
        Card(
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("ℹ️ About this assessment", fontWeight = FontWeight.SemiBold, color = LightTextMain, fontSize = 13.sp)
                Text(
                    "This tool uses a validated ML model trained on student survey data. Responses are stored securely. This is not a clinical diagnosis — please seek professional help if you are in distress.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextMuted,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

// ──────────────────────────── History tab ────────────────────────────────//

@Composable
private fun HistoryTab(
    vm: StudentAppViewModel,
    padding: PaddingValues,
    onItemSelected: () -> Unit,
) {
    val items by vm.history.collectAsState()
    val busy by vm.busy.collectAsState()

    LaunchedEffect(Unit) { vm.refreshHistory("weekly") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(LightBackground),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Assessment History",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = LightTextMain,
            )
            TextButton(onClick = { vm.refreshHistory("weekly") }, enabled = !busy) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Refresh", color = BrandPurple, fontSize = 13.sp)
            }
        }

        when {
            busy && items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandPurple)
                }
            }
            items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.History, contentDescription = null, tint = LightTextMuted, modifier = Modifier.size(64.dp))
                        Text("No history yet", color = LightTextMuted)
                        Text("Complete an assessment to see results here", style = MaterialTheme.typography.bodySmall, color = LightTextDim)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(items) { row ->
                        HistoryCard(
                            item = row,
                            onClick = {
                                vm.selectHistoryItem(row)
                                onItemSelected()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    item: StudentIndicatorResponse,
    onClick: () -> Unit,
) {
    val color = riskColor(item.riskLevel)
    val prob = item.probability?.let { "${(it * 100).toInt()}%" } ?: "—"

    Card(
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Color badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
            ) {
                Icon(Icons.Default.Analytics, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
            }

            // Info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    item.riskLevel,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 15.sp,
                )
                Text(
                    item.createdAt.take(16).replace("T", " · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextMuted,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScorePill("A", item.anxietyScore, Color(0xFFF7971E))
                    ScorePill("S", item.stressScore, Color(0xFF11998E))
                    ScorePill("D", item.depressionScore, BrandPurple)
                    if (prob != "—") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(color.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(prob, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Chevron
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View Details",
                tint = LightTextMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun ScorePill(prefix: String, score: Double?, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            "$prefix: ${score?.let { "%.1f".format(it) } ?: "—"}",
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}
