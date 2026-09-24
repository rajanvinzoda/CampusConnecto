package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatChannel
import com.example.data.model.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    channels: List<ChatChannel>,
    messages: List<ChatMessage>,
    onSendMessage: (channelId: String, text: String, isVoiceNote: Boolean, durationSec: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChannel by remember { mutableStateOf(channels.firstOrNull()) }
    var inputText by remember { mutableStateOf("") }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var showCallModal by remember { mutableStateOf<String?>(null) } // "voice" or "video"

    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingDuration = 0
            while (isRecordingVoice) {
                kotlinx.coroutines.delay(1000)
                recordingDuration++
            }
        }
    }

    Row(modifier = modifier.fillMaxSize()) {
        // Channels list column (left drawer style on wider screens or main panel)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = "Campus Communities & Messages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(channels) { channel ->
                    val isSelected = selectedChannel?.id == channel.id
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedChannel = channel }
                            .testTag("chat_channel_${channel.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (channel.isGroup) Icons.Default.Groups else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = channel.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = channel.lastMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            if (channel.unreadCount > 0) {
                                Badge {
                                    Text(channel.unreadCount.toString())
                                }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }

        // Selected Channel Active Conversation Window
        selectedChannel?.let { activeChan ->
            VerticalDivider()
            Column(
                modifier = Modifier
                    .weight(1.8f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Active Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (activeChan.isGroup) Icons.Default.Groups else Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(activeChan.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Online • Encrypted Campus Mesh", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row {
                            IconButton(onClick = { showCallModal = "Voice Call" }) {
                                Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { showCallModal = "Video Call" }) {
                                Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Messages List
                val chanMessages = messages.filter { it.channelId == activeChan.id }
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chanMessages) { msg ->
                        val isSelf = msg.senderName == "Alex Rivera"
                        Row(
                            horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelf) Color.White else MaterialTheme.colorScheme.onSurface,
                                shadowElevation = 1.dp,
                                modifier = Modifier.widthIn(max = 240.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (!isSelf) {
                                        Text(
                                            text = msg.senderName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }

                                    if (msg.isVoiceNote) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play Voice Note")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Voice Note (${msg.voiceDurationSec}s)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                    } else {
                                        Text(msg.text, fontSize = 13.sp)
                                    }

                                    if (msg.isPinned) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Icon(Icons.Default.PushPin, contentDescription = "Pinned", modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pinned", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Input Bar with Text, Voice Record & Attachments
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                        }

                        if (isRecordingVoice) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Recording Voice Note... ${recordingDuration}s", fontSize = 13.sp)
                                }
                            }
                            IconButton(onClick = {
                                isRecordingVoice = false
                                onSendMessage(activeChan.id, "Voice note recorded", true, recordingDuration)
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Send Voice Note", tint = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = { Text("Message ${activeChan.name}...") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_text")
                            )

                            IconButton(onClick = { isRecordingVoice = true }) {
                                Icon(Icons.Default.Mic, contentDescription = "Record Voice Note")
                            }

                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank()) {
                                        onSendMessage(activeChan.id, inputText, false, 0)
                                        inputText = ""
                                    }
                                },
                                modifier = Modifier.testTag("chat_send_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // Simulated Voice / Video Call Dialog
        showCallModal?.let { callType ->
            AlertDialog(
                onDismissRequest = { showCallModal = null },
                title = { Text("$callType Active") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (callType.contains("Video")) Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Connected to ${selectedChannel?.name}", fontWeight = FontWeight.Bold)
                        Text("HD Encrypted Stream • 60 FPS", style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = { showCallModal = null }
                    ) {
                        Text("End Call")
                    }
                }
            )
        }
    }
}
