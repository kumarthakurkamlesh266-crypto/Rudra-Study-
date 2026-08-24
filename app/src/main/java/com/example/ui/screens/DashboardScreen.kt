package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TimelineBlockEntity
import com.example.ui.RudraViewModel
import com.example.ui.components.RudraTopAppBar
import com.example.ui.components.StatCard
import com.example.ui.navigation.NavItem
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit,
    onNavigate: (NavItem) -> Unit
) {
    val timelineBlocks by viewModel.timelineBlocks.collectAsStateWithLifecycle()
    val todayCard by viewModel.todayScorecard.collectAsStateWithLifecycle()
    val syllabusSummary by viewModel.syllabusSummary.collectAsStateWithLifecycle()
    val pendingRevisions by viewModel.pendingRevisions.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()

    val (currentBlock, nextBlock) = remember(timelineBlocks) {
        viewModel.repository.findCurrentAndNextBlock(timelineBlocks)
    }

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "Rudra Life OS",
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

            // 1. CURRENT FOCUS CARD (The Heart of the OS)
            item {
                CurrentFocusHeroCard(
                    currentBlock = currentBlock,
                    nextBlock = nextBlock,
                    onStartFocus = { onNavigate(NavItem.STUDY) },
                    onViewTimeline = { onNavigate(NavItem.TIMELINE) }
                )
            }

            // 2. TODAY'S PROGRESS (Study Hours, Topics, Revision, Discipline)
            item {
                Text(
                    text = "TODAY'S PROGRESS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val studyMinutes = todayCard?.studyHoursMinutes ?: 0
                    val studyHoursStr = if (studyMinutes >= 60) "${studyMinutes / 60}h ${studyMinutes % 60}m" else "${studyMinutes}m"
                    StatCard(
                        title = "Study Hours",
                        value = studyHoursStr,
                        subtitle = "Target: 5.5h",
                        icon = Icons.Default.Timer,
                        iconColor = RudraCyan,
                        modifier = Modifier.weight(1f).testTag("stat_study_hours")
                    )
                    StatCard(
                        title = "Topics Done",
                        value = "${syllabusSummary.revisedTopicsCount + syllabusSummary.masteredTopicsCount}",
                        subtitle = "Total: ${syllabusSummary.totalTopicsCount}",
                        icon = Icons.Default.MenuBook,
                        iconColor = RudraAmber,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate(NavItem.SYLLABUS) }
                            .testTag("stat_topics_done")
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Revision Due",
                        value = "${pendingRevisions.size}",
                        subtitle = "Spaced Tasks",
                        icon = Icons.Default.Repeat,
                        iconColor = RudraPurple,
                        modifier = Modifier.weight(1f).testTag("stat_revisions_due")
                    )
                    val score = todayCard?.totalScore ?: 0
                    val scoreColor = when {
                        score >= 5 -> RudraEmerald
                        score >= 3 -> RudraAmber
                        else -> RudraRose
                    }
                    StatCard(
                        title = "Discipline Score",
                        value = "$score / 7",
                        subtitle = when {
                            score >= 5 -> "Green Day (Optimal)"
                            score >= 3 -> "Yellow Day (Survived)"
                            else -> "Red Day (Emergency)"
                        },
                        icon = Icons.Default.CheckCircle,
                        iconColor = scoreColor,
                        modifier = Modifier.weight(1f).testTag("stat_discipline_score")
                    )
                }
            }

            // 3. UPCOMING (Next 3 scheduled blocks)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UPCOMING SCHEDULE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    TextButton(onClick = { onNavigate(NavItem.TIMELINE) }) {
                        Text("View All", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                UpcomingBlocksCard(
                    blocks = timelineBlocks,
                    currentBlock = currentBlock,
                    onNavigateTimeline = { onNavigate(NavItem.TIMELINE) }
                )
            }

            // 4. QUICK STATS & RECOVERY ACTIONS
            item {
                Text(
                    text = "SYSTEM CONTROLS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                SystemControlsCard(
                    isLowEnergy = isLowEnergy,
                    onToggleLowEnergy = { viewModel.toggleLowEnergyMode(!isLowEnergy) },
                    onOpenAiCoach = { onNavigate(NavItem.AI_COACH) },
                    onOpenScorecard = { onNavigate(NavItem.ANALYTICS) }
                )
            }
        }
    }
}

@Composable
fun CurrentFocusHeroCard(
    currentBlock: TimelineBlockEntity?,
    nextBlock: TimelineBlockEntity?,
    onStartFocus: () -> Unit,
    onViewTimeline: () -> Unit
) {
    val blockTitle = currentBlock?.title ?: "Study Block 1 (Deep Focus)"
    val blockSubtitle = currentBlock?.subtitle ?: "Hardest subject — Physics / Chem numericals"
    val blockTime = if (currentBlock != null) "${currentBlock.startTime} – ${currentBlock.endTime}" else "06:15 – 07:30"
    val trigger = currentBlock?.triggerAction ?: "Water peene ke turant baad, bina phone dekhe"
    val nextTitle = nextBlock?.title ?: "Breakfast + Buffer"
    val nextTime = nextBlock?.startTime ?: "07:30"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, RudraAmber.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("current_focus_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Active Badge + Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(RudraEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WHAT TO DO NOW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = RudraEmerald
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = blockTime,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Activity Name
            Text(
                text = blockTitle,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = blockSubtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Trigger Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = RudraAmber.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, RudraAmber.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Trigger",
                        tint = RudraAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Action Trigger: $trigger",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Next Activity preview & Action button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NEXT ACTIVITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                    Text(
                        text = "$nextTitle ($nextTime)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onStartFocus,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RudraAmber,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("start_focus_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Focus Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun UpcomingBlocksCard(
    blocks: List<TimelineBlockEntity>,
    currentBlock: TimelineBlockEntity?,
    onNavigateTimeline: () -> Unit
) {
    val currentIndex = blocks.indexOfFirst { it.id == currentBlock?.id }
    val upcoming = if (currentIndex != -1 && blocks.isNotEmpty()) {
        val list = mutableListOf<TimelineBlockEntity>()
        for (i in 1..3) {
            val next = blocks.getOrNull((currentIndex + i) % blocks.size)
            if (next != null) list.add(next)
        }
        list
    } else {
        blocks.take(3)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (upcoming.isEmpty()) {
                Text(
                    text = "No upcoming blocks loaded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                upcoming.forEachIndexed { index, block ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTimeline() }
                            .padding(vertical = 6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.width(60.dp)
                        ) {
                            Text(
                                text = block.startTime,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = block.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = block.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                    if (index < upcoming.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SystemControlsCard(
    isLowEnergy: Boolean,
    onToggleLowEnergy: () -> Unit,
    onOpenAiCoach: () -> Unit,
    onOpenScorecard: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Low Energy Toggle row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Low-Energy / Fatigue Mode",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = if (isLowEnergy) "Active: Shrinks blocks to 15-30m without guilt" else "Normal 5.5h deep schedule",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
                Switch(
                    checked = isLowEnergy,
                    onCheckedChange = { onToggleLowEnergy() },
                    colors = SwitchDefaults.colors(checkedThumbColor = RudraAmber, checkedTrackColor = RudraAmber.copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("low_energy_switch")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Fast Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenAiCoach,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("quick_coach_button")
                ) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Coach Review", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onOpenScorecard,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("quick_scorecard_button")
                ) {
                    Icon(imageVector = Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Daily Scorecard", fontSize = 12.sp)
                }
            }
        }
    }
}
