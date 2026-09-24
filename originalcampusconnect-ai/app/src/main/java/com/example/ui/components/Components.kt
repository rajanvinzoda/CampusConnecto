package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole

@Composable
fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(role.badgeColor).copy(alpha = 0.15f),
        contentColor = Color(role.badgeColor),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(role.badgeColor).copy(alpha = 0.4f)),
        modifier = modifier.testTag("role_badge_${role.name.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = when (role) {
                    UserRole.STUDENT -> Icons.Default.School
                    UserRole.FACULTY -> Icons.Default.Psychology
                    UserRole.ALUMNI -> Icons.Default.Verified
                    UserRole.CLUB_LEADER -> Icons.Default.Stars
                    UserRole.DEPARTMENT_ADMIN -> Icons.Default.AdminPanelSettings
                    UserRole.UNIVERSITY_ADMIN -> Icons.Default.Shield
                },
                contentDescription = role.displayName,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = role.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusTopBar(
    currentRole: UserRole,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    unreadNotificationsCount: Int,
    onAdminDashboardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                        contentDescription = "App Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CampusConnect",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    RoleBadge(role = currentRole)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentRole == UserRole.UNIVERSITY_ADMIN || currentRole == UserRole.DEPARTMENT_ADMIN) {
                    IconButton(
                        onClick = onAdminDashboardClick,
                        modifier = Modifier.testTag("admin_dashboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "University Admin Panel",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.testTag("global_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Global Search"
                    )
                }

                BadgedBox(
                    badge = {
                        if (unreadNotificationsCount > 0) {
                            Badge {
                                Text(unreadNotificationsCount.toString())
                            }
                        }
                    },
                    modifier = Modifier.clickable { onNotificationsClick() }
                ) {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                }
            }
        }
    }
}

enum class NavigationTab(val label: String, val icon: ImageVector, val tag: String) {
    FEED("Feed", Icons.Default.DynamicFeed, "tab_feed"),
    CHAT("Chat", Icons.AutoMirrored.Filled.Chat, "tab_chat"),
    FORUM("Forums", Icons.Default.Forum, "tab_forum"),
    EVENTS("Events", Icons.Default.Event, "tab_events"),
    CAREER("Career", Icons.Default.Work, "tab_career"),
    AI_STUDY("AI Study", Icons.Default.Psychology, "tab_ai"),
    RESOURCES("Resources", Icons.Default.Folder, "tab_resources"),
    PROFILE("Profile", Icons.Default.Person, "tab_profile")
}

@Composable
fun CampusBottomNav(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 12.dp,
            divider = {},
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationTab.values().forEach { tab ->
                val selected = tab == selectedTab
                Tab(
                    selected = selected,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = tab.label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag(tab.tag)
                )
            }
        }
    }
}
