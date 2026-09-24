package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CampusDao {
    @Query("SELECT * FROM cached_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("UPDATE cached_posts SET isLiked = :isLiked, likesCount = likesCount + :delta WHERE id = :postId")
    suspend fun updatePostLike(postId: String, isLiked: Boolean, delta: Int)

    @Query("UPDATE cached_posts SET isBookmarked = :isBookmarked WHERE id = :postId")
    suspend fun updatePostBookmark(postId: String, isBookmarked: Boolean)

    @Query("SELECT * FROM cached_messages WHERE channelId = :channelId ORDER BY timestamp ASC")
    fun getMessagesForChannel(channelId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM cached_resources ORDER BY downloadsCount DESC")
    fun getAllResources(): Flow<List<ResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<ResourceEntity>)

    @Query("DELETE FROM cached_posts")
    suspend fun clearPosts()

    @Query("DELETE FROM cached_resources")
    suspend fun clearResources()

    @Query("UPDATE cached_resources SET isDownloaded = :isDownloaded, downloadsCount = downloadsCount + 1 WHERE id = :id")
    suspend fun markResourceDownloaded(id: String, isDownloaded: Boolean)
}
