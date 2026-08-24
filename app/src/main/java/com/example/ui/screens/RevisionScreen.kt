package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.RevisionTaskEntity
import com.example.ui.RudraViewModel
import com.example.ui.components.RudraTopAppBar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RevisionScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit
) {
    val pendingTasks by viewModel.pendingRevisions.collectAsStateWithLifecycle()
    val allTasks by viewModel.allRevisions.collectAsStateWithLifecycle()
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("PENDING") } // "PENDING", "COMPLETED"

    val displayedTasks = if (activeTab == "PENDING") pendingTasks else allTasks.filter { it.isCompleted }

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "Spaced Repetition System",
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
            // Ebbinghaus Curve Info Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().testTag("spaced_repetition_info_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Repeat, contentDescription = null, tint = RudraAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AUTOMATIC 1-3-7-SUNDAY PROTOCOL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber, letterSpacing = 0.8.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Jab bhi aap koi topic padhte hain ya mark karte hain, system automatically Same-Day (4h), +1 Day, +3 Days, aur +7 Days par active recall revision task generate kar deta hai.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // Tab Switcher
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = activeTab == "PENDING",
                        onClick = { activeTab = "PENDING" },
                        label = { Text("Due For Revision (${pendingTasks.size})") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RudraAmber, selectedLabelColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("tab_pending_revisions")
                    )
                    FilterChip(
                        selected = activeTab == "COMPLETED",
                        onClick = { activeTab = "COMPLETED" },
                        label = { Text("Retained / History") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RudraEmerald, selectedLabelColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("tab_completed_revisions")
                    )
                }
            }

            // Tasks List
            if (displayedTasks.isEmpty()) {
                item {
                    Text(
                        text = if (activeTab == "PENDING") "All caught up! No pending revision tasks." else "No completed revisions yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(displayedTasks, key = { it.id }) { task ->
                    RevisionTaskCard(
                        task = task,
                        onMarkDone = {
                            viewModel.completeRevision(task.id, task.topicId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RevisionTaskCard(
    task: RevisionTaskEntity,
    onMarkDone: () -> Unit
) {
    val intervalBadge = when (task.intervalType) {
        "SAME_DAY" -> "Same-Day (4h)"
        "PLUS_1" -> "+1 Day Recall"
        "PLUS_3" -> "+3 Days Deep"
        "PLUS_7" -> "+7 Days Permanent"
        else -> "Spaced"
    }

    val intervalColor = when (task.intervalType) {
        "SAME_DAY" -> RudraAmber
        "PLUS_1" -> RudraCyan
        "PLUS_3" -> RudraPurple
        else -> RudraEmerald
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().testTag("revision_card_${task.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = intervalColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = intervalBadge,
                        color = intervalColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(task.dueDateTimestamp))
                Text(
                    text = "Due: $dateFormat",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.topicName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            )

            Text(
                text = "${task.subject} • ${task.chapterName}",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (!task.isCompleted) {
                Button(
                    onClick = onMarkDone,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RudraEmerald, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.fillMaxWidth().testTag("mark_revision_done_button_${task.id}")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recalled & Completed", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = RudraEmerald, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Retained successfully", color = RudraEmerald, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
