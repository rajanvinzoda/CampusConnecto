package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CampusEvent
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    events: List<CampusEvent>,
    onRegisterEvent: (String) -> Unit,
    onCreateEvent: (title: String, category: String, date: String, location: String, description: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }

    var showAddEventModal by remember { mutableStateOf(false) }
    var showQrModal by remember { mutableStateOf<CampusEvent?>(null) }

    // New Event State
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Workshop") }
    var newLocation by remember { mutableStateOf("Main Auditorium") }
    var newDescription by remember { mutableStateOf("") }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val displayDateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy") }

    // Selected date formatted
    val selectedDateStr = remember(selectedDate) { selectedDate.format(dateFormatter) }

    // Dates with scheduled events
    val eventDates = remember(events) {
        events.mapNotNull { evt ->
            try {
                LocalDate.parse(evt.date, dateFormatter)
            } catch (e: Exception) {
                null
            }
        }.toSet()
    }

    // Filter events for the selected date
    val selectedDateEvents = remember(events, selectedDate) {
        events.filter { evt ->
            evt.date == selectedDateStr || evt.date == selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Campus Event Calendar",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Interactive Month Grid & Date Inspection",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddEventModal = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Schedule Event")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Event")
                    }
                }
            }

            // Full Month Calendar Component
            FullMonthCalendar(
                currentYearMonth = currentYearMonth,
                onMonthChange = { currentYearMonth = it },
                selectedDate = selectedDate,
                onDateSelect = { selectedDate = it },
                eventDates = eventDates
            )

            // Header for Selected Date Events
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedDate.format(displayDateFormatter),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Badge {
                        Text("${selectedDateEvents.size} Events")
                    }
                }
            }

            // Scheduled Events List or Empty State
            if (selectedDateEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No events scheduled on this date",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Click 'Add Event' above to schedule a workshop, hackathon, or meeting for ${selectedDate.format(displayDateFormatter)}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(selectedDateEvents, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            onRegister = { onRegisterEvent(event.id) },
                            onShowQr = { showQrModal = event }
                        )
                    }
                }
            }
        }

        // Add Event Modal
        if (showAddEventModal) {
            AlertDialog(
                onDismissRequest = { showAddEventModal = false },
                title = { Text("Schedule Event for ${selectedDate.format(displayDateFormatter)}") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Event Title *") },
                            placeholder = { Text("e.g. AI & Robotics Hackathon 2026") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newCategory,
                            onValueChange = { newCategory = it },
                            label = { Text("Category (Workshop, Hackathon, Cultural) *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newLocation,
                            onValueChange = { newLocation = it },
                            label = { Text("Location / Venue *") },
                            placeholder = { Text("e.g. CS Seminar Hall, Room 302") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newDescription,
                            onValueChange = { newDescription = it },
                            label = { Text("Event Description *") },
                            placeholder = { Text("Enter event agenda, prerequisites, or contact info...") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank()) {
                                onCreateEvent(
                                    newTitle.trim(),
                                    newCategory.trim(),
                                    selectedDateStr,
                                    newLocation.trim(),
                                    newDescription.trim()
                                )
                                newTitle = ""
                                newDescription = ""
                                showAddEventModal = false
                                Toast.makeText(context, "Event scheduled successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please enter event title!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Schedule Event")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddEventModal = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // QR Attendance Modal Dialog
        showQrModal?.let { event ->
            AlertDialog(
                onDismissRequest = { showQrModal = null },
                title = { Text("QR Attendance Badge") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier.size(180.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = "QR Code",
                                        tint = Color.Black,
                                        modifier = Modifier.size(120.dp)
                                    )
                                    Text(
                                        text = event.qrCodeSeed.take(16),
                                        color = Color.DarkGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(event.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Scan at hall entrance for automated attendance & certificate generation", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                confirmButton = {
                    Button(onClick = { showQrModal = null }) {
                        Text("Close Ticket")
                    }
                }
            )
        }
    }
}

@Composable
fun FullMonthCalendar(
    currentYearMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedDate: LocalDate,
    onDateSelect: (LocalDate) -> Unit,
    eventDates: Set<LocalDate>
) {
    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek.value % 7 // 0 for Sunday
    val monthTitle = currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentYearMonth.year

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Header with Prev/Next Navigation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { onMonthChange(currentYearMonth.minusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                }
                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onMonthChange(currentYearMonth.plusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weekday Headers
            val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                weekdays.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days Grid
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until rows) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (col in 0 until 7) {
                            val dayNumber = row * 7 + col - firstDayOfWeek + 1
                            if (dayNumber in 1..daysInMonth) {
                                val date = currentYearMonth.atDay(dayNumber)
                                val isSelected = date == selectedDate
                                val hasEvents = eventDates.contains(date)

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else if (hasEvents) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            else Color.Transparent
                                        )
                                        .clickable { onDateSelect(date) }
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNumber.toString(),
                                            fontWeight = if (isSelected || hasEvents) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp
                                        )
                                        if (hasEvents && !isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: CampusEvent,
    onRegister: () -> Unit,
    onShowQr: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}")
    ) {
        Column {
            // Event Banner Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(event.bannerGradientStart),
                                Color(event.bannerGradientEnd)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = event.category,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(event.date, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(event.location, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${event.attendeesCount} Registered", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row {
                        if (event.isRegistered) {
                            OutlinedButton(
                                onClick = onShowQr,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("view_qr_attendance_button_${event.id}")
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = "QR Ticket")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("QR Ticket")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Button(
                            onClick = onRegister,
                            colors = if (event.isRegistered) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("register_event_button_${event.id}")
                        ) {
                            Icon(
                                imageVector = if (event.isRegistered) Icons.Default.Check else Icons.Default.EventAvailable,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (event.isRegistered) "Registered" else "Register Now")
                        }
                    }
                }
            }
        }
    }
}
