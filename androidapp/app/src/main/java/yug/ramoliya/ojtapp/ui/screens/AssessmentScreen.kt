package yug.ramoliya.ojtapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import yug.ramoliya.ojtapp.ui.StudentAppViewModel
import yug.ramoliya.ojtapp.ui.theme.BrandPurple
import yug.ramoliya.ojtapp.ui.theme.LightBackground
import yug.ramoliya.ojtapp.ui.theme.LightBorder
import yug.ramoliya.ojtapp.ui.theme.LightSurface
import yug.ramoliya.ojtapp.ui.theme.LightSurface2
import yug.ramoliya.ojtapp.ui.theme.LightSurface3
import yug.ramoliya.ojtapp.ui.theme.LightTextDim
import yug.ramoliya.ojtapp.ui.theme.LightTextMain
import yug.ramoliya.ojtapp.ui.theme.LightTextMuted

// ─────────────────────────── option maps ────────────────────────────────── //

private val AGE_OPTIONS = listOf("18-22", "23-26", "27+")
private val GENDER_OPTIONS = listOf("Male", "Female", "Other")
private val UNIVERSITY_OPTIONS = listOf(
    "Independent University, Bangladesh (IUB)",
    "BRAC University",
    "North South University",
    "University of Dhaka",
    "Bangladesh University of Engineering and Technology (BUET)",
    "Other Public University",
    "Other Private University",
)
private val DEPARTMENT_OPTIONS = listOf(
    "Engineering - CS / CSE / CSC / Similar to CS",
    "Engineering - EEE / ETE / Similar to EEE",
    "Engineering - Other",
    "Business Administration / BBA / MBA",
    "Life Sciences / Biology / Pharmacy",
    "Social Sciences / Arts / Humanities",
    "Law",
    "Other",
)
private val YEAR_OPTIONS = listOf(
    "First Year or Equivalent",
    "Second Year or Equivalent",
    "Third Year or Equivalent",
    "Fourth Year or Equivalent",
    "Fifth Year or above",
)
private val CGPA_OPTIONS = listOf(
    "Below 2.50",
    "2.50 - 2.99",
    "3.00 - 3.39",
    "3.40 - 3.79",
    "3.80 - 4.00",
    "Other",
)
private val SCHOLARSHIP_OPTIONS = listOf("Yes", "No")

private val ANXIETY_DEPRESSION_LABELS = listOf(
    "Not at all",
    "Several days",
    "More than half the days",
    "Nearly every day",
)

private val STRESS_LABELS = listOf(
    "Never",
    "Almost Never",
    "Sometimes",
    "Fairly Often",
    "Very Often",
)

// ─────────────────────────── question definitions ───────────────────────── //

data class QuizQuestion(val key: String, val text: String, val isInt: Boolean = true)

private val ANXIETY_QUESTIONS = listOf(
    QuizQuestion("A1_Nervous",     "How often did you feel nervous, anxious, or on edge due to academic pressure?"),
    QuizQuestion("A2_Worrying",    "How often were you unable to stop worrying about academic affairs?"),
    QuizQuestion("A3_Relaxing",    "How often did you have trouble relaxing due to academic pressure?"),
    QuizQuestion("A4_Irritated",   "How often were you easily annoyed or irritated because of academics?"),
    QuizQuestion("A5_TooMuchWorry","How often did you worry too much about academic affairs?"),
    QuizQuestion("A6_Restless",    "How often were you so restless due to academics that it was hard to sit still?"),
    QuizQuestion("A7_Afraid",      "How often did you feel afraid, as if something awful might happen?"),
)
private val STRESS_QUESTIONS = listOf(
    QuizQuestion("S1_Upset",              "How often did you feel upset due to something in your academic life?"),
    QuizQuestion("S2_Uncontrolled",       "How often did you feel unable to control important things in academics?"),
    QuizQuestion("S3_NervousStressed",    "How often did you feel nervous and stressed because of academic pressure?"),
    QuizQuestion("S4_CannotCope",         "How often did you feel you could not cope with mandatory academic activities?"),
    QuizQuestion("S5_Confident",          "How often did you feel confident about handling your academic problems?"),
    QuizQuestion("S6_ThingsGoingWell",    "How often did you feel things in your academic life were going your way?"),
    QuizQuestion("S7_ControlIrritations", "How often were you able to control irritations in academic affairs?"),
    QuizQuestion("S8_PerformanceOnTop",   "How often did you feel your academic performance was on top?"),
    QuizQuestion("S9_Angered",            "How often did you get angered due to bad performance or low grades?"),
    QuizQuestion("S10_PilingUp",          "How often did you feel academic difficulties were piling up uncontrollably?"),
)
private val DEPRESSION_QUESTIONS = listOf(
    QuizQuestion("D1_LittleInterest", "How often did you have little interest or pleasure in doing things?"),
    QuizQuestion("D2_Hopeless",       "How often did you feel down, depressed, or hopeless?"),
    QuizQuestion("D3_SleepTrouble",   "How often did you have trouble falling/staying asleep, or sleeping too much?"),
    QuizQuestion("D4_Tired",          "How often did you feel tired or have little energy?"),
    QuizQuestion("D5_Appetite",       "How often did you have poor appetite or overeat?"),
    QuizQuestion("D6_Failure",        "How often did you feel bad about yourself or that you are a failure?"),
    QuizQuestion("D7_Concentration",  "How often did you have trouble concentrating on things?"),
    QuizQuestion("D8_Psychomotor",    "How often did you move/speak slowly so people noticed, or feel very restless?"),
    QuizQuestion("D9_SuicidalThoughts","How often did you have thoughts of being better off dead or hurting yourself?"),
)

