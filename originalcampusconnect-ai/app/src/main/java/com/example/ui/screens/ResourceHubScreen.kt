package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicResource
import com.example.data.model.UserRole

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceHubScreen(
    resources: List<AcademicResource>,
    onDownloadResource: (String) -> Unit,
    onUploadResource: (title: String, subject: String, fileType: String, fileName: String) -> Unit,
    currentRole: UserRole = UserRole.STUDENT,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") }

    var showUploadModal by remember { mutableStateOf(false) }
    var uploadTitle by remember { mutableStateOf("") }
    var uploadSubject by remember { mutableStateOf("") }
    var uploadFileType by remember { mutableStateOf("PDF") }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    val canUpload = currentRole == UserRole.FACULTY || currentRole == UserRole.DEPARTMENT_ADMIN || currentRole == UserRole.UNIVERSITY_ADMIN

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val name = getFileNameFromUri(context, selectedUri) ?: "Lecture_Notes.pdf"
            selectedFileName = name
            Toast.makeText(context, "File selected: $name", Toast.LENGTH_SHORT).show()
        }
    }

    val fileTypes = listOf("All", "PDF", "PPT", "NOTES", "CODE", "EXAM_PAPER")

    val filteredResources = remember(resources, searchQuery, selectedType) {
        resources.filter { res ->
            (selectedType == "All" || res.fileType.equals(selectedType, ignoreCase = true)) &&
            (searchQuery.isBlank() || res.title.contains(searchQuery, ignoreCase = true) || res.subject.contains(searchQuery, ignoreCase = true))
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Academic Resource Hub", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Lecture Notes, Syllabus, & Exam Papers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (canUpload) {
                            Button(
                                onClick = { showUploadModal = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = "Upload Material")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search academic notes, papers, code files...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resource_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(fileTypes) { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type, fontSize = 12.sp) },
                                modifier = Modifier.testTag("resource_filter_$type")
                            )
                        }
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredResources, key = { it.id }) { res ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resource_item_${res.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (res.fileType) {
                                            "PDF" -> Icons.Default.PictureAsPdf
                                            "CODE" -> Icons.Default.Code
                                            "PPT" -> Icons.Default.Slideshow
                                            else -> Icons.Default.Description
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(res.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text("${res.subject} • ${res.authorName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${res.rating} (${res.downloadsCount} downloads)", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { onDownloadResource(res.id) },
                                modifier = Modifier.testTag("download_resource_button_${res.id}")
                            ) {
                                Icon(
                                    imageVector = if (res.isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = if (res.isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Faculty / Admin Upload Modal
        if (showUploadModal) {
            AlertDialog(
                onDismissRequest = { showUploadModal = false },
                title = { Text("Upload Faculty Course Resource") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = uploadTitle,
                            onValueChange = { uploadTitle = it },
                            label = { Text("Resource Title *") },
                            placeholder = { Text("e.g. Advanced AI & ML Lecture Notes") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = uploadSubject,
                            onValueChange = { uploadSubject = it },
                            label = { Text("Subject / Course Code *") },
                            placeholder = { Text("e.g. CS402 - Artificial Intelligence") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            onClick = { filePickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(selectedFileName ?: "Select File from Device Storage")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (uploadTitle.isNotBlank()) {
                                onUploadResource(
                                    uploadTitle.trim(),
                                    uploadSubject.trim(),
                                    uploadFileType,
                                    selectedFileName ?: "Course_Material.pdf"
                                )
                                uploadTitle = ""
                                uploadSubject = ""
                                selectedFileName = null
                                showUploadModal = false
                                Toast.makeText(context, "Academic material published!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please enter resource title!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Publish Resource")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUploadModal = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
