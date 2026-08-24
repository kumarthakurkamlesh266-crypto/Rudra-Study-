package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.PyqEntity
import com.example.ui.RudraViewModel
import com.example.ui.components.RudraTopAppBar
import com.example.ui.theme.*

@Composable
fun PyqBankScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit
) {
    val pyqs by viewModel.pyqList.collectAsStateWithLifecycle()
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()

    var activeSubject by remember { mutableStateOf("Physics") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ALL") } // "ALL", "MCQ", "SHORT", "LONG"

    val subjects = listOf("Physics", "Chemistry", "Biology", "Hindi", "English")

    val filteredPyqs = remember(pyqs, activeSubject, searchQuery, selectedType) {
        pyqs.filter { pyq ->
            pyq.subject.equals(activeSubject, ignoreCase = true) &&
            (searchQuery.isBlank() || pyq.questionText.contains(searchQuery, ignoreCase = true) || pyq.chapter.contains(searchQuery, ignoreCase = true)) &&
            (selectedType == "ALL" || pyq.questionType == selectedType)
        }
    }

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "Previous Year Questions (PYQ)",
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
            // Subject Tabs
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(subjects) { sub ->
                        FilterChip(
                            selected = activeSubject == sub,
                            onClick = { activeSubject = sub },
                            label = { Text(sub) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RudraAmber, selectedLabelColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("pyq_subject_$sub")
                        )
                    }
                }
            }

            // Search and Type Filter
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search question or chapter...", fontSize = 12.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("pyq_search_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("ALL", "MCQ", "SHORT", "LONG").forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type, fontSize = 10.sp) },
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }
                }
            }

            // Questions List
            if (filteredPyqs.isEmpty()) {
                item {
                    Text(
                        text = "No questions found matching your filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(filteredPyqs, key = { it.id }) { pyq ->
                    PyqQuestionCard(pyq = pyq)
                }
            }
        }
    }
}

@Composable
fun PyqQuestionCard(pyq: PyqEntity) {
    var expandedSolution by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().testTag("pyq_card_${pyq.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Badges Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = RudraAmber.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${pyq.board} ${pyq.year}",
                            color = RudraAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${pyq.marks} Marks • ${pyq.questionType}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = pyq.chapter,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Question Text
            Text(
                text = pyq.questionText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            )

            // Solution Trigger
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedSolution = !expandedSolution }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (expandedSolution) "Hide Step-by-Step Solution" else "View Step-by-Step Solution",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Icon(
                    imageVector = if (expandedSolution) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Solution Content
            AnimatedVisibility(visible = expandedSolution) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "ANSWER & MARKING SCHEME",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraEmerald, fontSize = 10.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pyq.answerText,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    )

                    if (pyq.stepByStepSolution.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DETAILED STEPS:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraCyan, fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = pyq.stepByStepSolution,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }
}
