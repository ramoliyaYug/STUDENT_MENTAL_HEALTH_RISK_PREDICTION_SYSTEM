package yug.ramoliya.ojtapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import yug.ramoliya.ojtapp.data.model.ShapValue
import yug.ramoliya.ojtapp.data.model.StudentIndicatorResponse
import kotlin.math.roundToInt

// ─── risk color mapping ────────────────────────────────────────────────── //
fun riskColor(risk: String): Color = when {
    risk.contains("Low",       ignoreCase = true) -> Color(0xFF00C853)
    risk.contains("Moderate",  ignoreCase = true) -> Color(0xFFFFA726)
    risk.contains("High",      ignoreCase = true) -> Color(0xFFEF5350)
    risk.contains("Severe",    ignoreCase = true) -> Color(0xFFB71C1C)
    else -> Color(0xFF78909C)
}

fun labelColor(label: String?): Color = when {
    label == null -> Color.Gray
    label.contains("Minimal", ignoreCase = true)  -> Color(0xFF00C853)
    label.contains("Mild",    ignoreCase = true)   -> Color(0xFFCDDC39)
    label.contains("Moderate",ignoreCase = true)   -> Color(0xFFFFA726)
    label.contains("Severe",  ignoreCase = true)   -> Color(0xFFEF5350)
    else -> Color.Gray
}

// ─────────────────────── Entry composable ───────────────────────────────── //

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultDetailScreen(
    result: StudentIndicatorResponse,
    title: String = "Your Results",
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A2E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        },
        containerColor = Color(0xFF0F0F1A),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Overall risk banner ───────────────────────────────────── //
            RiskBanner(result)

            // ── Score cards ──────────────────────────────────────────── //
            Text(
                "Dimension Scores",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScoreCard(
                    modifier = Modifier.weight(1f),
                    label = "Anxiety",
                    score = result.anxietyScore,
                    resultLabel = result.anxietyLabel,
                    icon = Icons.Filled.Psychology,
                    gradient = Brush.verticalGradient(
                        listOf(Color(0xFFF7971E), Color(0xFFFFD200))
                    ),
                )
                ScoreCard(
                    modifier = Modifier.weight(1f),
                    label = "Stress",
                    score = result.stressScore,
                    resultLabel = result.stressLabel,
                    icon = Icons.Filled.EmojiEmotions,
                    gradient = Brush.verticalGradient(
                        listOf(Color(0xFF11998E), Color(0xFF38EF7D))
                    ),
                )
                ScoreCard(
                    modifier = Modifier.weight(1f),
                    label = "Depression",
                    score = result.depressionScore,
                    resultLabel = result.depressionLabel,
                    icon = Icons.Filled.HealthAndSafety,
                    gradient = Brush.verticalGradient(
                        listOf(Color(0xFF6C63FF), Color(0xFF9B59B6))
                    ),
                )
            }

            // ── SHAP Explainability ──────────────────────────────────── //
            if (!result.explainability.isNullOrEmpty()) {
                ExplainabilitySection(result.explainability)
            }

            // ── Full JSON detail ─────────────────────────────────────── //
            FullJsonSection(result)

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────── Risk banner ────────────────────────────────────── //

@Composable
private fun RiskBanner(result: StudentIndicatorResponse) {
    val color = riskColor(result.riskLevel)
    val prob = result.probability?.let { (it * 100).roundToInt() } ?: 0

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(color.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
                .padding(20.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f))
                        .padding(4.dp),
                ) {
                    Icon(
                        Icons.Filled.Analytics,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(44.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = result.riskLevel,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Prediction confidence: $prob%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                )
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { (result.probability ?: 0.0).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color,
                    trackColor = Color(0xFF2A2A3E),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Model: ${result.modelUsed}",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                )
                Text(
                    "Assessment: ${result.createdAt.take(16).replace("T", " at ")}",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                )
            }
        }
    }
}

// ─────────────────────── Score card ─────────────────────────────────────── //

@Composable
private fun ScoreCard(
    modifier: Modifier = Modifier,
    label: String,
    score: Double?,
    resultLabel: String?,
    icon: ImageVector,
    gradient: Brush,
) {
    val lColor = labelColor(resultLabel)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradient),
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(
                text = score?.let { "%.1f".format(it) } ?: "—",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Text(
                text = resultLabel ?: "—",
                fontSize = 10.sp,
                color = lColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp,
            )
        }
    }
}

