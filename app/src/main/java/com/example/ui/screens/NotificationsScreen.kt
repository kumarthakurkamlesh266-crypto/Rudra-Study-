package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.TimelineNotificationHelper
import com.example.ui.RudraViewModel
import com.example.ui.components.RudraTopAppBar
import com.example.ui.theme.*

@Composable
fun NotificationsScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    var testTriggered by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "Routine Notifications & Alarms",
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
            // Notification Master Toggle Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().testTag("notification_toggle_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Timeline Alarms & Alerts",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (notificationsEnabled) "Active: 15m before, 5m before & at start" else "Disabled",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = RudraAmber, checkedTrackColor = RudraAmber.copy(alpha = 0.3f)),
                            modifier = Modifier.testTag("notification_master_switch")
                        )
                    }
                }
            }

            // Notification Rules Breakdown
            item {
                Text(
                    text = "NOTIFICATION PROTOCOLS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        NotificationRuleItem(
                            time = "15 Mins Before Block",
                            desc = "Pre-warning to finish current activity, drink water, and clear the study table.",
                            color = RudraAmber
                        )
                        NotificationRuleItem(
                            time = "5 Mins Before Block",
                            desc = "Transition trigger to open books / question bank and park the phone outside.",
                            color = RudraCyan
                        )
                        NotificationRuleItem(
                            time = "At Block Start Time",
                            desc = "Execution alert: 2-minute rule micro-start begins immediately.",
                            color = RudraEmerald
                        )
                        NotificationRuleItem(
                            time = "9:45 PM Shutdown Ritual",
                            desc = "High-priority non-negotiable bedtime preparation and phone parking reminder.",
                            color = RudraPurple
                        )
                    }
                }
            }

            // Test Notification Trigger Button
            item {
                Button(
                    onClick = {
                        TimelineNotificationHelper.showNotification(
                            context,
                            999,
                            "Rudra OS: Study Block Active",
                            "Physics Numericals: Start your 2-minute rule micro-momentum now."
                        )
                        testTriggered = true
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().testTag("trigger_test_notification_button")
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trigger Test Timeline Alert", fontWeight = FontWeight.Bold)
                }
            }

            if (testTriggered) {
                item {
                    Text(
                        text = "Alert dispatched to system notification drawer ✅",
                        color = RudraEmerald,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationRuleItem(time: String, desc: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(time, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = color)
            Text(desc, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}
