package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Post
import com.example.data.model.PostType
import com.example.data.model.UserRole
import com.example.ui.components.RoleBadge

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var fileName: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = cursor.getString(displayNameIndex)
                }
            }
        }
    }
    if (fileName == null) {
        fileName = uri.path?.let { path ->
            val cut = path.lastIndexOf('/')
            if (cut != -1) path.substring(cut + 1) else path
        }
    }
    return fileName
}

@Composable
fun RoleGreetingBanner(
    userName: String,
    role: UserRole
) {
    val greetingTitle = when (role) {
        UserRole.STUDENT -> "Welcome back, $userName! 👋"
        UserRole.FACULTY -> "Welcome, Dr. $userName! 👨‍🏫"
        UserRole.ALUMNI -> "Welcome back, $userName! 🎓"
        UserRole.CLUB_LEADER -> "Welcome, $userName! 🏆"
        UserRole.DEPARTMENT_ADMIN -> "Department Console — $userName 🏢"
        UserRole.UNIVERSITY_ADMIN -> "Executive Console — $userName 🛡️"
    }

    val greetingSub = when (role) {
        UserRole.STUDENT -> "Track classes, study with AI, vote in polls, and explore campus events."
        UserRole.FACULTY -> "Manage lecture resources, publish course notices, and mentor students."
        UserRole.ALUMNI -> "Share career referrals, mentor university students, and expand your network."
        UserRole.CLUB_LEADER -> "Schedule campus events, launch polls, and engage university members."
        UserRole.DEPARTMENT_ADMIN -> "Department Roster • Course Analytics • Student Moderation"
        UserRole.UNIVERSITY_ADMIN -> "Global Access Control • Cloud Infrastructure Sync • System Telemetry"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greetingTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                RoleBadge(role = role)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = greetingSub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedScreen(
    posts: List<Post>,
    onCreatePost: (content: String, type: PostType, category: String, hashtags: List<String>, pollOptions: List<String>, mediaUrls: List<String>) -> Unit,
    onLikePost: (String) -> Unit,
    onBookmarkPost: (String) -> Unit,
    onVotePoll: (postId: String, optionIndex: Int) -> Unit,
    onAddComment: (postId: String, commentText: String) -> Unit,
    userName: String = "Campus Member",
    currentRole: UserRole = UserRole.STUDENT,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    var aiFeedEnabled by remember { mutableStateOf(true) }
    var showCreatePostModal by remember { mutableStateOf(false) }

    var postContent by remember { mutableStateOf("") }
    var postType by remember { mutableStateOf(PostType.TEXT) }
    var postCategory by remember { mutableStateOf("General") }
    var postHashtags by remember { mutableStateOf("Campus, AI") }

    // Poll options state for post creation
    var pollOptions by remember { mutableStateOf(listOf("Option 1", "Option 2")) }

    // File attachments state for post creation
    var attachedFiles by remember { mutableStateOf<List<String>>(emptyList()) }

    // Native System File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val fileName = getFileNameFromUri(context, selectedUri) ?: "Attached_File"
            if (!attachedFiles.contains(fileName)) {
                attachedFiles = attachedFiles + fileName
                Toast.makeText(context, "Attached from storage: $fileName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val categories = listOf("All", "Announcements", "Clubs", "Research", "Events", "Polls")

    val filteredPosts = remember(posts, selectedCategory, aiFeedEnabled) {
        posts.filter { post ->
            if (selectedCategory == "All") true
            else post.category.equals(selectedCategory, ignoreCase = true) ||
                 (selectedCategory == "Polls" && post.type == PostType.POLL)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Role Greeting Banner
            RoleGreetingBanner(userName = userName, role = currentRole)

            // AI Recommendation Banner & Category Filter Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Feed",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gemini AI Smart Feed",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = aiFeedEnabled,
                            onCheckedChange = { aiFeedEnabled = it },
                            modifier = Modifier.testTag("toggle_ai_feed_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 12.sp) },
                                leadingIcon = if (selectedCategory == cat) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                modifier = Modifier.testTag("feed_filter_$cat")
                            )
                        }
                    }
                }
            }

            // Posts List
            if (filteredPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DynamicFeed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No posts in this category yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Be the first student or faculty member to create an announcement!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyLazyPosts(
                    posts = filteredPosts,
                    onLike = onLikePost,
                    onBookmark = onBookmarkPost,
                    onVotePoll = onVotePoll,
                    onAddComment = onAddComment
                )
            }
        }

        // Floating Action Button to Create Post
        FloatingActionButton(
            onClick = { showCreatePostModal = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("create_post_fab")
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Post", fontWeight = FontWeight.Bold)
            }
        }

        // Create Post Modal Dialog
        if (showCreatePostModal) {
            AlertDialog(
                onDismissRequest = { showCreatePostModal = false },
                title = { Text("Create Campus Post") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Select Post Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = postType == PostType.TEXT,
                                onClick = { postType = PostType.TEXT },
                                label = { Text("Text") }
                            )
                            FilterChip(
                                selected = postType == PostType.POLL,
                                onClick = { postType = PostType.POLL },
                                label = { Text("Poll") }
                            )
                            FilterChip(
                                selected = postType == PostType.PHOTO,
                                onClick = { postType = PostType.PHOTO },
                                label = { Text("Photo/File") }
                            )
                            FilterChip(
                                selected = postType == PostType.ANNOUNCEMENT,
                                onClick = { postType = PostType.ANNOUNCEMENT },
                                label = { Text("Notice") }
                            )
                            FilterChip(
                                selected = postType == PostType.RESEARCH,
                                onClick = { postType = PostType.RESEARCH },
                                label = { Text("Research") }
                            )
                        }

                        OutlinedTextField(
                            value = postContent,
                            onValueChange = { postContent = it },
                            label = { Text("Post Content *") },
                            placeholder = { Text("Share an academic announcement, research update, or poll question...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("create_post_content_input")
                        )

                        // Interactive Poll Creation Fields
                        if (postType == PostType.POLL) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Poll Options", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                    pollOptions.forEachIndexed { index, optionText ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = optionText,
                                                onValueChange = { newTxt ->
                                                    pollOptions = pollOptions.toMutableList().apply { set(index, newTxt) }
                                                },
                                                label = { Text("Option ${index + 1}") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (pollOptions.size > 2) {
                                                IconButton(onClick = {
                                                    pollOptions = pollOptions.toMutableList().apply { removeAt(index) }
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Remove option", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                    if (pollOptions.size < 6) {
                                        TextButton(onClick = {
                                            pollOptions = pollOptions + "Option ${pollOptions.size + 1}"
                                        }) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Add Poll Option")
                                        }
                                    }
                                }
                            }
                        }

                        // File & Photo Attachment Options (Native Storage Picker)
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("File Attachments", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                    TextButton(onClick = {
                                        filePickerLauncher.launch("*/*")
                                    }) {
                                        Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Select File")
                                    }
                                }

                                if (attachedFiles.isNotEmpty()) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 6.dp)
                                    ) {
                                        attachedFiles.forEach { file ->
                                            InputChip(
                                                selected = true,
                                                onClick = { },
                                                label = { Text(file, fontSize = 12.sp) },
                                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                                trailingIcon = {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Remove",
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable {
                                                                attachedFiles = attachedFiles - file
                                                            }
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = postHashtags,
                            onValueChange = { postHashtags = it },
                            label = { Text("Hashtags (comma separated)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (postContent.isNotBlank()) {
                                val tags = postHashtags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                val finalPollOpts = if (postType == PostType.POLL) pollOptions.filter { it.isNotBlank() } else emptyList()

                                onCreatePost(postContent, postType, postCategory, tags, finalPollOpts, attachedFiles)

                                postContent = ""
                                attachedFiles = emptyList()
                                pollOptions = listOf("Option 1", "Option 2")
                                showCreatePostModal = false
                                Toast.makeText(context, "Post published successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please enter post content!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("submit_new_post_button")
                    ) {
                        Text("Publish Post")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePostModal = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun LazyLazyPosts(
    posts: List<Post>,
    onLike: (String) -> Unit,
    onBookmark: (String) -> Unit,
    onVotePoll: (postId: String, optionIndex: Int) -> Unit,
    onAddComment: (postId: String, commentText: String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(posts, key = { it.id }) { post ->
            PostCard(
                post = post,
                onLike = onLike,
                onBookmark = onBookmark,
                onVotePoll = onVotePoll,
                onAddComment = onAddComment
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostCard(
    post: Post,
    onLike: (String) -> Unit,
    onBookmark: (String) -> Unit,
    onVotePoll: (postId: String, optionIndex: Int) -> Unit,
    onAddComment: (postId: String, commentText: String) -> Unit
) {
    val context = LocalContext.current
    var showComments by remember { mutableStateOf(false) }
    var commentInput by remember { mutableStateOf("") }
    var localComments by remember { mutableStateOf(listOf("Great update!", "Thanks for sharing this campus news.")) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author & Role Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.take(1),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RoleBadge(role = post.authorRole)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${post.department}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Body Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )

            // Poll Options & Voting Interaction Rendering
            if (post.type == PostType.POLL && post.pollOptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val totalVotes = post.pollVotes.values.sum()

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Campus Poll", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("$totalVotes total votes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }

                        post.pollOptions.forEachIndexed { idx, opt ->
                            val votes = post.pollVotes[idx] ?: 0
                            val percentage = if (totalVotes > 0) (votes * 100) / totalVotes else 0
                            val isSelected = post.userPollVote == idx

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onVotePoll(post.id, idx)
                                        Toast.makeText(context, "Vote recorded for option ${idx + 1}!", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text(
                                                opt,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Text("$percentage% ($votes)", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { if (totalVotes > 0) votes.toFloat() / totalVotes else 0f },
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // File / Document / Media Attachments Rendering
            if (post.mediaUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Attached Files & Documents", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    post.mediaUrls.forEach { fileUrl ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                        contentDescription = "File Attachment",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(fileUrl, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Attached File • Local Storage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Opening file: $fileUrl", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = "View", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("View", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Hashtags
            if (post.hashtags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    post.hashtags.forEach { tag ->
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Footer Actions: Like, Comment, Bookmark, Share
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onLike(post.id) },
                        modifier = Modifier.testTag("like_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("${post.likesCount}", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(onClick = { showComments = !showComments }) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = if (showComments) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("${post.commentsCount + localComments.size - 2}", style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onBookmark(post.id) },
                        modifier = Modifier.testTag("bookmark_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (post.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "CampusConnect Post by ${post.authorName}")
                            putExtra(Intent.EXTRA_TEXT, "${post.authorName}: ${post.content}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Post via"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            }

            // Expanded Comments Section
            AnimatedVisibility(visible = showComments) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text("Comments", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))

                    localComments.forEach { comment ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(comment, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            placeholder = { Text("Write a comment...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (commentInput.isNotBlank()) {
                                    localComments = localComments + commentInput
                                    onAddComment(post.id, commentInput)
                                    commentInput = ""
                                    Toast.makeText(context, "Comment posted!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
