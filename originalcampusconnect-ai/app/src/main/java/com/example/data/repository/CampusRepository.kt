package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.PostEntity
import com.example.data.local.ResourceEntity
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CampusRepository(
    context: Context,
    private val syncRepository: BackendSyncRepository? = null
) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.campusDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val prefs = context.getSharedPreferences("campus_connect_prefs", Context.MODE_PRIVATE)

    private fun loadUserProfileFromPrefs(): UserProfile {
        val savedRoleName = prefs.getString("user_role", UserRole.STUDENT.name) ?: UserRole.STUDENT.name
        val savedRole = try { UserRole.valueOf(savedRoleName) } catch (e: Exception) { UserRole.STUDENT }

        return UserProfile(
            id = prefs.getString("user_id", "usr_001") ?: "usr_001",
            name = prefs.getString("user_name", "New Campus Member") ?: "New Campus Member",
            email = prefs.getString("user_email", "student@university.edu") ?: "student@university.edu",
            role = savedRole,
            department = prefs.getString("user_department", "Unassigned Department") ?: "Unassigned Department",
            branch = prefs.getString("user_branch", "First Year") ?: "First Year",
            semester = 1,
            graduationYear = "2029",
            bio = prefs.getString("user_bio", "Welcome to my campus profile!") ?: "Welcome to my campus profile!",
            avatarUrl = null,
            coverUrl = null,
            skills = prefs.getStringSet("user_skills", emptySet())?.toList() ?: emptyList(),
            interests = prefs.getStringSet("user_interests", emptySet())?.toList() ?: emptyList(),
            githubUrl = "",
            linkedinUrl = "",
            websiteUrl = "",
            followersCount = 0,
            followingCount = 0,
            mutualConnections = 0,
            isVerified = true,
            certifications = emptyList(),
            achievements = emptyList(),
            clubs = emptyList()
        )
    }

    private fun saveUserProfileToPrefs(profile: UserProfile) {
        prefs.edit()
            .putBoolean("is_profile_created", true)
            .putString("user_id", profile.id)
            .putString("user_name", profile.name)
            .putString("user_email", profile.email)
            .putString("user_role", profile.role.name)
            .putString("user_department", profile.department)
            .putString("user_branch", profile.branch)
            .putString("user_bio", profile.bio)
            .putStringSet("user_skills", profile.skills.toSet())
            .putStringSet("user_interests", profile.interests.toSet())
            .apply()
    }

    // Current logged in user state
    private val _currentUser = MutableStateFlow(loadUserProfileFromPrefs())
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Profile creation state
    private val _isProfileCreated = MutableStateFlow(prefs.getBoolean("is_profile_created", false))
    val isProfileCreated: StateFlow<Boolean> = _isProfileCreated.asStateFlow()

    fun completeProfileSetup(
        name: String,
        email: String,
        role: UserRole,
        department: String,
        branch: String,
        bio: String,
        skills: List<String>,
        interests: List<String>
    ) {
        val newProfile = _currentUser.value.copy(
            name = name,
            email = email,
            role = role,
            department = department,
            branch = branch,
            bio = bio,
            skills = skills,
            interests = interests,
            isVerified = true
        )
        _currentUser.value = newProfile
        _isProfileCreated.value = true
        saveUserProfileToPrefs(newProfile)

        syncRepository?.logCloudSyncEvent("POST", "/v2/users/profile", "Profile for $name created & synced to cloud")

        // Connect real-time Socket.IO connection on user profile setup
        val authPayload = com.example.network.SocketAuthPayload(
            token = "jwt_live_${System.currentTimeMillis()}",
            userId = _currentUser.value.id,
            userName = name,
            role = role.name
        )
        syncRepository?.socketService?.connect(auth = authPayload)
        observeSocketEvents()
    }

    fun loginWithCredentials(email: String, role: UserRole, customName: String? = null) {
        val userName = if (!customName.isNullOrBlank()) customName else when (role) {
            UserRole.STUDENT -> "Alex Rivera"
            UserRole.FACULTY -> "Dr. Aris Thorne"
            UserRole.ALUMNI -> "Sarah Jenkins"
            UserRole.CLUB_LEADER -> "Jordan Lee"
            UserRole.DEPARTMENT_ADMIN -> "Prof. Marcus Vance"
            UserRole.UNIVERSITY_ADMIN -> "Chancellor Vance"
        }
        val dept = when (role) {
            UserRole.FACULTY, UserRole.DEPARTMENT_ADMIN -> "Computer Science & AI"
            UserRole.STUDENT, UserRole.CLUB_LEADER -> "Software Engineering"
            UserRole.ALUMNI -> "Class of 2024 - AI Research"
            UserRole.UNIVERSITY_ADMIN -> "Executive Board"
        }
        val branchVal = when (role) {
            UserRole.STUDENT, UserRole.CLUB_LEADER -> "4th Year"
            UserRole.FACULTY -> "Professor"
            UserRole.ALUMNI -> "Alumni Network"
            UserRole.DEPARTMENT_ADMIN -> "Dept Head"
            UserRole.UNIVERSITY_ADMIN -> "University Administration"
        }

        val updatedProfile = _currentUser.value.copy(
            email = email,
            role = role,
            name = userName,
            department = dept,
            branch = branchVal,
            bio = "Official ${role.displayName} profile on CampusConnect AI.",
            skills = listOf("System Architecture", "Leadership", "AI Ecosystem"),
            interests = listOf("Campus Research", "Networking", "AI Innovation"),
            isVerified = true
        )
        _currentUser.value = updatedProfile
        _isProfileCreated.value = true
        saveUserProfileToPrefs(updatedProfile)
        syncRepository?.logCloudSyncEvent("POST", "/v2/auth/login", "Signed in as ${role.displayName} ($email)")
    }

    private fun observeSocketEvents() {
        syncRepository?.socketService?.let { socket ->
            scope.launch {
                socket.events.collect { event ->
                    when (event) {
                        is com.example.network.SocketEvent.NewMessage -> {
                            _activeMessages.value = _activeMessages.value + event.message
                        }
                        is com.example.network.SocketEvent.NewNotification -> {
                            _notifications.value = listOf(event.notification) + _notifications.value
                        }
                        is com.example.network.SocketEvent.NewPost -> {
                            _posts.value = listOf(event.post) + _posts.value
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
        _isProfileCreated.value = false
        _currentUser.value = UserProfile(
            id = "usr_001",
            name = "New Campus Member",
            email = "student@university.edu",
            role = UserRole.STUDENT,
            department = "Unassigned Department",
            branch = "First Year",
            semester = 1,
            graduationYear = "2029",
            bio = "Tap 'Edit Profile' to add your bio, academic details, skills, and social links.",
            avatarUrl = null,
            coverUrl = null,
            skills = emptyList(),
            interests = emptyList(),
            githubUrl = "",
            linkedinUrl = "",
            websiteUrl = "",
            followersCount = 0,
            followingCount = 0,
            mutualConnections = 0,
            isVerified = false,
            certifications = emptyList(),
            achievements = emptyList(),
            clubs = emptyList()
        )
        syncRepository?.logCloudSyncEvent("POST", "/v2/auth/logout", "User logged out")
    }

    // Security & Auth settings
    private val _biometricsEnabled = MutableStateFlow(false)
    val biometricsEnabled: StateFlow<Boolean> = _biometricsEnabled.asStateFlow()

    private val _twoFactorEnabled = MutableStateFlow(false)
    val twoFactorEnabled: StateFlow<Boolean> = _twoFactorEnabled.asStateFlow()

    // Feed state
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Forums state
    private val _forumTopics = MutableStateFlow<List<ForumTopic>>(emptyList())
    val forumTopics: StateFlow<List<ForumTopic>> = _forumTopics.asStateFlow()

    // Chat channels state
    private val _channels = MutableStateFlow<List<ChatChannel>>(emptyList())
    val channels: StateFlow<List<ChatChannel>> = _channels.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val activeMessages: StateFlow<List<ChatMessage>> = _activeMessages.asStateFlow()

    // Events state
    private val _events = MutableStateFlow<List<CampusEvent>>(emptyList())
    val events: StateFlow<List<CampusEvent>> = _events.asStateFlow()

    // Resources state
    private val _resources = MutableStateFlow<List<AcademicResource>>(emptyList())
    val resources: StateFlow<List<AcademicResource>> = _resources.asStateFlow()

    // Career opportunities
    private val _careerItems = MutableStateFlow<List<CareerOpportunity>>(emptyList())
    val careerItems: StateFlow<List<CareerOpportunity>> = _careerItems.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Admin Users List for Role Management & Content Moderation
    private val _adminUserList = MutableStateFlow<List<UserProfile>>(emptyList())
    val adminUserList: StateFlow<List<UserProfile>> = _adminUserList.asStateFlow()

    init {
        seedInitialData()
        observeRoomDatabase()
    }

    private fun seedInitialData() {
        // Pure fresh state - no prebuilt posts, fake followers, or sample data
        _posts.value = emptyList()
        _forumTopics.value = emptyList()
        _channels.value = emptyList()
        _activeMessages.value = emptyList()
        _events.value = emptyList()
        _resources.value = emptyList()
        _careerItems.value = emptyList()
        _notifications.value = emptyList()
        _adminUserList.value = listOf(_currentUser.value)

        // Clear local database cache on start so user starts with a 100% clean slate
        scope.launch {
            dao.clearPosts()
            dao.clearResources()
        }
    }

    private fun observeRoomDatabase() {
        scope.launch {
            dao.getAllPosts().collect { roomPosts ->
                if (roomPosts.isNotEmpty()) {
                    // Sync Room changes with local post state
                    val updatedPosts = _posts.value.map { currentPost ->
                        val cached = roomPosts.find { it.id == currentPost.id }
                        if (cached != null) {
                            currentPost.copy(
                                likesCount = cached.likesCount,
                                isLiked = cached.isLiked,
                                isBookmarked = cached.isBookmarked
                            )
                        } else currentPost
                    }
                    _posts.value = updatedPosts
                }
            }
        }
    }

    // Role Switching
    fun updateUserRole(role: UserRole) {
        if (_isProfileCreated.value) {
            // Role cannot be changed after login
            return
        }
        _currentUser.value = _currentUser.value.copy(role = role)
        syncRepository?.logCloudSyncEvent("PATCH", "/v2/users/role", "Role updated to ${role.displayName}")
    }

    fun updateProfileInfo(name: String, department: String, branch: String, bio: String, skills: List<String>, interests: List<String>) {
        val updatedProfile = _currentUser.value.copy(
            name = name,
            department = department,
            branch = branch,
            bio = bio,
            skills = skills,
            interests = interests
        )
        _currentUser.value = updatedProfile
        if (_isProfileCreated.value) {
            saveUserProfileToPrefs(updatedProfile)
        }
        syncRepository?.logCloudSyncEvent("PUT", "/v2/users/profile", "Profile details updated for $name")
    }

    fun toggleBiometrics(enabled: Boolean) {
        _biometricsEnabled.value = enabled
    }

    fun toggleTwoFactor(enabled: Boolean) {
        _twoFactorEnabled.value = enabled
    }

    // Post Interactions
    fun createPost(
        content: String,
        type: PostType,
        category: String,
        hashtags: List<String>,
        pollOptions: List<String> = emptyList(),
        mediaUrls: List<String> = emptyList()
    ) {
        val user = _currentUser.value
        val initialVotes = if (pollOptions.isNotEmpty()) pollOptions.indices.associateWith { 0 } else emptyMap()
        val newPost = Post(
            id = "post_${System.currentTimeMillis()}",
            authorId = user.id,
            authorName = user.name,
            authorRole = user.role,
            department = user.department,
            timestamp = System.currentTimeMillis(),
            type = type,
            content = content,
            category = category,
            hashtags = hashtags,
            pollOptions = pollOptions,
            pollVotes = initialVotes,
            mediaUrls = mediaUrls
        )
        _posts.value = listOf(newPost) + _posts.value

        syncRepository?.logCloudSyncEvent("POST", "/v2/posts/create", "Live post by ${user.name} dispatched to cloud")

        val postJson = org.json.JSONObject().apply {
            put("id", newPost.id)
            put("authorId", newPost.authorId)
            put("authorName", newPost.authorName)
            put("content", newPost.content)
            put("type", newPost.type.name)
            put("timestamp", newPost.timestamp)
        }
        syncRepository?.socketService?.emit("post:create", postJson)

        scope.launch {
            dao.insertPosts(listOf(
                PostEntity(
                    id = newPost.id,
                    authorName = newPost.authorName,
                    authorRole = newPost.authorRole.name,
                    department = newPost.department,
                    timestamp = newPost.timestamp,
                    type = newPost.type.name,
                    content = newPost.content,
                    likesCount = 0,
                    commentsCount = 0,
                    isLiked = false,
                    isBookmarked = false,
                    category = newPost.category
                )
            ))
        }
    }

    fun toggleLikePost(postId: String) {
        val list = _posts.value.map { post ->
            if (post.id == postId) {
                val newLiked = !post.isLiked
                val newCount = if (newLiked) post.likesCount + 1 else post.likesCount - 1
                scope.launch {
                    dao.updatePostLike(postId, newLiked, if (newLiked) 1 else -1)
                }
                post.copy(isLiked = newLiked, likesCount = newCount)
            } else post
        }
        _posts.value = list
    }

    fun toggleBookmarkPost(postId: String) {
        val list = _posts.value.map { post ->
            if (post.id == postId) {
                val newBM = !post.isBookmarked
                scope.launch {
                    dao.updatePostBookmark(postId, newBM)
                }
                post.copy(isBookmarked = newBM)
            } else post
        }
        _posts.value = list
    }

    fun votePoll(postId: String, optionIndex: Int) {
        val list = _posts.value.map { post ->
            if (post.id == postId) {
                val currentVotes = post.pollVotes.toMutableMap()
                val prevVote = post.userPollVote
                if (prevVote == optionIndex) {
                    val cnt = currentVotes[optionIndex] ?: 1
                    currentVotes[optionIndex] = (cnt - 1).coerceAtLeast(0)
                    post.copy(pollVotes = currentVotes, userPollVote = null)
                } else {
                    if (prevVote != null) {
                        val prevCnt = currentVotes[prevVote] ?: 1
                        currentVotes[prevVote] = (prevCnt - 1).coerceAtLeast(0)
                    }
                    val cnt = currentVotes[optionIndex] ?: 0
                    currentVotes[optionIndex] = cnt + 1
                    post.copy(pollVotes = currentVotes, userPollVote = optionIndex)
                }
            } else post
        }
        _posts.value = list
        syncRepository?.logCloudSyncEvent("POST", "/v2/posts/vote", "Poll vote cast for post $postId")
    }

    fun addComment(postId: String, commentText: String) {
        val list = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(commentsCount = post.commentsCount + 1)
            } else post
        }
        _posts.value = list
        syncRepository?.logCloudSyncEvent("POST", "/v2/posts/comment", "Comment added to post $postId")
    }

    // Forum Actions
    fun voteForumTopic(topicId: String, delta: Int) {
        val updated = _forumTopics.value.map { topic ->
            if (topic.id == topicId) {
                val newVote = if (topic.userVote == delta) 0 else delta
                val voteDiff = newVote - topic.userVote
                topic.copy(
                    userVote = newVote,
                    upvotes = if (voteDiff > 0) topic.upvotes + voteDiff else topic.upvotes,
                    downvotes = if (voteDiff < 0) topic.downvotes - voteDiff else topic.downvotes
                )
            } else topic
        }
        _forumTopics.value = updated
    }

    fun createForumTopic(title: String, category: String, content: String, tags: List<String>) {
        val user = _currentUser.value
        val newTopic = ForumTopic(
            id = "forum_${System.currentTimeMillis()}",
            title = title,
            authorName = user.name,
            authorRole = user.role,
            category = category,
            content = content,
            timestamp = System.currentTimeMillis(),
            tags = tags
        )
        _forumTopics.value = listOf(newTopic) + _forumTopics.value
        syncRepository?.logCloudSyncEvent("POST", "/v2/forums/topics", "Forum discussion '$title' posted to cloud")
    }

    // Chat Actions
    fun sendMessage(channelId: String, text: String, isVoiceNote: Boolean = false, durationSec: Int = 0) {
        val user = _currentUser.value
        val msg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            channelId = channelId,
            senderId = user.id,
            senderName = user.name,
            text = text,
            timestamp = System.currentTimeMillis(),
            isVoiceNote = isVoiceNote,
            voiceDurationSec = durationSec
        )
        _activeMessages.value = _activeMessages.value + msg
        syncRepository?.logCloudSyncEvent("POST", "/v2/chat/messages", "Live chat message dispatched by ${user.name}")

        val msgJson = org.json.JSONObject().apply {
            put("id", msg.id)
            put("channelId", msg.channelId)
            put("senderId", msg.senderId)
            put("senderName", msg.senderName)
            put("text", msg.text)
            put("timestamp", msg.timestamp)
        }
        syncRepository?.socketService?.emit("chat:message", msgJson)

        scope.launch {
            dao.insertMessage(
                ChatMessageEntity(
                    id = msg.id,
                    channelId = msg.channelId,
                    senderId = msg.senderId,
                    senderName = msg.senderName,
                    text = msg.text,
                    timestamp = msg.timestamp,
                    isVoiceNote = msg.isVoiceNote,
                    voiceDurationSec = msg.voiceDurationSec,
                    attachmentUrl = msg.attachmentUrl
                )
            )
        }
    }

    // Events Actions
    fun createEvent(
        title: String,
        category: String,
        date: String,
        location: String,
        description: String
    ) {
        val user = _currentUser.value
        val newEvent = CampusEvent(
            id = "evt_${System.currentTimeMillis()}",
            title = title,
            organizer = user.name,
            category = category.ifBlank { "Campus Event" },
            date = date,
            location = location.ifBlank { "Main Campus Auditorium" },
            description = description.ifBlank { "University event open to all students and faculty." },
            attendeesCount = 1,
            isRegistered = true,
            qrCodeSeed = "qr_${System.currentTimeMillis()}"
        )
        _events.value = listOf(newEvent) + _events.value
        syncRepository?.logCloudSyncEvent("POST", "/v2/events/create", "New campus event '$title' scheduled for $date")
    }

    fun toggleRegisterEvent(eventId: String) {
        val updated = _events.value.map { evt ->
            if (evt.id == eventId) {
                val newReg = !evt.isRegistered
                val newCount = if (newReg) evt.attendeesCount + 1 else evt.attendeesCount - 1
                syncRepository?.logCloudSyncEvent("POST", "/v2/events/register", "Event registration state changed for '${evt.title}'")
                evt.copy(isRegistered = newReg, attendeesCount = newCount)
            } else evt
        }
        _events.value = updated
    }

    // Resource Actions
    fun addResource(
        title: String,
        subject: String,
        fileType: String,
        fileName: String
    ) {
        val user = _currentUser.value
        val newRes = AcademicResource(
            id = "res_${System.currentTimeMillis()}",
            title = title,
            subject = subject.ifBlank { user.department },
            department = user.department,
            fileType = fileType.ifBlank { "PDF" },
            authorName = user.name,
            rating = 5.0f,
            downloadsCount = 1,
            isDownloaded = true,
            tags = listOf(user.department, fileType)
        )
        _resources.value = listOf(newRes) + _resources.value
        syncRepository?.logCloudSyncEvent("POST", "/v2/resources/upload", "Resource '$title' ($fileName) published by ${user.name}")
    }

    fun toggleDownloadResource(resourceId: String) {
        val updated = _resources.value.map { res ->
            if (res.id == resourceId) {
                val newDown = !res.isDownloaded
                val newCount = if (newDown) res.downloadsCount + 1 else res.downloadsCount
                syncRepository?.logCloudSyncEvent("GET", "/v2/resources/download", "Academic resource '${res.title}' requested")
                scope.launch {
                    dao.markResourceDownloaded(resourceId, newDown)
                }
                res.copy(isDownloaded = newDown, downloadsCount = newCount)
            } else res
        }
        _resources.value = updated
    }

    // Career Actions
    fun addCareerOpportunity(
        companyName: String,
        roleTitle: String,
        type: String,
        location: String,
        stipend: String,
        deadline: String,
        description: String,
        skills: List<String>
    ) {
        val user = _currentUser.value
        val newJob = CareerOpportunity(
            id = "job_${System.currentTimeMillis()}",
            companyName = companyName,
            roleTitle = roleTitle,
            type = type.ifBlank { "Full-Time / Internship" },
            location = location.ifBlank { "Remote / Hybrid" },
            stipend = stipend.ifBlank { "$1,200/mo" },
            deadline = deadline.ifBlank { "Rolling Admissions" },
            requiredSkills = skills.ifEmpty { listOf("Problem Solving", "Technical Skills") },
            description = description.ifBlank { "Opportunity posted by ${user.name}." },
            isApplied = false
        )
        _careerItems.value = listOf(newJob) + _careerItems.value
        syncRepository?.logCloudSyncEvent("POST", "/v2/career/create", "Career opportunity '$roleTitle at $companyName' posted")
    }

    fun applyToCareer(jobId: String) {
        val updated = _careerItems.value.map { job ->
            if (job.id == jobId) {
                syncRepository?.logCloudSyncEvent("POST", "/v2/career/apply", "Job application submitted for '${job.roleTitle}'")
                job.copy(isApplied = true)
            } else job
        }
        _careerItems.value = updated
    }

    // Admin Actions
    fun changeUserRoleByAdmin(userId: String, newRole: UserRole) {
        val updated = _adminUserList.value.map { u ->
            if (u.id == userId) u.copy(role = newRole) else u
        }
        _adminUserList.value = updated
        if (userId == _currentUser.value.id) {
            _currentUser.value = _currentUser.value.copy(role = newRole)
        }
    }
}
