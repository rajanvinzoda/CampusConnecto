package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorName: String,
    val authorRole: String,
    val department: String,
    val timestamp: Long,
    val type: String,
    val content: String,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val category: String
)

@Entity(tableName = "cached_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isVoiceNote: Boolean,
    val voiceDurationSec: Int,
    val attachmentUrl: String?
)

@Entity(tableName = "cached_resources")
data class ResourceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val department: String,
    val fileType: String,
    val authorName: String,
    val rating: Float,
    val downloadsCount: Int,
    val isDownloaded: Boolean
)
