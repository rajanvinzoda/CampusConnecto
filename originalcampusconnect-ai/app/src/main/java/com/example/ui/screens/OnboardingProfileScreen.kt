package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.RoleBadge

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingProfileScreen(
    onCompleteProfile: (
        name: String,
        email: String,
        role: UserRole,
        department: String,
        branch: String,
        bio: String,
        skills: List<String>,
        interests: List<String>
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var department by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var skillsInput by remember { mutableStateOf("") }
    var interestsInput by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var departmentError by remember { mutableStateOf<String?>(null) }
    var branchError by remember { mutableStateOf<String?>(null) }
    var bioError by remember { mutableStateOf<String?>(null) }
    var skillsError by remember { mutableStateOf<String?>(null) }
    var interestsError by remember { mutableStateOf<String?>(null) }

    var roleMenuExpanded by remember { mutableStateOf(false) }

    fun validateForm(): Boolean {
        var isValid = true

        if (name.trim().isBlank()) {
            nameError = "Full Name is required"
            isValid = false
        } else {
            nameError = null
        }

        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            emailError = "University Email is required"
            isValid = false
        } else if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            emailError = "Please enter a valid university email (e.g. student@university.edu)"
            isValid = false
        } else {
            emailError = null
        }

        if (department.trim().isBlank()) {
            departmentError = "Department / Major is required"
            isValid = false
        } else {
            departmentError = null
        }

        if (branch.trim().isBlank()) {
            branchError = "Year / Branch / Specialization is required"
            isValid = false
        } else {
            branchError = null
        }

        if (bio.trim().isBlank() || bio.trim().length < 5) {
            bioError = "Bio is required (minimum 5 characters)"
            isValid = false
        } else {
            bioError = null
        }

        val sList = skillsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (sList.isEmpty()) {
            skillsError = "Please enter at least one technical skill"
            isValid = false
        } else {
            skillsError = null
        }

        val iList = interestsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (iList.isEmpty()) {
            interestsError = "Please enter at least one interest or hobby"
            isValid = false
        } else {
            interestsError = null
        }

        return isValid
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header Avatar Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to CampusConnect!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Fill in your complete profile details below to enter campus.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Personal & Academic Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g. Alex Rivera") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError != null,
                    supportingText = nameError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input")
                )

                // University Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text("University Email (.edu) *") },
                    placeholder = { Text("e.g. alex@university.edu") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    isError = emailError != null,
                    supportingText = emailError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_email_input")
                )

                // Role Dropdown Selector
                Text("Select Campus Role *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                ExposedDropdownMenuBox(
                    expanded = roleMenuExpanded,
                    onExpandedChange = { roleMenuExpanded = !roleMenuExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRole.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded) },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("onboarding_role_selector")
                    )

                    ExposedDropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        listOf(UserRole.STUDENT, UserRole.FACULTY, UserRole.ALUMNI, UserRole.CLUB_LEADER).forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RoleBadge(role = role)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(role.displayName)
                                    }
                                },
                                onClick = {
                                    selectedRole = role
                                    roleMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Department
                OutlinedTextField(
                    value = department,
                    onValueChange = {
                        department = it
                        if (departmentError != null) departmentError = null
                    },
                    label = { Text("Department / Major *") },
                    placeholder = { Text("e.g. Computer Science & AI") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    isError = departmentError != null,
                    supportingText = departmentError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_dept_input")
                )

                // Branch / Academic Year
                OutlinedTextField(
                    value = branch,
                    onValueChange = {
                        branch = it
                        if (branchError != null) branchError = null
                    },
                    label = { Text("Year / Branch / Specialization *") },
                    placeholder = { Text("e.g. 2nd Year Software Dev") },
                    leadingIcon = { Icon(Icons.Default.Class, contentDescription = null) },
                    isError = branchError != null,
                    supportingText = branchError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_branch_input")
                )

                // Bio
                OutlinedTextField(
                    value = bio,
                    onValueChange = {
                        bio = it
                        if (bioError != null) bioError = null
                    },
                    label = { Text("Short Bio / About Me *") },
                    placeholder = { Text("Write a short intro about yourself...") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    isError = bioError != null,
                    supportingText = bioError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_bio_input")
                )

                // Skills (comma separated)
                OutlinedTextField(
                    value = skillsInput,
                    onValueChange = {
                        skillsInput = it
                        if (skillsError != null) skillsError = null
                    },
                    label = { Text("Skills (comma separated) *") },
                    placeholder = { Text("e.g. Kotlin, Python, UI Design") },
                    leadingIcon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                    isError = skillsError != null,
                    supportingText = skillsError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_skills_input")
                )

                // Interests (comma separated)
                OutlinedTextField(
                    value = interestsInput,
                    onValueChange = {
                        interestsInput = it
                        if (interestsError != null) interestsError = null
                    },
                    label = { Text("Interests / Hobbies (comma separated) *") },
                    placeholder = { Text("e.g. AI Research, Hackathons, Robotics") },
                    leadingIcon = { Icon(Icons.Default.Interests, contentDescription = null) },
                    isError = interestsError != null,
                    supportingText = interestsError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_interests_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (!validateForm()) {
                            Toast.makeText(context, "Please fill in all required profile details accurately!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val sList = skillsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val iList = interestsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                        onCompleteProfile(
                            name.trim(),
                            email.trim(),
                            selectedRole,
                            department.trim(),
                            branch.trim(),
                            bio.trim(),
                            sList,
                            iList
                        )
                        Toast.makeText(context, "Profile created successfully! Welcome aboard.", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("onboarding_submit_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Profile & Enter Campus", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
