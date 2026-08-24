package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.RudraViewModel
import com.example.ui.components.RudraTopAppBar
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit
) {
    val apiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()
    val validationStatus by viewModel.apiValidationStatus.collectAsStateWithLifecycle()
    val selectedBoard by viewModel.selectedBoard.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()

    var apiKeyInput by remember(apiKey) { mutableStateOf(apiKey) }
    var showResetDbConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "Settings & Configuration",
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {

            // 1. GEMINI API KEY CONFIGURATION
            item {
                Text(
                    text = "AI INTELLIGENCE CONFIGURATION",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber, letterSpacing = 1.sp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().testTag("api_key_settings_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Google Gemini API Key",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Powers AI Tutor, Personal Coach reviews, Test Generator, and Screen Time diagnostics.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            placeholder = { Text("AIzaSy...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("gemini_api_key_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateApiKey(apiKeyInput.trim())
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black),
                                modifier = Modifier.testTag("save_api_key_button")
                            ) {
                                Text("Save & Test Connection", fontWeight = FontWeight.Bold)
                            }

                            validationStatus?.let { status ->
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (status.contains("Active")) RudraEmerald else RudraRose,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 2. BOARD & ACADEMIC PREFERENCES
            item {
                Text(
                    text = "ACADEMIC BOARD PREFERENCES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Active Examination Board:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("BSEB", "CBSE").forEach { b ->
                                FilterChip(
                                    selected = selectedBoard == b,
                                    onClick = { viewModel.setSelectedBoard(b) },
                                    label = { Text(if (b == "BSEB") "Bihar Board (BSEB 12th)" else "CBSE (Class 12)") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RudraAmber, selectedLabelColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. WORKMANAGER BACKGROUND SERVICE
            item {
                Text(
                    text = "BACKGROUND WORKMANAGER SERVICES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraCyan, letterSpacing = 1.sp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().testTag("workmanager_settings_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = RudraCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PDF Question Extraction Worker", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Text(
                            text = "WorkManager automatically runs in the background to extract text from stored PDFs, consult Gemini API, and structure questions into 'Question', 'Chapter', and 'Difficulty' fields.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.triggerBackgroundPdfExtraction(forceReanalyze = false)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RudraCyan, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("settings_run_worker_btn")
                            ) {
                                Text("Run Worker Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.schedulePeriodicPdfExtraction(intervalHours = 24)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("settings_schedule_periodic_btn")
                            ) {
                                Text("Schedule 24h Sync", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 4. THEME MODE
            item {
                Text(
                    text = "THEME & VISUAL APPEARANCE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("App Theme Mode:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("DARK" to "Dark (Default)", "LIGHT" to "Light", "SYSTEM" to "System").forEach { (mode, label) ->
                                FilterChip(
                                    selected = themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RudraAmber, selectedLabelColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. DATA & SYSTEM RESET
            item {
                Text(
                    text = "SYSTEM DATA & MAINTENANCE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { showResetDbConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RudraRose),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RudraRose),
                            modifier = Modifier.fillMaxWidth().testTag("reset_all_data_button")
                        ) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = RudraRose, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset All Data & Re-seed Syllabus")
                        }
                    }
                }
            }
        }
    }

    if (showResetDbConfirm) {
        AlertDialog(
            onDismissRequest = { showResetDbConfirm = false },
            title = { Text("Reset to Initial Seed?") },
            text = { Text("This will re-initialize all default timeline blocks, syllabus topics, and preloaded PYQs.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToInitialSeed(selectedBoard)
                        showResetDbConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RudraRose, contentColor = Color.White)
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDbConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
