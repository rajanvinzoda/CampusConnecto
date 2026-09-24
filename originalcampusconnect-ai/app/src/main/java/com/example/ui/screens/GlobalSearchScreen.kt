package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AcademicResource
import com.example.data.model.CampusEvent
import com.example.data.model.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    posts: List<Post>,
    resources: List<AcademicResource>,
    events: List<CampusEvent>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }

    val matchedPosts = remember(query, posts) {
        if (query.isBlank()) emptyList()
        else posts.filter { it.content.contains(query, ignoreCase = true) || it.authorName.contains(query, ignoreCase = true) }
    }

    val matchedResources = remember(query, resources) {
        if (query.isBlank()) emptyList()
        else resources.filter { it.title.contains(query, ignoreCase = true) || it.subject.contains(query, ignoreCase = true) }
    }

    val matchedEvents = remember(query, events) {
        if (query.isBlank()) emptyList()
        else events.filter { it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }
    }

    val totalMatches = matchedPosts.size + matchedResources.size + matchedEvents.size

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search Students, Faculty, Posts, Resources, Events...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("global_search_input")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (query.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Type to search across CampusConnect AI", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (totalMatches == 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No results found for '$query'", fontWeight = FontWeight.Bold)
                    Text("Try searching for different keywords or categories.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (matchedPosts.isNotEmpty()) {
                    item { Text("Posts (${matchedPosts.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
                    items(matchedPosts) { p ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(p.authorName, fontWeight = FontWeight.Bold)
                                Text(p.content, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                if (matchedResources.isNotEmpty()) {
                    item { Text("Resource Hub (${matchedResources.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
                    items(matchedResources) { r ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(r.title, fontWeight = FontWeight.Bold)
                                Text("${r.subject} • ${r.fileType}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                if (matchedEvents.isNotEmpty()) {
                    item { Text("Campus Events (${matchedEvents.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
                    items(matchedEvents) { e ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(e.title, fontWeight = FontWeight.Bold)
                                Text(e.date, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
