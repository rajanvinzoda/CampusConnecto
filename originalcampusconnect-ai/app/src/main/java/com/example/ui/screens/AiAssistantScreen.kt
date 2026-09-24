package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.model.Flashcard
import com.example.data.model.QuizQuestion
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onGenerateAiText: suspend (prompt: String) -> String,
    onGenerateFlashcards: suspend (subject: String) -> List<Flashcard>,
    onGenerateQuiz: suspend (topic: String) -> List<QuizQuestion>,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(0) } // 0: Chat Assistant, 1: Flashcards, 2: Interactive Quiz, 3: Study Timetable
    var userPrompt by remember { mutableStateOf("") }
    var chatHistory by remember { mutableStateOf(listOf(Pair("AI Assistant", "Hello Alex! I am your CampusConnect Gemini AI Academic Assistant. How can I assist with your studies today?"))) }
    var isThinking by remember { mutableStateOf(false) }

    // Flashcard & Quiz States
    var flashcardsSubject by remember { mutableStateOf("Operating Systems") }
    var generatedCards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }

    var quizTopic by remember { mutableStateOf("Computer Networks") }
    var generatedQuiz by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var selectedAnswers by remember { mutableStateOf(mutableStateMapOf<Int, Int>()) }

    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedMode,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 12.dp
        ) {
            Tab(
                selected = selectedMode == 0,
                onClick = { selectedMode = 0 },
                text = { Text("Academic Tutor", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                modifier = Modifier.testTag("ai_mode_tutor")
            )
            Tab(
                selected = selectedMode == 1,
                onClick = { selectedMode = 1 },
                text = { Text("Flashcards", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Style, contentDescription = null) },
                modifier = Modifier.testTag("ai_mode_flashcards")
            )
            Tab(
                selected = selectedMode == 2,
                onClick = { selectedMode = 2 },
                text = { Text("Quiz Generator", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Quiz, contentDescription = null) },
                modifier = Modifier.testTag("ai_mode_quiz")
            )
            Tab(
                selected = selectedMode == 3,
                onClick = { selectedMode = 3 },
                text = { Text("Study Timetable", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                modifier = Modifier.testTag("ai_mode_timetable")
            )
        }

        when (selectedMode) {
            0 -> {
                // AI Academic Chat Window
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chatHistory) { msg ->
                            val isUser = msg.first == "User"
                            Row(
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(msg.first, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (isUser) Color.White else MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(msg.second, fontSize = 13.sp, color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }

                    if (isThinking) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            OutlinedTextField(
                                value = userPrompt,
                                onValueChange = { userPrompt = it },
                                placeholder = { Text("Ask Gemini to solve a coding bug, explain a subject...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_chat_prompt_input")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (userPrompt.isNotBlank()) {
                                        val promptCopy = userPrompt
                                        chatHistory = chatHistory + Pair("User", promptCopy)
                                        userPrompt = ""
                                        isThinking = true
                                        coroutineScope.launch {
                                            val aiAnswer = onGenerateAiText(promptCopy)
                                            chatHistory = chatHistory + Pair("Gemini AI", aiAnswer)
                                            isThinking = false
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("ai_send_prompt_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
            1 -> {
                // Flashcards Generator
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("AI Revision Flashcard Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = flashcardsSubject,
                            onValueChange = { flashcardsSubject = it },
                            label = { Text("Subject Name") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    generatedCards = onGenerateFlashcards(flashcardsSubject)
                                }
                            },
                            modifier = Modifier.testTag("generate_flashcards_button")
                        ) {
                            Text("Generate")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    generatedCards.forEach { card ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Q: ${card.question}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text("A: ${card.answer}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            2 -> {
                // Interactive Quiz Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Interactive AI Quiz Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = quizTopic,
                            onValueChange = { quizTopic = it },
                            label = { Text("Quiz Subject Topic") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    generatedQuiz = onGenerateQuiz(quizTopic)
                                    selectedAnswers.clear()
                                }
                            },
                            modifier = Modifier.testTag("generate_quiz_button")
                        ) {
                            Text("Create Quiz")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    generatedQuiz.forEachIndexed { qIdx, quiz ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Q${qIdx + 1}: ${quiz.question}", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                quiz.options.forEachIndexed { optIdx, optionText ->
                                    val isSelected = selectedAnswers[qIdx] == optIdx
                                    val isCorrect = optIdx == quiz.correctIndex
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) {
                                            if (isCorrect) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                                        } else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { selectedAnswers[qIdx] = optIdx }
                                    ) {
                                        Text(
                                            text = optionText,
                                            modifier = Modifier.padding(12.dp),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }

                                if (selectedAnswers.containsKey(qIdx)) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Explanation: ${quiz.explanation}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // Study Timetable Generator
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Automated Gemini Study Timetable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val timetable = onGenerateAiText("Generate a balanced daily study timetable for a CS student preparing for midterm exams")
                                chatHistory = chatHistory + Pair("Gemini Timetable Engine", timetable)
                                selectedMode = 0
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Optimal Daily Timetable")
                    }
                }
            }
        }
    }
}
