package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.RudraViewModel
import com.example.ui.components.RudraTopAppBar
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit
) {
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()
    val focusSessions by viewModel.focusSessions.collectAsStateWithLifecycle()

    var selectedDurationMinutes by remember { mutableIntStateOf(50) }
    var remainingSeconds by remember { mutableIntStateOf(50 * 60) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf("Physics") }
    var currentTopicText by remember { mutableStateOf("Electrostatics Numericals") }
    var parkingLotInput by remember { mutableStateOf("") }
    val parkingLotList = remember { mutableStateListOf<String>() }
    var showCompletionDialog by remember { mutableStateOf(false) }

    // 2-Minute Rule Mode
    var isTwoMinuteMode by remember { mutableStateOf(false) }

    // Timer Effect
    LaunchedEffect(isTimerRunning, remainingSeconds) {
        if (isTimerRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        } else if (isTimerRunning && remainingSeconds == 0) {
            isTimerRunning = false
            showCompletionDialog = true
        }
    }

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "Study & Focus Mode",
                onMenuClick = onOpenDrawer,
                currentStreak = streak,
                isLowEnergy = isLowEnergy
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {

            // 1. Anti-Procrastination Banner: 2-Minute Rule
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isTwoMinuteMode) RudraAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isTwoMinuteMode) RudraAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = RudraAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "THE 2-MINUTE ANTI-PROCRASTINATION RULE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber, letterSpacing = 0.8.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dimaag pooray block se darta hai, task se nahi. Bas 2 minute ke liye baitho aur pehla sawaal kholo. Momentum will take over.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                isTwoMinuteMode = true
                                selectedDurationMinutes = 2
                                remainingSeconds = 120
                                isTimerRunning = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RudraAmber),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RudraAmber),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("launch_2min_rule_button")
                        ) {
                            Text("Launch 2-Minute Micro-Start", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Central Timer Display
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("focus_timer_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Subject & Topic Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(selectedSubject, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    listOf("Physics", "Chemistry", "Biology", "Hindi", "English").forEach { sub ->
                                        DropdownMenuItem(
                                            text = { Text(sub) },
                                            onClick = {
                                                selectedSubject = sub
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Preset chips
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(25, 50, 75).forEach { mins ->
                                    FilterChip(
                                        selected = selectedDurationMinutes == mins && !isTwoMinuteMode,
                                        onClick = {
                                            if (!isTimerRunning) {
                                                isTwoMinuteMode = false
                                                selectedDurationMinutes = mins
                                                remainingSeconds = mins * 60
                                            }
                                        },
                                        label = { Text("${mins}m", fontSize = 11.sp) },
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = currentTopicText,
                            onValueChange = { currentTopicText = it },
                            label = { Text("Topic / Target Problem") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("focus_topic_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large Timer Countdown
                        val minutes = remainingSeconds / 60
                        val seconds = remainingSeconds % 60
                        val timeStr = String.format("%02d:%02d", minutes, seconds)

                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = if (isTimerRunning) RudraAmber else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("timer_display_text")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isTimerRunning) "Phone outside room • Zero distraction" else "Ready to execute",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Control Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    isTimerRunning = !isTimerRunning
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTimerRunning) RudraRose else RudraAmber,
                                    contentColor = Color.Black
                                ),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                                modifier = Modifier.testTag("toggle_timer_button")
                            ) {
                                Icon(
                                    imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isTimerRunning) "Pause Block" else "Start Deep Focus",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    isTimerRunning = false
                                    remainingSeconds = selectedDurationMinutes * 60
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
                            }
                        }
                    }
                }
            }

            // 3. PARKING LOT NOTEBOOK (Anti-Distraction Buffer)
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().testTag("parking_lot_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, tint = RudraCyan)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PARKING LOT NOTEBOOK",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = RudraCyan)
                                )
                            }
                            Text(
                                text = "Distraction Dump",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Beech mein koi random khayal aaye ('video dekhni hai', 'message karna hai'), yahan dump kar do aur wapas study mein lag jao.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = parkingLotInput,
                                onValueChange = { parkingLotInput = it },
                                placeholder = { Text("Dump random thought here...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("parking_lot_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (parkingLotInput.isNotBlank()) {
                                        parkingLotList.add(0, parkingLotInput.trim())
                                        parkingLotInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .testTag("add_parking_thought_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        if (parkingLotList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            parkingLotList.forEachIndexed { index, thought ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "• $thought",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { parkingLotList.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. RECENT FOCUS SESSIONS
            item {
                Text(
                    text = "LOGGED FOCUS SESSIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            if (focusSessions.isEmpty()) {
                item {
                    Text(
                        text = "No study sessions logged today yet. Start a timer to build consistency.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                items(focusSessions.take(5)) { session ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = session.subject + " • " + session.topicOrTask,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(session.timestamp))
                                Text(
                                    text = "$timeFormat • Quality: ${"★".repeat(session.qualityRating)}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = RudraEmerald.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "+${session.durationMinutes}m",
                                    color = RudraEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Completion Dialog
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { showCompletionDialog = false },
            title = { Text("Study Block Completed! 🎉", fontWeight = FontWeight.Bold) },
            text = {
                Text("Great work. $selectedDurationMinutes minutes added to your daily study progress.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logFocusSession(
                            subject = selectedSubject,
                            topic = currentTopicText,
                            durationMinutes = selectedDurationMinutes,
                            thoughts = parkingLotList.joinToString("; "),
                            rating = 5
                        )
                        showCompletionDialog = false
                    }
                ) {
                    Text("Log to Scorecard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompletionDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
