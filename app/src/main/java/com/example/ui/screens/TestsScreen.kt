package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.outlined.*
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
import com.example.data.local.GeneratedTestEntity
import com.example.ui.RudraViewModel
import com.example.ui.components.BoardExamPaperViewerModal
import com.example.ui.components.InteractiveQuizModal
import com.example.ui.components.RudraTopAppBar
import com.example.ui.components.SimpleMarkdownCard
import com.example.ui.navigation.NavItem
import com.example.ui.theme.*

@Composable
fun TestsScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit,
    onNavigate: (NavItem) -> Unit
) {
    val tests by viewModel.generatedTests.collectAsStateWithLifecycle()
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()

    // Examination Active States
    val activeQuiz by viewModel.activeQuizState.collectAsStateWithLifecycle()
    val activePaper by viewModel.activePaperState.collectAsStateWithLifecycle()
    val lastAttempt by viewModel.lastCompletedAttempt.collectAsStateWithLifecycle()
    val lastBundle by viewModel.lastGeneratedBundle.collectAsStateWithLifecycle()

    var selectedSubjectFilter by remember { mutableStateOf("ALL") }

    val filteredTests = remember(tests, selectedSubjectFilter) {
        if (selectedSubjectFilter == "ALL") tests
        else tests.filter { it.subject.equals(selectedSubjectFilter, ignoreCase = true) }
    }

    val completedCount = tests.count { it.isCompleted }
    val totalTestsCount = tests.size

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "Tests & Mock Papers",
                onMenuClick = onOpenDrawer,
                currentStreak = streak,
                isLowEnergy = isLowEnergy,
                actions = {
                    IconButton(
                        onClick = { onNavigate(NavItem.AI_TEST_GEN) },
                        modifier = Modifier.testTag("generate_test_action_button")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Generate Test", tint = RudraAmber)
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            // Action Banner
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "BOARD EXAMINATION INTELLIGENCE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber, letterSpacing = 0.8.sp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Targeted papers strictly aligned to BSEB / CBSE weightages, 5-year PYQ frequencies, and model blueprints.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onNavigate(NavItem.AI_TEST_GEN) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth().testTag("launch_test_generator_button")
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Realistic Board Exam Paper", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Summary Stats Pill
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalTestsCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                            Text(text = "Total Papers", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp))
                        }
                        Divider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$completedCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RudraEmerald))
                            Text(text = "Completed", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp))
                        }
                        Divider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${totalTestsCount - completedCount}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RudraAmber))
                            Text(text = "Pending", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp))
                        }
                    }
                }
            }

            // Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val subjects = listOf("ALL", "Physics", "Chemistry", "Biology", "Mathematics", "Hindi", "English")
                    items(subjects) { sub ->
                        FilterChip(
                            selected = selectedSubjectFilter == sub,
                            onClick = { selectedSubjectFilter = sub },
                            label = { Text(sub, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }
            }

            // Saved Tests List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GENERATED BOARD EXAMINATIONS (${filteredTests.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp)
                    )
                }
            }

            if (filteredTests.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedSubjectFilter == "ALL") "No examination papers generated yet." else "No tests found for $selectedSubjectFilter.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { onNavigate(NavItem.AI_TEST_GEN) }) {
                                Text("Generate a Test with AI Exam Engine", color = RudraAmber, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredTests, key = { it.id }) { test ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_item_${test.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (test.isCompleted) RudraEmerald.copy(alpha = 0.2f) else RudraAmber.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (test.isCompleted) "COMPLETED" else "READY",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (test.isCompleted) RudraEmerald else RudraAmber,
                                                fontSize = 9.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${test.board} • ${test.subject}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${test.totalMarks} Marks",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = test.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${test.testType.replace("_", " ")} • ${test.difficulty} • ${test.timeMinutes} Mins",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.openSavedTestAsExamPaper(test, initialTab = 0) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("View Paper", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.openSavedTestAsExamPaper(test, initialTab = 2) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Solutions", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.openSavedTestAsExamPaper(test, initialTab = 3) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Analysis", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Interactive Timed Quiz Mode
    activeQuiz?.let { quizState ->
        InteractiveQuizModal(
            state = quizState,
            lastAttempt = lastAttempt,
            onSelectAnswer = { idx, ans -> viewModel.selectQuizAnswer(idx, ans) },
            onJumpToQuestion = { idx -> viewModel.jumpToQuizQuestion(idx) },
            onToggleBookmark = { idx -> viewModel.toggleQuizBookmark(idx) },
            onUpdateTimeRemaining = { secs -> viewModel.updateQuizTimeRemaining(secs) },
            onSubmit = { viewModel.submitInteractiveQuiz() },
            onRetryIncorrect = { viewModel.retryIncorrectQuizQuestions() },
            onViewSolutionsInPaper = {
                viewModel.closeInteractiveQuiz()
                lastBundle?.let { viewModel.openExamPaperView(it, initialTab = 2) }
            },
            onClose = { viewModel.closeInteractiveQuiz() }
        )
    }

    // Modal: Realistic Board PDF Exam Paper Viewer
    activePaper?.let { paperState ->
        BoardExamPaperViewerModal(
            state = paperState,
            onTabSelected = { tab -> viewModel.setPaperActiveTab(tab) },
            onZoomChanged = { delta -> viewModel.updatePaperZoom(delta) },
            onSearchChanged = { query -> viewModel.updatePaperSearchQuery(query) },
            onLaunchAsQuiz = {
                viewModel.closeExamPaperView()
                lastBundle?.let { viewModel.startInteractiveQuiz(it) }
            },
            onClose = { viewModel.closeExamPaperView() }
        )
    }
}
