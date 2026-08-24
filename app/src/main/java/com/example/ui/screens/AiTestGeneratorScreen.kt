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
import com.example.data.testengine.ExamIntelligenceEngine
import com.example.ui.RudraViewModel
import com.example.ui.components.BoardExamPaperViewerModal
import com.example.ui.components.InteractiveQuizModal
import com.example.ui.components.PreExamAnalysisViewer
import com.example.ui.components.RudraTopAppBar
import com.example.ui.components.SimpleMarkdownCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTestGeneratorScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit
) {
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()
    val testResponse by viewModel.aiTestGenResponse.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAiTestGenLoading.collectAsStateWithLifecycle()
    val defaultBoard by viewModel.selectedBoard.collectAsStateWithLifecycle()
    val allQuestions by viewModel.allQuestions.collectAsStateWithLifecycle()
    val allPatterns by viewModel.allPatterns.collectAsStateWithLifecycle()
    val weakTopicsList by viewModel.weakTopics.collectAsStateWithLifecycle()

    // Examination Active States
    val activeQuiz by viewModel.activeQuizState.collectAsStateWithLifecycle()
    val activePaper by viewModel.activePaperState.collectAsStateWithLifecycle()
    val lastAttempt by viewModel.lastCompletedAttempt.collectAsStateWithLifecycle()
    val preAnalysisReport by viewModel.activePreAnalysisReport.collectAsStateWithLifecycle()
    val lastBundle by viewModel.lastGeneratedBundle.collectAsStateWithLifecycle()

    var board by remember { mutableStateOf(defaultBoard) }
    var subject by remember { mutableStateOf("Physics") }
    var chapter by remember { mutableStateOf("Electrostatics & Capacitance") }
    var testMode by remember { mutableStateOf("MOCK_EXAM") }
    var difficulty by remember { mutableStateOf("Medium") }
    var questionCount by remember { mutableIntStateOf(15) }
    var timeLimitMinutes by remember { mutableIntStateOf(45) }
    var outputFormat by remember { mutableStateOf("QUIZ_MODE") } // "QUIZ_MODE", "PDF_EXAM_MODE"
    var showLivePreAnalysis by remember { mutableStateOf(false) }

    val availableSubjectQuestionsCount = allQuestions.count { it.subject.equals(subject, ignoreCase = true) }
    val availableSubjectPatternsCount = allPatterns.count { it.subject.equals(subject, ignoreCase = true) }

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "AI Examination Engine",
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
            // Header Info
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().testTag("ai_test_gen_header")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = RudraAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EXAMINATION INTELLIGENCE ENGINE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber, letterSpacing = 0.8.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Synthesizes authentic BSEB / CBSE Class 12 board papers using official syllabus weightages, 5-year PYQ frequencies, model paper patterns, and your Resource Vault.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // Configuration Form
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Output Format Selector
                        Text("Examination Mode / Output Format:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = outputFormat == "QUIZ_MODE",
                                onClick = { outputFormat = "QUIZ_MODE" },
                                label = { Text("🎮 Interactive Timed Exam", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RudraAmber, selectedLabelColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = outputFormat == "PDF_EXAM_MODE",
                                onClick = { outputFormat = "PDF_EXAM_MODE" },
                                label = { Text("📄 Board PDF Paper View", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RudraAmber, selectedLabelColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Test Mode
                        Text("Test Architecture:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        val modes = listOf(
                            "RAPID_QUIZ" to "⚡ Rapid 10-Q Drill",
                            "CHAPTER_TEST" to "📑 Chapter Test",
                            "HIGH_PROBABILITY" to "🎯 High-Yield PYQs",
                            "WEAK_TOPICS" to "🏥 Weak Topics Rescue",
                            "MOCK_EXAM" to "🏆 Full Model Paper (70M)",
                            "REVISION_QUIZ" to "🔄 Spaced Revision"
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(modes) { (modeKey, modeTitle) ->
                                FilterChip(
                                    selected = testMode == modeKey,
                                    onClick = {
                                        testMode = modeKey
                                        if (modeKey == "RAPID_QUIZ") {
                                            questionCount = 10
                                            timeLimitMinutes = 15
                                        } else if (modeKey == "MOCK_EXAM") {
                                            questionCount = 20
                                            timeLimitMinutes = 70
                                        }
                                    },
                                    label = { Text(modeTitle, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer),
                                    shape = RoundedCornerShape(6.dp)
                                )
                            }
                        }

                        // Subject Selection
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Subject:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "$availableSubjectQuestionsCount Qs • $availableSubjectPatternsCount Patterns Grounded",
                                    fontSize = 10.sp,
                                    color = RudraEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(listOf("Physics", "Chemistry", "Biology", "Mathematics", "Hindi", "English")) { sub ->
                                    FilterChip(
                                        selected = subject == sub,
                                        onClick = {
                                            subject = sub
                                            chapter = when (sub) {
                                                "Physics" -> "Electrostatics & Current Electricity"
                                                "Chemistry" -> "Solutions & Electrochemistry"
                                                "Biology" -> "Genetics & Reproduction"
                                                "Mathematics" -> "Calculus & Integrals"
                                                else -> "Full Syllabus"
                                            }
                                        },
                                        label = { Text(sub, fontSize = 10.sp) },
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                }
                            }
                        }

                        // Board & Difficulty
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            // Board
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Board Target:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("BSEB", "CBSE").forEach { b ->
                                        FilterChip(
                                            selected = board == b,
                                            onClick = { board = b },
                                            label = { Text(b, fontSize = 10.sp) },
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                    }
                                }
                            }

                            // Difficulty
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Standard:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Medium", "Advanced").forEach { diff ->
                                        FilterChip(
                                            selected = difficulty == diff,
                                            onClick = { difficulty = diff },
                                            label = { Text(diff, fontSize = 10.sp) },
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Chapter / Scope Input
                        OutlinedTextField(
                            value = chapter,
                            onValueChange = { chapter = it },
                            label = { Text("Chapter / Focus Topics (leave empty for Full Board Paper)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("test_gen_chapter_input")
                        )

                        // Question Count & Time Limits
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Questions: $questionCount Qs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = questionCount.toFloat(),
                                    onValueChange = { questionCount = it.toInt() },
                                    valueRange = 5f..35f,
                                    steps = 5
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Time Limit: $timeLimitMinutes mins", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = timeLimitMinutes.toFloat(),
                                    onValueChange = { timeLimitMinutes = it.toInt() },
                                    valueRange = 10f..180f,
                                    steps = 16
                                )
                            }
                        }

                        // Pre-Generation Live Analysis Toggle
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLivePreAnalysis = !showLivePreAnalysis }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = RudraAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Preview Board Syllabus & PYQ Intelligence Analysis", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Icon(
                                    imageVector = if (showLivePreAnalysis) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }
                        }

                        if (showLivePreAnalysis) {
                            val liveReport = remember(board, subject, testMode, chapter, difficulty, questionCount) {
                                ExamIntelligenceEngine.performPreGenerationAnalysis(
                                    board = board,
                                    subject = subject,
                                    testMode = testMode,
                                    targetChapterOrUnit = chapter,
                                    difficulty = difficulty,
                                    questionCount = questionCount,
                                    userWeakTopics = weakTopicsList,
                                    vaultQuestions = allQuestions,
                                    vaultPatterns = allPatterns
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Pre-Calculated Blueprint for $subject:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RudraAmber)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("• Target Marks: ${liveReport.totalMarks} Marks | Time: ${liveReport.timeMinutes} Minutes", fontSize = 10.sp)
                                    Text("• Composition: ${liveReport.pyqMixPercent}% PYQ Data • ${liveReport.modelPaperMixPercent}% Model Patterns • ${liveReport.freshMixPercent}% Fresh", fontSize = 10.sp)
                                    Text("• High Yield: ${liveReport.highProbabilityTopics.take(3).joinToString(", ")}", fontSize = 10.sp, color = RudraEmerald)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Generate Button
                        Button(
                            onClick = {
                                viewModel.generateRealisticExamination(
                                    board = board,
                                    subject = subject,
                                    chapter = chapter,
                                    testMode = testMode,
                                    difficulty = difficulty,
                                    questionCount = questionCount,
                                    timeLimitMinutes = timeLimitMinutes,
                                    outputFormat = outputFormat
                                )
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth().testTag("generate_test_paper_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing Syllabus & Compiling Paper...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (outputFormat == "QUIZ_MODE") "Compile & Start Interactive Exam" else "Compile Realistic Board PDF Paper",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Last Generated Exam Summary Banner if available
            lastBundle?.let { bundle ->
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "LAST COMPILED BOARD PAPER",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraEmerald)
                                    )
                                    Text(
                                        text = bundle.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${bundle.totalMarks} Marks • ${bundle.timeMinutes} Mins • ${bundle.items.size} Questions",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.startInteractiveQuiz(bundle) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Take Quiz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.openExamPaperView(bundle) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("View PDF Paper", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Markdown Card if AI enriched response exists
            testResponse?.let { response ->
                item {
                    Text(
                        text = "ENRICHED CHIEF EXAMINER PAPER & SOLUTIONS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraEmerald, letterSpacing = 1.sp)
                    )
                }

                item {
                    SimpleMarkdownCard(markdownText = response, modifier = Modifier.testTag("generated_test_card"))
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