// ─────────────────────── SHAP explainability ────────────────────────────── //

@Composable
private fun ExplainabilitySection(items: List<ShapValue>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Top Influencing Factors",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                "SHAP values indicate how much each feature contributed to the prediction.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
            items.forEachIndexed { idx, shap ->
                ShapRow(rank = idx + 1, shap = shap, maxAbs = items.maxOf { it.absShapValue })
            }
        }
    }
}

private fun shapFeatureLabel(key: String): String = when (key) {
    "A1_Nervous"       -> "Feeling nervous/anxious"
    "A2_Worrying"      -> "Unable to stop worrying"
    "A3_Relaxing"      -> "Trouble relaxing"
    "A4_Irritated"     -> "Easily annoyed/irritated"
    "A5_TooMuchWorry"  -> "Worrying too much"
    "A6_Restless"      -> "Restlessness"
    "A7_Afraid"        -> "Feeling afraid"
    "S1_Upset"         -> "Feeling upset"
    "S2_Uncontrolled"  -> "Feeling out of control"
    "S3_NervousStressed"-> "Nervous/stressed"
    "S4_CannotCope"    -> "Cannot cope with academics"
    "S5_Confident"     -> "Confidence in handling issues"
    "S6_ThingsGoingWell"-> "Things going your way"
    "S7_ControlIrritations"-> "Controlling irritations"
    "S8_PerformanceOnTop"  -> "Academic performance"
    "S9_Angered"       -> "Angered by bad grades"
    "S10_PilingUp"     -> "Difficulties piling up"
    "D1_LittleInterest"-> "Little interest/pleasure"
    "D2_Hopeless"      -> "Feeling hopeless"
    "D3_SleepTrouble"  -> "Sleep problems"
    "D4_Tired"         -> "Tiredness/low energy"
    "D5_Appetite"      -> "Appetite changes"
    "D6_Failure"       -> "Feeling like a failure"
    "D7_Concentration" -> "Trouble concentrating"
    "D8_Psychomotor"   -> "Psychomotor changes"
    "D9_SuicidalThoughts"-> "Thoughts of self-harm"
    else -> key
}

@Composable
private fun ShapRow(rank: Int, shap: ShapValue, maxAbs: Double) {
    val barColor = if (shap.shapValue >= 0) Color(0xFF6C63FF) else Color(0xFFEF5350)
    val barWidth = if (maxAbs > 0) (shap.absShapValue / maxAbs).toFloat() else 0f

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(barColor.copy(alpha = 0.2f)),
                ) {
                    Text("$rank", fontSize = 10.sp, color = barColor, fontWeight = FontWeight.Bold)
                }
                Text(
                    shapFeatureLabel(shap.feature),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE0E0E0),
                )
            }
            Text(
                "%.4f".format(shap.absShapValue),
                fontSize = 10.sp,
                color = barColor,
                fontWeight = FontWeight.Medium,
            )
        }
        LinearProgressIndicator(
            progress = { barWidth },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = Color(0xFF2A2A3E),
        )
    }
}

// ─────────────────────── Full JSON section ──────────────────────────────── //

@Composable
private fun FullJsonSection(result: StudentIndicatorResponse) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Assessment Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            InfoRow("Assessment ID", "#${result.id}")
            InfoRow("Student ID",    "#${result.studentId}")
            InfoRow("Risk Level",    result.riskLevel, riskColor(result.riskLevel))
            InfoRow("Probability",   "${"%.1f".format((result.probability ?: 0.0) * 100)}%")
            InfoRow("Anxiety",       "${result.anxietyScore?.let { "%.2f".format(it) }} — ${result.anxietyLabel}")
            InfoRow("Stress",        "${result.stressScore?.let { "%.2f".format(it) }} — ${result.stressLabel}")
            InfoRow("Depression",    "${result.depressionScore?.let { "%.2f".format(it) }} — ${result.depressionLabel}")
            InfoRow("Model Used",    result.modelUsed)
            InfoRow("Date",          result.createdAt.take(16).replace("T", " at "))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = Color(0xFFB0BEC5)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.weight(0.45f))
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.55f),
            textAlign = TextAlign.End,
        )
    }
}
