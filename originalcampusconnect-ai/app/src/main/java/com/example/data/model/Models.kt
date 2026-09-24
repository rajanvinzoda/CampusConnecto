package com.example.data.model

enum class UserRole(val displayName: String, val badgeColor: Long) {
    STUDENT("Student", 0xFF3B82F6),
    FACULTY("Faculty", 0xFF8B5CF6),
    ALUMNI("Alumni", 0xFF10B981),
    CLUB_LEADER("Club Leader", 0xFFF59E0B),
    DEPARTMENT_ADMIN("Department Admin", 0xFFEC4899),
    UNIVERSITY_ADMIN("University Admin", 0xFFEF4444)
}

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val department: String,
    val branch: String,
    val semester: Int,
    val graduationYear: String,
    val bio: String,
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val githubUrl: String = "",
    val linkedinUrl: String = "",
    val websiteUrl: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val mutualConnections: Int = 0,
    val isVerified: Boolean = true,
    val certifications: List<String> = emptyList(),
    val achievements: List<String> = emptyList(),
    val clubs: List<String> = emptyList()
)

enum class PostType {
    TEXT, PHOTO, POLL, EVENT, ANNOUNCEMENT, RESEARCH, ACHIEVEMENT
}

data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorRole: UserRole,
    val authorAvatar: String? = null,
    val department: String,
    val timestamp: Long,
    val type: PostType,
    val content: String,
    val mediaUrls: List<String> = emptyList(),
    val pollOptions: List<String> = emptyList(),
    val pollVotes: Map<Int, Int> = emptyMap(),
    val userPollVote: Int? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val hashtags: List<String> = emptyList(),
    val category: String = "General"
)

data class Comment(
    val id: String,
    val postId: String,
    val authorName: String,
    val authorRole: UserRole,
    val content: String,
    val timestamp: Long,
    val likesCount: Int = 0
)

data class ChatChannel(
    val id: String,
    val name: String,
    val isGroup: Boolean,
    val avatarUrl: String? = null,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val category: String = "Direct Message"
)

data class ChatMessage(
    val id: String,
    val channelId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isVoiceNote: Boolean = false,
    val voiceDurationSec: Int = 0,
    val attachmentUrl: String? = null,
    val attachmentType: String? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val isPinned: Boolean = false,
    val readReceipt: Boolean = true
)

data class ForumTopic(
    val id: String,
    val title: String,
    val authorName: String,
    val authorRole: UserRole,
    val category: String,
    val content: String,
    val timestamp: Long,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val userVote: Int = 0, // 1 for up, -1 for down, 0 for none
    val repliesCount: Int = 0,
    val tags: List<String> = emptyList(),
    val isSolved: Boolean = false,
    val bestAnswer: String? = null
)

data class CampusEvent(
    val id: String,
    val title: String,
    val organizer: String,
    val category: String, // Workshop, Hackathon, Guest Lecture, Competition, Sports, Cultural
    val date: String,
    val location: String,
    val description: String,
    val attendeesCount: Int,
    val isRegistered: Boolean = false,
    val qrCodeSeed: String = "",
    val bannerGradientStart: Long = 0xFF4338CA,
    val bannerGradientEnd: Long = 0xFF6D28D9
)

data class AcademicResource(
    val id: String,
    val title: String,
    val subject: String,
    val department: String,
    val fileType: String, // PDF, PPT, NOTES, CODE, EXAM_PAPER
    val authorName: String,
    val rating: Float,
    val downloadsCount: Int,
    val isDownloaded: Boolean = false,
    val tags: List<String> = emptyList()
)

data class CareerOpportunity(
    val id: String,
    val companyName: String,
    val roleTitle: String,
    val type: String, // Internship, Full-Time, Research Assistant
    val location: String,
    val stipend: String,
    val deadline: String,
    val requiredSkills: List<String>,
    val description: String,
    val isApplied: Boolean = false
)

data class Flashcard(
    val question: String,
    val answer: String
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: String, // MESSAGE, REQUEST, LIKE, COMMENT, ANNOUNCEMENT, EVENT, PLACEMENT, AI
    val isRead: Boolean = false
)
