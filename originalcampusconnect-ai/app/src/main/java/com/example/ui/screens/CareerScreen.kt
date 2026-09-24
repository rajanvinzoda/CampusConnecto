package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CareerOpportunity
import com.example.data.model.UserRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerScreen(
    careerItems: List<CareerOpportunity>,
    onApplyToCareer: (String) -> Unit,
    onAddCareerOpportunity: (companyName: String, roleTitle: String, type: String, location: String, stipend: String, deadline: String, description: String, skills: List<String>) -> Unit,
    onAnalyzeResumeWithAi: suspend (String) -> String,
    currentRole: UserRole = UserRole.STUDENT,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Placement Drive, 1: AI Resume Reviewer, 2: Alumni Mentorship
    var resumeInputText by remember { mutableStateOf("Alex Rivera • CS Senior\nSkills: Kotlin, Jetpack Compose, Python, Gemini API, System Architecture, PostgreSQL.\nProjects: Built CampusConnect AI social mobile ecosystem, High-performance Edge Computing research.\nExperience: Software Intern at Campus Tech Lab.") }
    var aiAnalysisResult by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Alumni/Admin Job Posting Modal State
    var showPostJobModal by remember { mutableStateOf(false) }
    var companyInput by remember { mutableStateOf("") }
    var roleInput by remember { mutableStateOf("") }
    var typeInput by remember { mutableStateOf("Full-Time / Internship") }
    var locationInput by remember { mutableStateOf("Remote / On-site") }
    var stipendInput by remember { mutableStateOf("$1,500 / month") }
    var deadlineInput by remember { mutableStateOf("May 30, 2026") }
    var descriptionInput by remember { mutableStateOf("") }
    var skillsInput by remember { mutableStateOf("Kotlin, AI, Data Structures") }

    val canPostJob = currentRole == UserRole.ALUMNI || currentRole == UserRole.FACULTY || currentRole == UserRole.DEPARTMENT_ADMIN || currentRole == UserRole.UNIVERSITY_ADMIN

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Placement Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Work, contentDescription = null) },
                    modifier = Modifier.testTag("career_tab_jobs")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("AI Resume Review", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    modifier = Modifier.testTag("career_tab_resume")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Alumni Mentors", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Groups, contentDescription = null) },
                    modifier = Modifier.testTag("career_tab_mentors")
                )
            }

            when (selectedTab) {
                0 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (canPostJob) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Alumni & Faculty Referral Portal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Post job openings & industry referrals for students", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(
                                        onClick = { showPostJobModal = true },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Post Job", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Job Opportunities List
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(careerItems, key = { it.id }) { job ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("job_card_${job.id}")
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column {
                                                Text(job.roleTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text("${job.companyName} • ${job.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                            SuggestionChip(
                                                onClick = { },
                                                label = { Text(job.type, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(job.stipend, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Deadline: ${job.deadline}", fontSize = 12.sp)
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(job.description, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            job.requiredSkills.forEach { skill ->
                                                SuggestionChip(onClick = { }, label = { Text(skill, fontSize = 10.sp) })
                                            }
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                        Button(
                                            onClick = { onApplyToCareer(job.id) },
                                            enabled = !job.isApplied,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("apply_job_button_${job.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (job.isApplied) Icons.Default.Check else Icons.AutoMirrored.Filled.Send,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(if (job.isApplied) "Application Submitted" else "1-Click Apply with University Profile")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // AI Resume Reviewer Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Gemini AI Resume Analyzer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Paste your resume summary or technical bio to get instant feedback & score for campus placements",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = resumeInputText,
                            onValueChange = { resumeInputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("resume_input_text")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                isAnalyzing = true
                                coroutineScope.launch {
                                    val result = onAnalyzeResumeWithAi(resumeInputText)
                                    aiAnalysisResult = result
                                    isAnalyzing = false
                                }
                            },
                            enabled = !isAnalyzing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("analyze_resume_button")
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing with Gemini AI...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Review Resume with Gemini AI")
                            }
                        }

                        aiAnalysisResult?.let { res ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Gemini Placement Score & Feedback", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                                    Text(res, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Alumni Mentors Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text("1-on-1 Alumni Mentorship Network", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        listOf(
                            Triple("Sarah Jenkins", "SDE-II at Google • Class of 2024", "System Design, LeetCode, Distributed Systems"),
                            Triple("David Kim", "Software Engineer at Apple • Class of 2025", "iOS, Swift, Edge AI, Interview Prep"),
                            Triple("Rohan Gupta", "AI Research Scientist at NVIDIA • Class of 2023", "PyTorch, CUDA, Computer Vision")
                        ).forEach { mentor ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(mentor.first, fontWeight = FontWeight.Bold)
                                        Text(mentor.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        Text(mentor.third, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Mentorship session requested with ${mentor.first}!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Book 1:1")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Post Job Referral Modal
        if (showPostJobModal) {
            AlertDialog(
                onDismissRequest = { showPostJobModal = false },
                title = { Text("Post Career Opening / Alumni Referral") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = companyInput,
                            onValueChange = { companyInput = it },
                            label = { Text("Company Name *") },
                            placeholder = { Text("e.g. Google, Microsoft, Startup") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = roleInput,
                            onValueChange = { roleInput = it },
                            label = { Text("Role Title *") },
                            placeholder = { Text("e.g. Software Engineer Intern") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = locationInput,
                            onValueChange = { locationInput = it },
                            label = { Text("Location *") },
                            placeholder = { Text("e.g. San Francisco / Remote") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = stipendInput,
                            onValueChange = { stipendInput = it },
                            label = { Text("Stipend / Package *") },
                            placeholder = { Text("e.g. $2,000 / month") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = descriptionInput,
                            onValueChange = { descriptionInput = it },
                            label = { Text("Job Description & Requirements *") },
                            placeholder = { Text("Enter referral details, requirements...") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = skillsInput,
                            onValueChange = { skillsInput = it },
                            label = { Text("Required Skills (comma separated)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (companyInput.isNotBlank() && roleInput.isNotBlank()) {
                                val sList = skillsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                onAddCareerOpportunity(
                                    companyInput.trim(),
                                    roleInput.trim(),
                                    typeInput,
                                    locationInput.trim(),
                                    stipendInput.trim(),
                                    deadlineInput,
                                    descriptionInput.trim(),
                                    sList
                                )
                                companyInput = ""
                                roleInput = ""
                                descriptionInput = ""
                                showPostJobModal = false
                                Toast.makeText(context, "Career referral posted successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please enter company and role title!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Post Opening")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPostJobModal = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
