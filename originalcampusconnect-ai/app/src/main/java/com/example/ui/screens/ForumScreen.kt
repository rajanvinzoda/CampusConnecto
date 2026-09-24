package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ForumTopic
import com.example.ui.components.RoleBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    topics: List<ForumTopic>,
    onVoteTopic: (topicId: String, delta: Int) -> Unit,
    onCreateTopic: (title: String, category: String, content: String, tags: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var showAskModal by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Programming") }
    var newContent by remember { mutableStateOf("") }
    var newTags by remember { mutableStateOf("Android, AI") }

    val forumCategories = listOf("All", "Programming", "AI", "Placements", "Internships", "Exams", "Hostel", "Sports", "Clubs", "Research")

    val filteredTopics = remember(topics, selectedCategory) {
        if (selectedCategory == "All") topics
        else topics.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Forum Categories Horizontal Picker Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(forumCategories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            leadingIcon = if (selectedCategory == cat) {
                                { Icon(Icons.Default.Tag, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            modifier = Modifier.testTag("forum_category_$cat")
                        )
                    }
                }
            }

            // Topics Feed
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTopics, key = { it.id }) { topic ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("forum_topic_card_${topic.id}")
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {

                            // Upvote / Downvote Pillar
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                IconButton(
                                    onClick = { onVoteTopic(topic.id, 1) },
                                    modifier = Modifier.testTag("upvote_button_${topic.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Upvote",
                                        tint = if (topic.userVote == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${topic.upvotes - topic.downvotes}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (topic.userVote != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = { onVoteTopic(topic.id, -1) },
                                    modifier = Modifier.testTag("downvote_button_${topic.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Downvote",
                                        tint = if (topic.userVote == -1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Forum Topic Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(topic.category, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    RoleBadge(role = topic.authorRole)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(topic.authorName, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = topic.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = topic.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3
                                )

                                // Best Answer Badge if Solved
                                if (topic.isSolved && topic.bestAnswer != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Best Answer",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Verified Solution / Best Answer", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(topic.bestAnswer, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        topic.tags.forEach { tag ->
                                            Text("#$tag", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${topic.repliesCount} replies", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to Ask Question
        FloatingActionButton(
            onClick = { showAskModal = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("ask_forum_fab")
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Ask Question")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ask Forum", fontWeight = FontWeight.Bold)
            }
        }

        // Ask Question Modal Dialog
        if (showAskModal) {
            AlertDialog(
                onDismissRequest = { showAskModal = false },
                title = { Text("Ask University Forum") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Question Title") },
                            placeholder = { Text("e.g. Best resources for CS-302 Operating Systems?") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forum_title_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newContent,
                            onValueChange = { newContent = it },
                            label = { Text("Detailed Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("forum_description_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank()) {
                                val tags = newTags.split(",").map { it.trim() }
                                onCreateTopic(newTitle, newCategory, newContent, tags)
                                newTitle = ""
                                newContent = ""
                                showAskModal = false
                            }
                        },
                        modifier = Modifier.testTag("submit_forum_topic_button")
                    ) {
                        Text("Post Question")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAskModal = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
