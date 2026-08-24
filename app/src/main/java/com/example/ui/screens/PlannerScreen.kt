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

@Composable
fun PlannerScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit
) {
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()

    val weeklyRotation = listOf(
        Triple("Monday", "Physics (Hard Numerical) + Chemistry", "Morning: Electrostatics / Current • Evening: Organic Reactions"),
        Triple("Tuesday", "Chemistry (Physical/Organic) + Biology", "Morning: Solutions / Electrochemistry • Evening: Genetics / Reproduction"),
        Triple("Wednesday", "Biology (High Weightage) + English", "Morning: Molecular Biology • Evening: Literature & Writing Skills"),
        Triple("Thursday", "Physics (Derivations) + Hindi", "Morning: Magnetism & Optics • Evening: Hindi Vyakaran & Prose"),
        Triple("Friday", "Chemistry (Inorganic/Coordination) + Physics", "Morning: d & f block • Evening: Wave Optics & Modern Physics"),
        Triple("Saturday", "Biology + Full Weekly Spaced Revision", "Morning: Biotechnology • Evening: Active Recall of all Mon-Fri notes"),
        Triple("Sunday", "Full Mock Test + Weak Subject Recovery", "Morning: 3-Hour Timed Mock Paper • Evening: Weak Topic Surgery & Error Log")
    )

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "Weekly Planner & Rotation",
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
            // Principle Banner
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = RudraAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM ROTATION > RANDOM DECISIONS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber, letterSpacing = 0.8.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kab kya padhna hai, subah uthke sochna nahi hai. Morning slot always handles the highest cognitive load subject.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // Days Matrix
            items(weeklyRotation.size) { index ->
                val (day, subjects, focus) = weeklyRotation[index]
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().testTag("planner_day_$day")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = subjects,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = focus,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }
}
