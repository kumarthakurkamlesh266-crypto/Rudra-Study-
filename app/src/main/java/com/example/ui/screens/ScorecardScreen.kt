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
import com.example.data.local.DailyScorecardEntity
import com.example.ui.RudraViewModel
import com.example.ui.components.RudraTopAppBar
import com.example.ui.theme.*

@Composable
fun ScorecardScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit
) {
    val todayCard by viewModel.todayScorecard.collectAsStateWithLifecycle()
    val pastCards by viewModel.pastScorecards.collectAsStateWithLifecycle()
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()

    var wokeUp by remember(todayCard) { mutableStateOf(todayCard?.wokeUpOnTime ?: false) }
    var b1 by remember(todayCard) { mutableStateOf(todayCard?.completedBlock1 ?: false) }
    var b3 by remember(todayCard) { mutableStateOf(todayCard?.completedBlock3 ?: false) }
    var fitness by remember(todayCard) { mutableStateOf(todayCard?.completedFitness ?: false) }
    var b5 by remember(todayCard) { mutableStateOf(todayCard?.completedBlock5 ?: false) }
    var shutdown by remember(todayCard) { mutableStateOf(todayCard?.didShutdownRitual ?: false) }
    var noPhone by remember(todayCard) { mutableStateOf(todayCard?.noPhoneBlockedHours ?: false) }

    var journalDone by remember(todayCard) { mutableStateOf(todayCard?.journalLineDone ?: "") }
    var journalMissed by remember(todayCard) { mutableStateOf(todayCard?.journalLineMissed ?: "") }
    var journalFocus by remember(todayCard) { mutableStateOf(todayCard?.journalLineFocusTomorrow ?: "") }

    val currentScore = (if (wokeUp) 1 else 0) +
            (if (b1) 1 else 0) +
            (if (b3) 1 else 0) +
            (if (fitness) 1 else 0) +
            (if (b5) 1 else 0) +
            (if (shutdown) 1 else 0) +
            (if (noPhone) 1 else 0)

    val scoreColor = when {
        currentScore >= 5 -> RudraEmerald
        currentScore >= 3 -> RudraAmber
        else -> RudraRose
    }

    val scoreStatusText = when {
        currentScore >= 5 -> "Green Day (Optimal Execution)"
        currentScore >= 3 -> "Yellow Day (Survived)"
        else -> "Red Day (Emergency Protocol)"
    }

    fun saveScorecard() {
        viewModel.updateTodayScorecard(
            wokeUp = wokeUp,
            b1 = b1,
            b3 = b3,
            fitness = fitness,
            b5 = b5,
            shutdown = shutdown,
            noPhone = noPhone,
            jDone = journalDone,
            jMissed = journalMissed,
            jFocus = journalFocus,
            isEmergency = currentScore < 3,
            isLowEnergy = isLowEnergy
        )
    }

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "Daily Discipline Scorecard",
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
            // Score Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, scoreColor.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("scorecard_hero_card")
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "TODAY'S DISCIPLINE SCORE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$currentScore / 7 Points",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = scoreColor)
                            )
                            Text(
                                text = scoreStatusText,
                                style = MaterialTheme.typography.bodySmall.copy(color = scoreColor, fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = scoreColor,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // 7 Non-Negotiable Checkboxes
            item {
                Text(
                    text = "THE 7 DAILY NON-NEGOTIABLES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().testTag("scorecard_checklist")
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ScoreCheckItem("1. Woke up at 6:30 AM (Phone 0 sec in bed)", wokeUp) { wokeUp = it; saveScorecard() }
                        ScoreCheckItem("2. Study Block 1 completed (6:15 - 7:30 AM)", b1) { b1 = it; saveScorecard() }
                        ScoreCheckItem("3. Study Block 3 completed (4:45 - 6:00 PM)", b3) { b3 = it; saveScorecard() }
                        ScoreCheckItem("4. 15-min Fitness executed (6:00 - 6:15 PM)", fitness) { fitness = it; saveScorecard() }
                        ScoreCheckItem("5. Study Block 5 / Revision done (8:45 - 9:45 PM)", b5) { b5 = it; saveScorecard() }
                        ScoreCheckItem("6. 9:45 PM Shutdown ritual executed (Phone parked outside)", shutdown) { shutdown = it; saveScorecard() }
                        ScoreCheckItem("7. Zero social media / phone during study blocks", noPhone) { noPhone = it; saveScorecard() }
                    }
                }
            }

            // 3-Line Evening Journal
            item {
                Text(
                    text = "3-LINE EVENING JOURNAL (NIGHT REFLECTION)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().testTag("scorecard_journal_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = journalDone,
                            onValueChange = { journalDone = it; saveScorecard() },
                            label = { Text("1. Aaj kya kiya? (3 main achievements)") },
                            modifier = Modifier.fillMaxWidth().testTag("journal_done_input")
                        )
                        OutlinedTextField(
                            value = journalMissed,
                            onValueChange = { journalMissed = it; saveScorecard() },
                            label = { Text("2. Kya miss hua aur kyon? (Objective root cause)") },
                            modifier = Modifier.fillMaxWidth().testTag("journal_missed_input")
                        )
                        OutlinedTextField(
                            value = journalFocus,
                            onValueChange = { journalFocus = it; saveScorecard() },
                            label = { Text("3. Kal ka EK focus kya hai? (Chhota Knob)") },
                            modifier = Modifier.fillMaxWidth().testTag("journal_focus_input")
                        )
                    }
                }
            }

            // Recent Score History
            if (pastCards.isNotEmpty()) {
                item {
                    Text(
                        text = "PAST SCORE HISTORY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                items(pastCards.take(7)) { card ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(card.dateString, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("${card.totalScore} / 7 Points", color = if (card.totalScore >= 5) RudraEmerald else if (card.totalScore >= 3) RudraAmber else RudraRose, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreCheckItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = RudraEmerald)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                color = if (checked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
