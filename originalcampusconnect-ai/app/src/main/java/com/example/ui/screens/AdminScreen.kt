package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.BackendSyncRepository
import com.example.ui.components.RoleBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    users: List<UserProfile>,
    onChangeRole: (userId: String, newRole: UserRole) -> Unit,
    syncRepository: BackendSyncRepository,
    currentRole: UserRole = UserRole.UNIVERSITY_ADMIN,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnivAdmin = currentRole == UserRole.UNIVERSITY_ADMIN
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = if (isUnivAdmin) {
        listOf("User Roles", "Backend & Cloud Sync", "Moderation", "Telemetry")
    } else {
        listOf("Department Users", "Dept Moderation", "Dept Analytics")
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isUnivAdmin) "University Admin Panel" else "Department Admin Panel",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isUnivAdmin) "Campus Access Control & Cloud Infrastructure" else "Department Roster & Academic Analytics",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Admin Panel"
                    )
                }
            }
        }

        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
            tabs.forEachIndexed { idx, title ->
                Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }, text = { Text(title) })
            }
        }

        if (isUnivAdmin) {
            when (selectedTab) {
                0 -> UserRolesContent(users = users, onChangeRole = onChangeRole)
                1 -> BackendSyncScreen(syncRepository = syncRepository, onClose = { selectedTab = 0 }, modifier = Modifier.fillMaxSize())
                2 -> ModerationContent()
                3 -> TelemetryContent()
            }
        } else {
            when (selectedTab) {
                0 -> UserRolesContent(users = users, onChangeRole = onChangeRole)
                1 -> ModerationContent()
                2 -> TelemetryContent()
            }
        }
    }
}

@Composable
private fun UserRolesContent(
    users: List<UserProfile>,
    onChangeRole: (userId: String, newRole: UserRole) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(users, key = { it.id }) { u ->
            var menuExpanded by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_user_card_${u.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(u.name, fontWeight = FontWeight.Bold)
                        Text(u.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        RoleBadge(role = u.role)
                    }

                    Box {
                        OutlinedButton(
                            onClick = { menuExpanded = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Change Role")
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            UserRole.values().forEach { r ->
                                DropdownMenuItem(
                                    text = { RoleBadge(role = r) },
                                    onClick = {
                                        onChangeRole(u.id, r)
                                        menuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModerationContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Moderation Queue Clean", fontWeight = FontWeight.Bold)
            Text("All posts and files compliant with AI Safety Guidelines", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TelemetryContent() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Campus Network Telemetry", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("⚡ Active Connected Users: 2,480", fontWeight = FontWeight.SemiBold)
                Text("🛡️ Room DB Local Cache Sync: 100%", fontWeight = FontWeight.SemiBold)
                Text("🤖 Gemini AI API Requests Today: 1,240", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