// ─────────────────────── public entry-point ─────────────────────────────── //

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentScreen(
    vm: StudentAppViewModel,
    onBack: () -> Unit,
    onResult: () -> Unit,
) {
    val busy by vm.busy.collectAsState()

    var step by remember { mutableIntStateOf(0) }

    var age by remember { mutableStateOf(AGE_OPTIONS[0]) }
    var gender by remember { mutableStateOf(GENDER_OPTIONS[0]) }
    var university by remember { mutableStateOf(UNIVERSITY_OPTIONS[0]) }
    var department by remember { mutableStateOf(DEPARTMENT_OPTIONS[0]) }
    var academicYear by remember { mutableStateOf(YEAR_OPTIONS[0]) }
    var cgpa by remember { mutableStateOf(CGPA_OPTIONS[0]) }
    var scholarship by remember { mutableStateOf(SCHOLARSHIP_OPTIONS[0]) }

    val answers = remember {
        mutableStateMapOf<String, Int>().apply {
            ANXIETY_QUESTIONS.forEach { put(it.key, 1) }
            STRESS_QUESTIONS.forEach { put(it.key, 2) }
            DEPRESSION_QUESTIONS.forEach { put(it.key, 1) }
        }
    }

    val totalSteps = 4
    val stepLabels = listOf("Demographics", "Anxiety", "Stress", "Depression")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mental Health Assessment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor        = LightSurface,
                    titleContentColor     = LightTextMain,
                    navigationIconContentColor = LightTextMain,
                ),
            )
        },
        containerColor = LightBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ── progress bar ──────────────────────────────────────────── //
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightSurface)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        stepLabels.forEachIndexed { i, label ->
                            val active = i == step
                            val done   = i < step
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        when {
                                            done   -> BrandPurple
                                            active -> BrandPurple.copy(alpha = 0.20f)
                                            else   -> LightBorder
                                        }
                                    )
                                    .padding(vertical = 6.dp),
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    color = when {
                                        done   -> Color.White
                                        active -> BrandPurple
                                        else   -> LightTextMuted
                                    },
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    LinearProgressIndicator(
                        progress = { (step + 1).toFloat() / totalSteps },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = BrandPurple,
                        trackColor = LightBorder,
                    )
                }
            }

            // ── animated step content ─────────────────────────────────── //
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState)
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    else
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                },
                modifier = Modifier.weight(1f),
                label = "step_transition",
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    when (currentStep) {
                        0 -> DemographicsStep(
                            age, gender, university, department, academicYear, cgpa, scholarship,
                            onAge = { age = it },
                            onGender = { gender = it },
                            onUniversity = { university = it },
                            onDepartment = { department = it },
                            onYear = { academicYear = it },
                            onCgpa = { cgpa = it },
                            onScholarship = { scholarship = it },
                        )
                        1 -> LikertStep(
                            title = "Anxiety Questions",
                            subtitle = "In this semester, how often…",
                            color = Color(0xFFF7971E),
                            questions = ANXIETY_QUESTIONS,
                            scaleLabels = ANXIETY_DEPRESSION_LABELS,
                            answers = answers,
                        )
                        2 -> LikertStep(
                            title = "Stress Questions",
                            subtitle = "In this semester, how often…",
                            color = Color(0xFF11998E),
                            questions = STRESS_QUESTIONS,
                            scaleLabels = STRESS_LABELS,
                            answers = answers,
                        )
                        3 -> LikertStep(
                            title = "Depression Questions",
                            subtitle = "In this semester, how often…",
                            color = BrandPurple,
                            questions = DEPRESSION_QUESTIONS,
                            scaleLabels = ANXIETY_DEPRESSION_LABELS,
                            answers = answers,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── nav buttons ───────────────────────────────────────────── //
            Surface(
                shadowElevation = 8.dp,
                color = LightSurface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Back button
                    Button(
                        onClick = { if (step > 0) step-- else onBack() },
                        colors = ButtonDefaults.buttonColors(containerColor = LightBorder),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = LightTextMain,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Back", color = LightTextMain)
                    }

                    // Step indicator dots
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(totalSteps) { i ->
                            Box(
                                modifier = Modifier
                                    .size(if (i == step) 10.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            i == step -> BrandPurple
                                            i < step  -> BrandPurple.copy(alpha = 0.4f)
                                            else      -> LightBorder
                                        }
                                    ),
                            )
                        }
                    }

                    // Next / Submit button
                    if (step < totalSteps - 1) {
                        Button(
                            onClick = { step++ },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("Next", color = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                val indicators = buildIndicators(
                                    age, gender, university, department,
                                    academicYear, cgpa, scholarship, answers
                                )
                                vm.predictAndSave(indicators, onResult)
                            },
                            enabled = !busy,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            if (busy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(if (busy) "Analyzing…" else "Get Results", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────── Demographics step ──────────────────────────── //

@Composable
private fun DemographicsStep(
    age: String, gender: String, university: String,
    department: String, academicYear: String, cgpa: String, scholarship: String,
    onAge: (String) -> Unit, onGender: (String) -> Unit,
    onUniversity: (String) -> Unit, onDepartment: (String) -> Unit,
    onYear: (String) -> Unit, onCgpa: (String) -> Unit,
    onScholarship: (String) -> Unit,
) {
    StepHeader("Your Background", "Tell us a bit about yourself", Color(0xFF0097B2))
    DropdownField("Age Group", age, AGE_OPTIONS, onAge)
    DropdownField("Gender", gender, GENDER_OPTIONS, onGender)
    DropdownField("University", university, UNIVERSITY_OPTIONS, onUniversity)
    DropdownField("Department", department, DEPARTMENT_OPTIONS, onDepartment)
    DropdownField("Academic Year", academicYear, YEAR_OPTIONS, onYear)
    DropdownField("Current CGPA", cgpa, CGPA_OPTIONS, onCgpa)
    DropdownField("Scholarship / Waiver", scholarship, SCHOLARSHIP_OPTIONS, onScholarship)
}

// ─────────────────────────── Likert step ────────────────────────────────── //

@Composable
private fun LikertStep(
    title: String,
    subtitle: String,
    color: Color,
    questions: List<QuizQuestion>,
    scaleLabels: List<String>,
    answers: MutableMap<String, Int>,
) {
    StepHeader(title, subtitle, color)
    questions.forEach { q ->
        LikertCard(
            question = q.text,
            labels = scaleLabels,
            selected = answers[q.key] ?: 0,
            accentColor = color,
            onSelect = { answers[q.key] = it },
        )
    }
}

// ─────────────────────────── Composable widgets ─────────────────────────── //

@Composable
private fun StepHeader(title: String, subtitle: String, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = LightTextMuted,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = LightTextMuted) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor        = LightTextMain,
                unfocusedTextColor      = LightTextMain,
                focusedBorderColor      = BrandPurple,
                unfocusedBorderColor    = LightBorder,
                focusedContainerColor   = LightSurface,
                unfocusedContainerColor = LightSurface,
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(LightSurface2),
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt, color = LightTextMain) },
                    onClick = { onSelect(opt); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun LikertCard(
    question: String,
    labels: List<String>,
    selected: Int,
    accentColor: Color,
    onSelect: (Int) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextMain,
                lineHeight = 20.sp,
            )
            // Scale buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                labels.forEachIndexed { idx, label ->
                    val isSelected = idx == selected
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) accentColor else LightBorder,
                                shape = RoundedCornerShape(10.dp),
                            )
                            .background(
                                if (isSelected) accentColor.copy(alpha = 0.12f)
                                else LightSurface3
                            )
                            .clickable { onSelect(idx) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "$idx",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isSelected) accentColor else LightTextMuted,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = label,
                            fontSize = 8.sp,
                            color = if (isSelected) accentColor else LightTextDim,
                            textAlign = TextAlign.Center,
                            lineHeight = 10.sp,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────── Build indicators ───────────────────────────── //

fun buildIndicators(
    age: String,
    gender: String,
    university: String,
    department: String,
    academicYear: String,
    cgpa: String,
    scholarship: String,
    answers: Map<String, Int>,
): JsonObject {
    val obj = JsonObject()
    obj.add("Age",           JsonPrimitive(age))
    obj.add("Gender",        JsonPrimitive(gender))
    obj.add("University",    JsonPrimitive(university))
    obj.add("Department",    JsonPrimitive(department))
    obj.add("Academic_Year", JsonPrimitive(academicYear))
    obj.add("CGPA",          JsonPrimitive(cgpa))
    obj.add("Scholarship",   JsonPrimitive(scholarship))
    answers.forEach { (k, v) -> obj.add(k, JsonPrimitive(v)) }
    return obj
}
