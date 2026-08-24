package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.TestAttemptEntity
import com.example.data.testengine.ExamAnalysisReport
import com.example.data.testengine.TestItemData
import com.example.ui.ExamPaperViewState
import com.example.ui.InteractiveQuizState
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun InteractiveQuizModal(
    state: InteractiveQuizState,
    lastAttempt: TestAttemptEntity?,
    onSelectAnswer: (Int, String) -> Unit,
    onJumpToQuestion: (Int) -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onUpdateTimeRemaining: (Int) -> Unit,
    onSubmit: () -> Unit,
    onRetryIncorrect: () -> Unit,
    onViewSolutionsInPaper: () -> Unit,
    onClose: () -> Unit
) {
    // Timer Effect
    LaunchedEffect(state.isSubmitted, state.timeRemainingSeconds) {
        if (!state.isSubmitted && state.timeRemainingSeconds > 0) {
            delay(1000L)
            onUpdateTimeRemaining(state.timeRemainingSeconds - 1)
            if (state.timeRemainingSeconds - 1 <= 0) {
                onSubmit()
            }
        }
    }

    var showPalette by remember { mutableStateOf(false) }
    var showSubmitConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            if (state.isSubmitted) onClose() else showSubmitConfirm = true
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = state.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${state.board} • ${state.subject} • ${state.totalMarks} Marks • ${state.items.size} Questions",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }

                            // Timer Badge
                            if (!state.isSubmitted) {
                                val mins = state.timeRemainingSeconds / 60
                                val secs = state.timeRemainingSeconds % 60
                                val isCritical = mins < 3
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCritical) RudraRose.copy(alpha = 0.2f) else RudraAmber.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isCritical) RudraRose else RudraAmber
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = "Timer",
                                            tint = if (isCritical) RudraRose else RudraAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "%02d:%02d".format(mins, secs),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (isCritical) RudraRose else RudraAmber
                                            )
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = {
                                if (state.isSubmitted) onClose() else showSubmitConfirm = true
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        // Progress Bar
                        if (!state.isSubmitted && state.items.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val answeredCount = state.userAnswers.count { it.value.isNotBlank() }
                            val progress = answeredCount.toFloat() / state.items.size.toFloat()
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = RudraEmerald,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            },
            bottomBar = {
                if (!state.isSubmitted && state.items.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous
                            OutlinedButton(
                                onClick = {
                                    if (state.currentQuestionIndex > 0) {
                                        onJumpToQuestion(state.currentQuestionIndex - 1)
                                    }
                                },
                                enabled = state.currentQuestionIndex > 0,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("quiz_prev_button")
                            ) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Prev", fontSize = 12.sp)
                            }

                            // Palette Toggle
                            IconButton(onClick = { showPalette = !showPalette }) {
                                Icon(
                                    imageVector = Icons.Default.GridOn,
                                    contentDescription = "Question Palette",
                                    tint = if (showPalette) RudraAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Bookmark Toggle
                            val isBookmarked = state.currentQuestionIndex in state.bookmarkedQuestionIndices
                            IconButton(onClick = { onToggleBookmark(state.currentQuestionIndex) }) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) RudraAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Next / Submit
                            if (state.currentQuestionIndex < state.items.size - 1) {
                                Button(
                                    onClick = { onJumpToQuestion(state.currentQuestionIndex + 1) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("quiz_next_button")
                                ) {
                                    Text("Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            } else {
                                Button(
                                    onClick = { showSubmitConfirm = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RudraEmerald, contentColor = Color.Black),
                                    modifier = Modifier.testTag("quiz_submit_button")
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Submit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (state.isSubmitted) {
                    // Scorecard & Post-Exam Evaluation View
                    QuizEvaluationView(
                        state = state,
                        attempt = lastAttempt,
                        onRetryIncorrect = onRetryIncorrect,
                        onViewSolutionsInPaper = onViewSolutionsInPaper,
                        onClose = onClose
                    )
                } else if (state.items.isNotEmpty()) {
                    val currentQ = state.items.getOrNull(state.currentQuestionIndex) ?: state.items.first()
                    val userAns = state.userAnswers[state.currentQuestionIndex] ?: ""

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Palette Sheet overlay if open
                        if (showPalette) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("QUESTION PALETTE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber))
                                        IconButton(onClick = { showPalette = false }, modifier = Modifier.size(24.dp)) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyVerticalGrid(
                                        columns = GridCells.Adaptive(minSize = 36.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.heightIn(max = 140.dp)
                                    ) {
                                        itemsIndexed(state.items) { idx, _ ->
                                            val isCurrent = idx == state.currentQuestionIndex
                                            val isAns = state.userAnswers[idx]?.isNotBlank() == true
                                            val isBkmk = idx in state.bookmarkedQuestionIndices

                                            val bgColor = when {
                                                isCurrent -> MaterialTheme.colorScheme.primary
                                                isBkmk -> RudraAmber
                                                isAns -> RudraEmerald
                                                else -> MaterialTheme.colorScheme.surface
                                            }
                                            val textColor = when {
                                                isCurrent || isAns || isBkmk -> Color.Black
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }

                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(bgColor)
                                                    .clickable {
                                                        onJumpToQuestion(idx)
                                                        showPalette = false
                                                    }
                                            ) {
                                                Text(
                                                    text = "${idx + 1}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Question Card
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                // Question Header & Meta
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = currentQ.section,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = RudraAmber.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "${currentQ.marks} Marks",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                ) {
                                                    Text(
                                                        text = currentQ.sourceType,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Question Number & Text
                                        Text(
                                            text = "Q${currentQ.questionNumber}. ${currentQ.questionText}",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                lineHeight = 22.sp
                                            )
                                        )

                                        if (currentQ.topicName.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Topic: ${currentQ.topicName} • ${currentQ.chapterName}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Options / Input area
                            if (currentQ.questionType == "MCQ" && currentQ.options.isNotEmpty()) {
                                items(currentQ.options) { option ->
                                    val isSelected = userAns == option || userAns.startsWith(option.take(3))
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectAnswer(state.currentQuestionIndex, option) }
                                            .testTag("quiz_option_${option.take(2)}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(14.dp)
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSelectAnswer(state.currentQuestionIndex, option) }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = option,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Subjective response input
                                item {
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = "Subjective Answer / Key Equations & Numerical Result:",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            OutlinedTextField(
                                                value = userAns,
                                                onValueChange = { onSelectAnswer(state.currentQuestionIndex, it) },
                                                placeholder = { Text("Write your key steps, final numerical value with SI unit, or derivation summary here...") },
                                                minLines = 4,
                                                maxLines = 8,
                                                modifier = Modifier.fillMaxWidth().testTag("subjective_answer_input")
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "💡 In Board exams, write step formulas explicitly to earn partial marks even if numerical calculation is imperfect.",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitConfirm) {
        val answeredCount = state.userAnswers.count { it.value.isNotBlank() }
        val unattempted = state.items.size - answeredCount

        AlertDialog(
            onDismissRequest = { showSubmitConfirm = false },
            title = { Text("Submit Examination Paper?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("You have answered $answeredCount of ${state.items.size} questions.")
                    if (unattempted > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ $unattempted questions are currently unanswered.",
                            color = RudraAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirm = false
                        onSubmit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RudraEmerald, contentColor = Color.Black)
                ) {
                    Text("Yes, Submit & Evaluate", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirm = false }) {
                    Text("Continue Test")
                }
            }
        )
    }
}

@Composable
fun QuizEvaluationView(
    state: InteractiveQuizState,
    attempt: TestAttemptEntity?,
    onRetryIncorrect: () -> Unit,
    onViewSolutionsInPaper: () -> Unit,
    onClose: () -> Unit
) {
    val score = attempt?.scoredMarks ?: 0
    val totalMarks = state.totalMarks
    val accuracy = attempt?.accuracyPercentage ?: 0f
    val correct = attempt?.correctCount ?: 0
    val incorrect = attempt?.incorrectCount ?: 0
    val skipped = attempt?.skippedCount ?: 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Score Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (accuracy >= 60f) RudraEmerald.copy(alpha = 0.2f) else RudraRose.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (accuracy >= 80f) "🏆 BOARD DISTINCTION STANDARD" else if (accuracy >= 50f) "✅ PASS WITH REVISION REQUIRED" else "⚠️ CRITICAL GAPS IDENTIFIED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (accuracy >= 60f) RudraEmerald else RudraRose
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$score / $totalMarks",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Total Marks Scored (${"%.1f".format(accuracy)}% Accuracy)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Breakdown chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatPill("Correct", "$correct", RudraEmerald)
                        StatPill("Incorrect", "$incorrect", RudraRose)
                        StatPill("Skipped", "$skipped", MaterialTheme.colorScheme.onSurfaceVariant)
                        StatPill("Time", "${attempt?.timeTakenSeconds?.div(60) ?: 0}m", RudraAmber)
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (incorrect > 0 || skipped > 0) {
                    Button(
                        onClick = onRetryIncorrect,
                        colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("retry_incorrect_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry Missed (${incorrect + skipped})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onViewSolutionsInPaper,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).testTag("view_paper_solutions_button")
                ) {
                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Full Solutions", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Examiner Recommendations & Weak Topics Analysis
        attempt?.let { att ->
            if (att.improvementSuggestions.isNotBlank()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = RudraAmber, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("EXAMINER ANALYSIS & RECOMMENDATIONS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = att.improvementSuggestions, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "📅 Spaced Repetition Schedule: ${att.revisionRecommendations}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
        }

        // Question-by-Question Detailed Review
        item {
            Text(
                text = "DETAILED QUESTION REVIEW",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
            )
        }

        itemsIndexed(state.items) { idx: Int, q: TestItemData ->
            val userAns = state.userAnswers[idx] ?: ""
            val isCorrect = if (q.questionType == "MCQ") {
                userAns.isNotBlank() && (userAns.equals(q.correctAnswer, ignoreCase = true) || q.correctAnswer.startsWith(userAns.take(3), ignoreCase = true))
            } else {
                userAns.length > 5
            }

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCorrect) RudraEmerald.copy(alpha = 0.4f) else RudraRose.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Q${idx + 1}. [${q.questionType} - ${q.marks}M]",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isCorrect) RudraEmerald.copy(alpha = 0.2f) else RudraRose.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (userAns.isBlank()) "SKIPPED" else if (isCorrect) "CORRECT (+${q.marks}M)" else "INCORRECT (0M)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) RudraEmerald else RudraRose
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = q.questionText, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))

                    Spacer(modifier = Modifier.height(8.dp))
                    if (userAns.isNotBlank()) {
                        Text(
                            text = "Your Answer: $userAns",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isCorrect) RudraEmerald else RudraRose,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = "Official Answer: ${q.correctAnswer}",
                        style = MaterialTheme.typography.bodySmall.copy(color = RudraEmerald, fontWeight = FontWeight.Bold)
                    )

                    if (q.stepByStepSolution.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Step-by-Step Marking & Proof:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = q.stepByStepSolution, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = color))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardExamPaperViewerModal(
    state: ExamPaperViewState,
    onTabSelected: (Int) -> Unit,
    onZoomChanged: (Float) -> Unit,
    onSearchChanged: (String) -> Unit,
    onLaunchAsQuiz: () -> Unit,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = state.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Class 12 ${state.board} • ${state.subject} • ${state.totalMarks} Marks • ${state.timeMinutes} Mins",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onZoomChanged(-0.1f) }) {
                                    Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
                                }
                                Text("${(state.zoomLevel * 100).toInt()}%", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                IconButton(onClick = { onZoomChanged(0.1f) }) {
                                    Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = onClose) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                                }
                            }
                        }

                        // Search Bar
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = onSearchChanged,
                            placeholder = { Text("Search question paper, formulas, or answer key...", fontSize = 11.sp) },
                            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            trailingIcon = {
                                if (state.searchQuery.isNotBlank()) {
                                    IconButton(onClick = { onSearchChanged("") }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                .height(44.dp)
                        )

                        // 4 Primary Tabs
                        val tabs = listOf("📝 Question Paper", "🔑 Answer Key", "📐 Marking Scheme", "📊 Pre-Analysis")
                        PrimaryTabRow(
                            selectedTabIndex = state.activeTab,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tabs.forEachIndexed { idx, title ->
                                Tab(
                                    selected = state.activeTab == idx,
                                    onClick = { onTabSelected(idx) },
                                    text = { Text(title, fontSize = 11.sp, fontWeight = if (state.activeTab == idx) FontWeight.Bold else FontWeight.Normal) }
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onLaunchAsQuiz,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth().testTag("launch_as_quiz_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Take as Timed Interactive Examination", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val contentToShow = when (state.activeTab) {
                    0 -> state.questionPaperMarkdown
                    1 -> state.answerKeyMarkdown
                    2 -> state.solutionMarkdown
                    else -> ""
                }

                if (state.activeTab == 3 && state.analysisReport != null) {
                    PreExamAnalysisViewer(report = state.analysisReport)
                } else {
                    // Render Markdown content with zoom and search highlight
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            SimpleMarkdownCard(
                                markdownText = filterContentByQuery(contentToShow, state.searchQuery),
                                modifier = Modifier.testTag("exam_paper_content_card")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreExamAnalysisViewer(report: ExamAnalysisReport) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = RudraAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRE-GENERATION BOARD INTELLIGENCE REPORT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber, letterSpacing = 0.8.sp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Every question in this paper was compiled through deep syllabus weightage analysis and 5-year board PYQ frequency trends.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // Data Mix Ratios
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("QUESTION SOURCE DISTRIBUTION", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatPill("PYQ Database", "${report.pyqMixPercent}%", RudraEmerald)
                        StatPill("Model Paper Patterns", "${report.modelPaperMixPercent}%", RudraAmber)
                        StatPill("Fresh Calibration", "${report.freshMixPercent}%", MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // High Probability Topics
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("HIGH-PROBABILITY TOPICS INCLUDED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber))
                    Spacer(modifier = Modifier.height(8.dp))
                    report.highProbabilityTopics.forEach { topic ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = RudraEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = topic, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // 5-Year PYQ Frequencies
        if (report.repeatedPyqTopics.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("5-YEAR BOARD PYQ FREQUENCY TRENDS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        Spacer(modifier = Modifier.height(8.dp))
                        report.repeatedPyqTopics.forEach { (topicName, occurrencesIn5Years) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = topicName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = RudraAmber.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${occurrencesIn5Years}x in Board",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun filterContentByQuery(text: String, query: String): String {
    if (query.isBlank()) return text
    val lines = text.lines()
    val matchingLines = lines.filter { it.contains(query, ignoreCase = true) }
    return if (matchingLines.isNotEmpty()) {
        "**[Search Filter: \"$query\" (${matchingLines.size} matches found)]**\n\n" + matchingLines.joinToString("\n\n")
    } else {
        "**No direct matches for \"$query\". Showing full document:**\n\n$text"
    }
}
