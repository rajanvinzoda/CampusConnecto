package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.UserRole
import com.example.data.repository.BackendSyncRepository
import com.example.data.repository.CampusRepository
import com.example.data.repository.GeminiRepository
import com.example.ui.components.CampusBottomNav
import com.example.ui.components.CampusTopBar
import com.example.ui.components.NavigationTab
import com.example.ui.screens.*
import com.example.ui.theme.CampusConnectTheme

class MainActivity : ComponentActivity() {

    private lateinit var campusRepository: CampusRepository
    private lateinit var geminiRepository: GeminiRepository
    private lateinit var backendSyncRepository: BackendSyncRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        backendSyncRepository = BackendSyncRepository(applicationContext)
        campusRepository = CampusRepository(applicationContext, backendSyncRepository)
        geminiRepository = GeminiRepository()

        setContent {
            CampusConnectTheme {
                MainAppScreen(campusRepository, geminiRepository, backendSyncRepository)
            }
        }
    }
}

@Composable
fun MainAppScreen(
    campusRepository: CampusRepository,
    geminiRepository: GeminiRepository,
    backendSyncRepository: BackendSyncRepository
) {
    var activeTab by remember { mutableStateOf(NavigationTab.FEED) }
    var showSearchOverlay by remember { mutableStateOf(false) }
    var showNotificationsOverlay by remember { mutableStateOf(false) }
    var showAdminOverlay by remember { mutableStateOf(false) }

    // Collect repository state flows
    val currentUser by campusRepository.currentUser.collectAsState()
    val isProfileCreated by campusRepository.isProfileCreated.collectAsState()

    val posts by campusRepository.posts.collectAsState()
    val forumTopics by campusRepository.forumTopics.collectAsState()
    val channels by campusRepository.channels.collectAsState()
    val activeMessages by campusRepository.activeMessages.collectAsState()
    val events by campusRepository.events.collectAsState()
    val resources by campusRepository.resources.collectAsState()
    val careerItems by campusRepository.careerItems.collectAsState()
    val notifications by campusRepository.notifications.collectAsState()
    val adminUserList by campusRepository.adminUserList.collectAsState()

    Scaffold(
        topBar = {
            if (isProfileCreated && !showSearchOverlay && !showNotificationsOverlay && !showAdminOverlay) {
                CampusTopBar(
                    currentRole = currentUser.role,
                    onSearchClick = { showSearchOverlay = true },
                    onNotificationsClick = { showNotificationsOverlay = true },
                    unreadNotificationsCount = notifications.size,
                    onAdminDashboardClick = { showAdminOverlay = true }
                )
            }
        },
        bottomBar = {
            if (isProfileCreated && !showSearchOverlay && !showNotificationsOverlay && !showAdminOverlay) {
                CampusBottomNav(
                    selectedTab = activeTab,
                    onTabSelected = { tab -> activeTab = tab }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isProfileCreated) innerPadding else PaddingValues(0.dp))
        ) {
            when {
                !isProfileCreated -> {
                    AuthScreen(
                        onLoginWithCredentials = { email, role ->
                            campusRepository.loginWithCredentials(email, role)
                        },
                        onCompleteProfile = { name, email, role, dept, branch, bio, skills, interests ->
                            campusRepository.completeProfileSetup(
                                name = name,
                                email = email,
                                role = role,
                                department = dept,
                                branch = branch,
                                bio = bio,
                                skills = skills,
                                interests = interests
                            )
                        }
                    )
                }
                showSearchOverlay -> {
                    GlobalSearchScreen(
                        posts = posts,
                        resources = resources,
                        events = events,
                        onClose = { showSearchOverlay = false }
                    )
                }
                showNotificationsOverlay -> {
                    NotificationsScreen(
                        notifications = notifications,
                        onClose = { showNotificationsOverlay = false }
                    )
                }
                showAdminOverlay && (currentUser.role == UserRole.UNIVERSITY_ADMIN || currentUser.role == UserRole.DEPARTMENT_ADMIN) -> {
                    AdminScreen(
                        users = adminUserList,
                        onChangeRole = { uId, role -> campusRepository.changeUserRoleByAdmin(uId, role) },
                        syncRepository = backendSyncRepository,
                        currentRole = currentUser.role,
                        onClose = { showAdminOverlay = false }
                    )
                }
                else -> {
                    when (activeTab) {
                        NavigationTab.FEED -> {
                            FeedScreen(
                                posts = posts,
                                onCreatePost = { content, type, category, hashtags, pollOptions, mediaUrls ->
                                    campusRepository.createPost(content, type, category, hashtags, pollOptions, mediaUrls)
                                },
                                onLikePost = { postId -> campusRepository.toggleLikePost(postId) },
                                onBookmarkPost = { postId -> campusRepository.toggleBookmarkPost(postId) },
                                onVotePoll = { postId, optionIndex -> campusRepository.votePoll(postId, optionIndex) },
                                onAddComment = { postId, commentText -> campusRepository.addComment(postId, commentText) },
                                userName = currentUser.name,
                                currentRole = currentUser.role
                            )
                        }
                        NavigationTab.CHAT -> {
                            ChatScreen(
                                channels = channels,
                                messages = activeMessages,
                                onSendMessage = { channelId, text, isVoice, duration ->
                                    campusRepository.sendMessage(channelId, text, isVoice, duration)
                                }
                            )
                        }
                        NavigationTab.FORUM -> {
                            ForumScreen(
                                topics = forumTopics,
                                onVoteTopic = { tId, delta -> campusRepository.voteForumTopic(tId, delta) },
                                onCreateTopic = { title, cat, content, tags ->
                                    campusRepository.createForumTopic(title, cat, content, tags)
                                }
                            )
                        }
                        NavigationTab.EVENTS -> {
                            EventsScreen(
                                events = events,
                                onRegisterEvent = { eventId -> campusRepository.toggleRegisterEvent(eventId) },
                                onCreateEvent = { title, category, date, location, description ->
                                    campusRepository.createEvent(title, category, date, location, description)
                                }
                            )
                        }
                        NavigationTab.CAREER -> {
                            CareerScreen(
                                careerItems = careerItems,
                                onApplyToCareer = { jobId -> campusRepository.applyToCareer(jobId) },
                                onAddCareerOpportunity = { company, roleTitle, type, loc, stipend, deadline, desc, skills ->
                                    campusRepository.addCareerOpportunity(company, roleTitle, type, loc, stipend, deadline, desc, skills)
                                },
                                onAnalyzeResumeWithAi = { resumeText ->
                                    geminiRepository.analyzeResume(resumeText)
                                },
                                currentRole = currentUser.role
                            )
                        }
                        NavigationTab.AI_STUDY -> {
                            AiAssistantScreen(
                                onGenerateAiText = { prompt -> geminiRepository.generateAcademicExplanation(prompt) },
                                onGenerateFlashcards = { subject -> geminiRepository.generateFlashcards(subject) },
                                onGenerateQuiz = { topic -> geminiRepository.generateQuiz(topic) }
                            )
                        }
                        NavigationTab.RESOURCES -> {
                            ResourceHubScreen(
                                resources = resources,
                                onDownloadResource = { resId -> campusRepository.toggleDownloadResource(resId) },
                                onUploadResource = { title, subject, fileType, fileName ->
                                    campusRepository.addResource(title, subject, fileType, fileName)
                                },
                                currentRole = currentUser.role
                            )
                        }
                        NavigationTab.PROFILE -> {
                            ProfileScreen(
                                user = currentUser,
                                onUpdateProfile = { name, department, branch, bio, skills, interests ->
                                    campusRepository.updateProfileInfo(name, department, branch, bio, skills, interests)
                                },
                                onLogout = {
                                    campusRepository.logout()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
