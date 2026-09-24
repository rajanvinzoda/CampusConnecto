package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.RoleBadge

data class DemoCredential(
    val role: UserRole,
    val email: String,
    val pass: String,
    val description: String
)

val defaultDemoCredentials = listOf(
    DemoCredential(UserRole.STUDENT, "student@university.edu", "student123", "Student Access: Feed, Polls, Calendar, Career & AI Study"),
    DemoCredential(UserRole.FACULTY, "faculty@university.edu", "faculty123", "Faculty Access: Upload Course Materials, Faculty Notices, Quizzes"),
    DemoCredential(UserRole.ALUMNI, "alumni@university.edu", "alumni123", "Alumni Access: Post Career Referrals, 1:1 Mentorship Network"),
    DemoCredential(UserRole.CLUB_LEADER, "clubleader@university.edu", "club123", "Club Leader Access: Schedule Campus Events, Club Polls"),
    DemoCredential(UserRole.DEPARTMENT_ADMIN, "deptadmin@university.edu", "deptadmin123", "Dept Admin Access: Department User Roster, Dept Analytics"),
    DemoCredential(UserRole.UNIVERSITY_ADMIN, "admin@university.edu", "admin123", "Univ Admin Access: Full Admin Panel, User Roles & Cloud Sync")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    onLoginWithCredentials: (email: String, role: UserRole) -> Unit,
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
    var selectedAuthTab by remember { mutableStateOf(0) } // 0: Sign In, 1: Create Account

    var emailInput by remember { mutableStateOf("student@university.edu") }
    var passwordInput by remember { mutableStateOf("student123") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // App Hero Header
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CampusConnect",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " AI",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Unified University Ecosystem & Role-Based Rights",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Auth Tabs: Sign In vs Create Account
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedAuthTab,
                containerColor = Color.Transparent
            ) {
                Tab(
                    selected = selectedAuthTab == 0,
                    onClick = { selectedAuthTab = 0 },
                    text = { Text("Sign In", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null) }
                )
                Tab(
                    selected = selectedAuthTab == 1,
                    onClick = { selectedAuthTab = 1 },
                    text = { Text("Create Account", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) }
                )
            }
        }

        when (selectedAuthTab) {
            0 -> {
                // Sign In Form
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Sign In to Your Account",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enter your credentials or select a default demo role below to sign in instantly",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("University Email (.edu)") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (emailInput.isNotBlank()) {
                                    onLoginWithCredentials(emailInput.trim(), selectedRole)
                                    Toast.makeText(context, "Welcome back! Signed in as ${selectedRole.displayName}.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter your university email!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_submit_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign In as ${selectedRole.displayName}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Default Role Demo Credentials Quick-Select Section
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Default Role Credentials (Instant Sign In)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Click any role below to pre-fill credentials & sign in as that role:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            defaultDemoCredentials.forEach { demo ->
                                val isSelected = demo.role == selectedRole && emailInput == demo.email

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            emailInput = demo.email
                                            passwordInput = demo.pass
                                            selectedRole = demo.role
                                            onLoginWithCredentials(demo.email, demo.role)
                                            Toast.makeText(context, "Signed in as ${demo.role.displayName}!", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RoleBadge(role = demo.role)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(demo.email, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Password: ${demo.pass}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                            Text(demo.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            contentDescription = "Select",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Dedicated Create Account Section
                CreateAccountSection(onCompleteProfile = onCompleteProfile)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateAccountSection(
    onCompleteProfile: (
        name: String,
        email: String,
        role: UserRole,
        department: String,
        branch: String,
        bio: String,
        skills: List<String>,
        interests: List<String>
    ) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var department by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var skillsInput by remember { mutableStateOf("") }
    var interestsInput by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var departmentError by remember { mutableStateOf<String?>(null) }
    var branchError by remember { mutableStateOf<String?>(null) }
    var bioError by remember { mutableStateOf<String?>(null) }
    var skillsError by remember { mutableStateOf<String?>(null) }
    var interestsError by remember { mutableStateOf<String?>(null) }

    var roleMenuExpanded by remember { mutableStateOf(false) }

    val allowedSignUpRoles = listOf(UserRole.STUDENT, UserRole.FACULTY, UserRole.ALUMNI, UserRole.CLUB_LEADER)

    fun validate(): Boolean {
        var isValid = true

        if (name.trim().isBlank()) {
            nameError = "Full Name is required"
            isValid = false
        } else nameError = null

        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            emailError = "University Email is required"
            isValid = false
        } else if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            emailError = "Valid email required (e.g. user@university.edu)"
            isValid = false
        } else emailError = null

        if (password.isBlank() || password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else passwordError = null

        if (department.trim().isBlank()) {
            departmentError = "Department / Major is required"
            isValid = false
        } else departmentError = null

        if (branch.trim().isBlank()) {
            branchError = "Year / Branch is required"
            isValid = false
        } else branchError = null

        if (bio.trim().isBlank() || bio.trim().length < 5) {
            bioError = "Bio required (minimum 5 characters)"
            isValid = false
        } else bioError = null

        val sList = skillsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (sList.isEmpty()) {
            skillsError = "At least one skill is required"
            isValid = false
        } else skillsError = null

        val iList = interestsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (iList.isEmpty()) {
            interestsError = "At least one interest is required"
            isValid = false
        } else interestsError = null

        return isValid
    }

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
                text = "Create New University Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Fill in your complete profile details below to register.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Full Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("Full Name *") },
                placeholder = { Text("e.g. Alex Rivera") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = nameError != null,
                supportingText = nameError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input")
            )

            // University Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                label = { Text("University Email (.edu) *") },
                placeholder = { Text("e.g. alex@university.edu") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = emailError != null,
                supportingText = emailError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("onboarding_email_input")
            )

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                label = { Text("Password (min 6 chars) *") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = passwordError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Campus Role Dropdown Selector (Restricted to non-admin roles!)
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
                    allowedSignUpRoles.forEach { role ->
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
                onValueChange = { department = it; departmentError = null },
                label = { Text("Department / Major *") },
                placeholder = { Text("e.g. Computer Science & AI") },
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                isError = departmentError != null,
                supportingText = departmentError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("onboarding_dept_input")
            )

            // Branch / Academic Year
            OutlinedTextField(
                value = branch,
                onValueChange = { branch = it; branchError = null },
                label = { Text("Year / Branch / Specialization *") },
                placeholder = { Text("e.g. 2nd Year Software Dev") },
                leadingIcon = { Icon(Icons.Default.Class, contentDescription = null) },
                isError = branchError != null,
                supportingText = branchError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("onboarding_branch_input")
            )

            // Bio
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it; bioError = null },
                label = { Text("Short Bio / About Me *") },
                placeholder = { Text("Write a short intro about yourself...") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                isError = bioError != null,
                supportingText = bioError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth().testTag("onboarding_bio_input")
            )

            // Skills
            OutlinedTextField(
                value = skillsInput,
                onValueChange = { skillsInput = it; skillsError = null },
                label = { Text("Skills (comma separated) *") },
                placeholder = { Text("e.g. Kotlin, Python, UI Design") },
                leadingIcon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                isError = skillsError != null,
                supportingText = skillsError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("onboarding_skills_input")
            )

            // Interests
            OutlinedTextField(
                value = interestsInput,
                onValueChange = { interestsInput = it; interestsError = null },
                label = { Text("Interests / Hobbies (comma separated) *") },
                placeholder = { Text("e.g. AI Research, Hackathons, Robotics") },
                leadingIcon = { Icon(Icons.Default.Interests, contentDescription = null) },
                isError = interestsError != null,
                supportingText = interestsError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("onboarding_interests_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (!validate()) {
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
                    Toast.makeText(context, "Account created successfully! Welcome to CampusConnect AI.", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("onboarding_submit_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Account & Enter Campus", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
