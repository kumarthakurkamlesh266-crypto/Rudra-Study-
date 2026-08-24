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
import com.example.data.local.TimelineBlockEntity
import com.example.ui.RudraViewModel
import com.example.ui.components.RudraTopAppBar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit
) {
    val timelineBlocks by viewModel.timelineBlocks.collectAsStateWithLifecycle()
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingBlock by remember { mutableStateOf<TimelineBlockEntity?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "Master Timeline & Routine",
                onMenuClick = onOpenDrawer,
                currentStreak = streak,
                isLowEnergy = isLowEnergy,
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_timeline_block_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Block", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.testTag("reset_timeline_button")
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = "Reset", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = RudraAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DAILY EXECUTION SKELETON (5.5h DEEP STUDY)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber, letterSpacing = 0.8.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fixed anchors (black) provide structure. Action triggers eliminate 'kya karoon' decision fatigue. Check off completed blocks as you execute.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // Timeline Blocks List
            items(timelineBlocks, key = { it.id }) { block ->
                TimelineBlockCard(
                    block = block,
                    onToggleComplete = { completed ->
                        viewModel.toggleBlockCompleted(block.id, completed)
                    },
                    onEdit = { editingBlock = block },
                    onDelete = { viewModel.deleteTimelineBlock(block.id) }
                )
            }
        }
    }

    // Add Block Dialog
    if (showAddDialog) {
        BlockEditorDialog(
            initialBlock = null,
            onDismiss = { showAddDialog = false },
            onSave = { title, subtitle, start, end, type, trigger, backup ->
                viewModel.addTimelineBlock(title, subtitle, start, end, type, trigger, backup)
                showAddDialog = false
            }
        )
    }

    // Edit Block Dialog
    editingBlock?.let { block ->
        BlockEditorDialog(
            initialBlock = block,
            onDismiss = { editingBlock = null },
            onSave = { title, subtitle, start, end, type, trigger, backup ->
                viewModel.updateTimelineBlock(
                    block.copy(
                        title = title,
                        subtitle = subtitle,
                        startTime = start,
                        endTime = end,
                        type = type,
                        triggerAction = trigger,
                        backupVersion = backup
                    )
                )
                editingBlock = null
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset to Master Routine?") },
            text = { Text("This will restore the original 19 Rudra Life OS routine blocks.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetTimeline()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TimelineBlockCard(
    block: TimelineBlockEntity,
    onToggleComplete: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val typeColor = when (block.type) {
        "DEEP_FOCUS" -> RudraAmber
        "REVISION" -> RudraCyan
        "FITNESS" -> RudraEmerald
        "SCHOOL" -> RudraBlue
        "SHUTDOWN", "SLEEP" -> RudraPurple
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (block.isCompletedToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (block.isCompletedToday) RudraEmerald.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("timeline_block_${block.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Time pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.width(90.dp)
                ) {
                    Text(
                        text = "${block.startTime} – ${block.endTime}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = typeColor,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = block.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (block.isCompletedToday) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = block.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        ),
                        maxLines = if (expanded) Int.MAX_VALUE else 1
                    )
                }

                Checkbox(
                    checked = block.isCompletedToday,
                    onCheckedChange = onToggleComplete,
                    colors = CheckboxDefaults.colors(checkedColor = RudraEmerald),
                    modifier = Modifier.testTag("block_checkbox_${block.id}")
                )
            }

            // Expanded Details
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (block.triggerAction.isNotBlank()) {
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = "Action Trigger: ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber)
                            )
                            Text(
                                text = block.triggerAction,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                            )
                        }
                    }

                    if (block.backupVersion.isNotBlank()) {
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = "Backup Version: ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraCyan)
                            )
                            Text(
                                text = block.backupVersion,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onEdit) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 12.sp)
                        }
                        TextButton(onClick = onDelete) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = RudraRose, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", fontSize = 12.sp, color = RudraRose)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlockEditorDialog(
    initialBlock: TimelineBlockEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, subtitle: String, start: String, end: String, type: String, trigger: String, backup: String) -> Unit
) {
    var title by remember { mutableStateOf(initialBlock?.title ?: "") }
    var subtitle by remember { mutableStateOf(initialBlock?.subtitle ?: "") }
    var start by remember { mutableStateOf(initialBlock?.startTime ?: "06:00") }
    var end by remember { mutableStateOf(initialBlock?.endTime ?: "07:00") }
    var type by remember { mutableStateOf(initialBlock?.type ?: "DEEP_FOCUS") }
    var trigger by remember { mutableStateOf(initialBlock?.triggerAction ?: "") }
    var backup by remember { mutableStateOf(initialBlock?.backupVersion ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialBlock == null) "Add Routine Block" else "Edit Block", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Block Title (e.g. Study Block 1)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Subtitle / Subject Focus") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = start,
                        onValueChange = { start = it },
                        label = { Text("Start (HH:mm)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = end,
                        onValueChange = { end = it },
                        label = { Text("End (HH:mm)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text("Physical Action Trigger") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = backup,
                    onValueChange = { backup = it },
                    label = { Text("Backup Version (If exhausted)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, subtitle, start, end, type, trigger, backup)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
