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
import com.example.ui.components.SimpleMarkdownCard
import com.example.ui.theme.*

@Composable
fun AiCoachScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit
) {
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()
    val coachResponse by viewModel.aiCoachResponse.collectAsStateWithLifecycle()
    val isCoachLoading by viewModel.isAiCoachLoading.collectAsStateWithLifecycle()
    val screenTimeResult by viewModel.screenTimeAnalysisResult.collectAsStateWithLifecycle()
    val isScreenTimeLoading by viewModel.isScreenTimeAnalyzing.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("COACH") } // "COACH", "SCREEN_TIME"
    var screenTimeInputText by remember { mutableStateOf("Instagram: 2h 15m (Reels after 10 PM), YouTube: 1h 30m, Total: 5h 20m, Pickups: 92") }

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "AI OS Coach",
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
            // Tab Switcher
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = activeTab == "COACH",
                        onClick = { activeTab = "COACH" },
                        label = { Text("Discipline & Study Coach") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RudraAmber, selectedLabelColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("tab_coach_review")
                    )
                    FilterChip(
                        selected = activeTab == "SCREEN_TIME",
                        onClick = { activeTab = "SCREEN_TIME" },
                        label = { Text("Screen Time Analyzer") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RudraRose, selectedLabelColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("tab_screen_time_analysis")
                    )
                }
            }

            if (activeTab == "COACH") {
                // Coach Section
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = RudraAmber, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RUDRA OS PERSONAL COACH",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber, letterSpacing = 0.8.sp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Zero motivational lectures. Evaluates your live database metrics (scorecard, weak topics, completed blocks) and gives exact adjustments based on 'System > Willpower'.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }

                // Action Buttons (Daily, Weekly, Monthly, Recovery)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.requestAiCoachAnalysis("DAILY") },
                                enabled = !isCoachLoading,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black),
                                modifier = Modifier.weight(1f).testTag("daily_coach_review_button")
                            ) {
                                Text("Daily Review", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.requestAiCoachAnalysis("WEEKLY") },
                                enabled = !isCoachLoading,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Weekly Audit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.requestAiCoachAnalysis("RECOVERY_PLAN") },
                            enabled = !isCoachLoading,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RudraRose),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RudraRose),
                            modifier = Modifier.fillMaxWidth().testTag("recovery_plan_button")
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = RudraRose, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Emergency Day: Generate Recovery Plan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isCoachLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(color = RudraAmber, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Coach is auditing your system data...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                coachResponse?.let { response ->
                    item {
                        SimpleMarkdownCard(markdownText = response, modifier = Modifier.testTag("coach_response_card"))
                    }
                }

            } else {
                // Screen Time Analyzer
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = RudraRose, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DOPAMINE BASELINE & SCREEN TIME AUDIT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraRose, letterSpacing = 0.8.sp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enter your Digital Wellbeing / Screen Time stats or logs to identify friction leaks sabotaging your focus blocks.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            OutlinedTextField(
                                value = screenTimeInputText,
                                onValueChange = { screenTimeInputText = it },
                                label = { Text("Screen Time Stats / Top Distraction Apps") },
                                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("screen_time_input"),
                                maxLines = 4
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    if (screenTimeInputText.isNotBlank()) {
                                        viewModel.analyzeScreenTime(screenTimeInputText)
                                    }
                                },
                                enabled = !isScreenTimeLoading && screenTimeInputText.isNotBlank(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RudraRose, contentColor = Color.White),
                                modifier = Modifier.fillMaxWidth().testTag("analyze_screen_time_button")
                            ) {
                                if (isScreenTimeLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Analyzing Dopamine Leak...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(imageVector = Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Diagnose Phone Addiction Friction", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                screenTimeResult?.let { result ->
                    item {
                        SimpleMarkdownCard(markdownText = result, modifier = Modifier.testTag("screen_time_result_card"))
                    }
                }
            }
        }
    }
}
