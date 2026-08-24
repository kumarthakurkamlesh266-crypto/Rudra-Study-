package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import com.example.data.local.PatternEntity
import com.example.data.local.QuestionEntity
import com.example.data.local.VaultDocumentEntity
import com.example.data.vault.PdfVaultManager
import com.example.service.PdfQuestionExtractionWorker
import com.example.ui.RudraViewModel
import com.example.ui.components.RudraTopAppBar
import com.example.ui.components.SimpleMarkdownCard
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfLibraryScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()

    val vaultDocs by viewModel.vaultDocuments.collectAsStateWithLifecycle()
    val vaultBytes by viewModel.vaultStorageBytes.collectAsStateWithLifecycle()
    val vaultDocsCount by viewModel.vaultDocsCount.collectAsStateWithLifecycle()
    val allQuestions by viewModel.allQuestions.collectAsStateWithLifecycle()
    val totalQuestionsCount by viewModel.totalQuestionsCount.collectAsStateWithLifecycle()
    val allPatterns by viewModel.allPatterns.collectAsStateWithLifecycle()
    val totalPatternsCount by viewModel.totalPatternsCount.collectAsStateWithLifecycle()

    val isUploading by viewModel.isVaultUploading.collectAsStateWithLifecycle()
    val uploadStatus by viewModel.vaultUploadStatus.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isVaultAnalyzing.collectAsStateWithLifecycle()
    val analysisStatus by viewModel.vaultAnalysisStatus.collectAsStateWithLifecycle()

    // WorkManager Extraction Observables
    val extractionWorkInfo by viewModel.pdfExtractionWorkInfo.collectAsStateWithLifecycle()
    val isWorkerRunning = extractionWorkInfo?.state == WorkInfo.State.RUNNING
    val isWorkerEnqueued = extractionWorkInfo?.state == WorkInfo.State.ENQUEUED
    val workerProgress = extractionWorkInfo?.progress?.getInt(PdfQuestionExtractionWorker.KEY_PROGRESS_PERCENT, 0) ?: 0
    val workerCurrentDoc = extractionWorkInfo?.progress?.getString(PdfQuestionExtractionWorker.KEY_CURRENT_DOC_TITLE)
    val workerExtractedCount = extractionWorkInfo?.progress?.getInt(PdfQuestionExtractionWorker.KEY_TOTAL_QUESTIONS_EXTRACTED, 0)
        ?: extractionWorkInfo?.outputData?.getInt(PdfQuestionExtractionWorker.KEY_TOTAL_QUESTIONS_EXTRACTED, 0) ?: 0
    val workerStatusMsg = extractionWorkInfo?.progress?.getString(PdfQuestionExtractionWorker.KEY_STATUS_MESSAGE)
        ?: extractionWorkInfo?.outputData?.getString(PdfQuestionExtractionWorker.KEY_STATUS_MESSAGE)

    var selectedMainTab by remember { mutableStateOf(0) } // 0: Resource Vault, 1: Question Database, 2: Pattern Database
    var activeCategory by remember { mutableStateOf("ALL") }
    var activeSubjectFilter by remember { mutableStateOf("ALL") }
    var activeDifficultyFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    var viewingDoc by remember { mutableStateOf<VaultDocumentEntity?>(null) }
    var viewingQuestionDetail by remember { mutableStateOf<QuestionEntity?>(null) }

    // Upload Dialog State
    var showUploadDialog by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var pendingFileName by remember { mutableStateOf("") }
    var pendingTitle by remember { mutableStateOf("") }
    var pendingSubject by remember { mutableStateOf("Physics") }
    var pendingCategory by remember { mutableStateOf("QUESTION_BANK") }
    var pendingBoard by remember { mutableStateOf("BSEB") }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingUri = uri
            val resolvedName = queryFileName(context, uri)
            pendingFileName = resolvedName
            pendingTitle = resolvedName.removeSuffix(".pdf").replace("_", " ")
            showUploadDialog = true
        }
    }

    val maxVaultBytes = 1024L * 1024L * 1024L // 1 GB
    val usedBytesSafe = vaultBytes ?: 0L
    val storagePercent = ((usedBytesSafe.toFloat() / maxVaultBytes.toFloat()) * 100f).coerceIn(0.1f, 100f)

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "Resource Vault & PDF Hub",
                onMenuClick = onOpenDrawer,
                currentStreak = streak,
                isLowEnergy = isLowEnergy,
                actions = {
                    IconButton(
                        onClick = { filePickerLauncher.launch("application/pdf") },
                        modifier = Modifier.testTag("upload_pdf_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload PDF",
                            tint = RudraAmber
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedMainTab == 0) {
                FloatingActionButton(
                    onClick = { filePickerLauncher.launch("application/pdf") },
                    containerColor = RudraAmber,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("upload_pdf_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload PDF", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            // Storage & Metrics Header
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = RudraAmber, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("RESOURCE VAULT STORAGE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
                            }
                            Text(
                                text = "${PdfVaultManager.formatFileSize(usedBytesSafe)} / 1.00 GB",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = RudraAmber)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { storagePercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = RudraAmber,
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Vault PDFs",
                                value = "$vaultDocsCount/100",
                                subtitle = "Capacity: 100+",
                                icon = Icons.Default.PictureAsPdf,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Questions",
                                value = "$totalQuestionsCount",
                                subtitle = "Extracted DB",
                                icon = Icons.Default.Quiz,
                                iconColor = RudraEmerald,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Patterns",
                                value = "$totalPatternsCount",
                                subtitle = "High-Yield",
                                icon = Icons.Default.Analytics,
                                iconColor = RudraCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // WorkManager Background Service Panel & Controls
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isWorkerRunning) RudraAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("workmanager_extraction_panel")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isWorkerRunning) RudraAmber.copy(alpha = 0.2f) else RudraEmerald.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isWorkerRunning) Icons.Default.Sync else Icons.Default.Engineering,
                                        contentDescription = null,
                                        tint = if (isWorkerRunning) RudraAmber else RudraEmerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "BACKGROUND PDF EXTRACTION",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    )
                                    Text(
                                        text = "WorkManager Service • Gemini AI Structured Pipeline",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    isWorkerRunning -> RudraAmber.copy(alpha = 0.2f)
                                    isWorkerEnqueued -> RudraCyan.copy(alpha = 0.2f)
                                    extractionWorkInfo?.state == WorkInfo.State.SUCCEEDED -> RudraEmerald.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            ) {
                                Text(
                                    text = when {
                                        isWorkerRunning -> "RUNNING ($workerProgress%)"
                                        isWorkerEnqueued -> "QUEUED"
                                        extractionWorkInfo?.state == WorkInfo.State.SUCCEEDED -> "COMPLETED"
                                        extractionWorkInfo?.state == WorkInfo.State.FAILED -> "FAILED"
                                        else -> "IDLE"
                                    },
                                    color = when {
                                        isWorkerRunning -> RudraAmber
                                        isWorkerEnqueued -> RudraCyan
                                        extractionWorkInfo?.state == WorkInfo.State.SUCCEEDED -> RudraEmerald
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Progress indicator when active
                        if (isWorkerRunning) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { workerProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = RudraAmber,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = workerCurrentDoc ?: "Extracting question patterns...",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$workerExtractedCount Qs Added",
                                    style = MaterialTheme.typography.labelSmall.copy(color = RudraEmerald, fontWeight = FontWeight.Bold)
                                )
                            }
                        } else if (workerStatusMsg != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = workerStatusMsg,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.triggerBackgroundPdfExtraction(forceReanalyze = false)
                                    Toast.makeText(context, "WorkManager extraction service enqueued in background!", Toast.LENGTH_SHORT).show()
                                },
                                enabled = !isWorkerRunning,
                                colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("trigger_background_extraction_btn")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Extract Pending", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.triggerBackgroundPdfExtraction(forceReanalyze = true)
                                    Toast.makeText(context, "Force re-extraction worker started for all stored PDFs!", Toast.LENGTH_SHORT).show()
                                },
                                enabled = !isWorkerRunning,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("force_extract_all_btn")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Re-Extract All", fontSize = 11.sp)
                            }

                            if (isWorkerRunning) {
                                IconButton(
                                    onClick = {
                                        viewModel.cancelBackgroundPdfExtraction()
                                        Toast.makeText(context, "Cancelled background extraction worker", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = "Cancel Worker", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            // Ingestion / Analysis Status Toast Banner
            if (isUploading || isAnalyzing || uploadStatus != null || analysisStatus != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isUploading || isAnalyzing) RudraAmber.copy(alpha = 0.15f) else RudraEmerald.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isUploading || isAnalyzing) RudraAmber else RudraEmerald),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isUploading || isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = RudraAmber)
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RudraEmerald, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uploadStatus ?: analysisStatus ?: "Processing...",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Primary Navigation Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedMainTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedMainTab == 0,
                        onClick = { selectedMainTab = 0 },
                        text = { Text("Vault PDFs ($vaultDocsCount)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedMainTab == 1,
                        onClick = { selectedMainTab = 1 },
                        text = { Text("Question Bank ($totalQuestionsCount)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedMainTab == 2,
                        onClick = { selectedMainTab = 2 },
                        text = { Text("Pattern DB ($totalPatternsCount)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Pattern, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            // --- TAB 0: RESOURCE VAULT (PDFs) ---
            if (selectedMainTab == 0) {
                // Category Filter Chips
                item {
                    val categories = listOf(
                        "ALL" to "All PDFs",
                        "PYQ_PAPER" to "PYQ Papers",
                        "QUESTION_BANK" to "Question Banks",
                        "CHAPTER_NOTES" to "Chapter Notes",
                        "FORMULA_SHEET" to "Formula Sheets"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { (code, label) ->
                            FilterChip(
                                selected = activeCategory == code,
                                onClick = { activeCategory = code },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RudraAmber, selectedLabelColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search PDF titles or subjects...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("vault_search_input")
                    )
                }

                val filteredDocs = vaultDocs.filter { doc ->
                    (activeCategory == "ALL" || doc.category == activeCategory) &&
                    (searchQuery.isBlank() || doc.title.contains(searchQuery, ignoreCase = true) || doc.fileName.contains(searchQuery, ignoreCase = true) || doc.subject.contains(searchQuery, ignoreCase = true))
                }

                if (filteredDocs.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, tint = RudraAmber, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No PDFs in this category", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tap 'Upload PDF' to ingest your local notes or past papers.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { filePickerLauncher.launch("application/pdf") },
                                    colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black)
                                ) {
                                    Text("Select Local PDF")
                                }
                            }
                        }
                    }
                }

                items(filteredDocs, key = { it.id }) { doc ->
                    VaultDocumentCard(
                        doc = doc,
                        onOpen = {
                            viewingDoc = doc
                            viewModel.recordVaultDocOpened(doc.id)
                        },
                        onReanalyze = { viewModel.reanalyzeVaultDocument(doc) },
                        onQueueBackgroundExtraction = {
                            viewModel.triggerBackgroundPdfExtraction(doc.id, forceReanalyze = true)
                            Toast.makeText(context, "Queued '${doc.fileName}' for WorkManager background extraction!", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { viewModel.deleteVaultDocument(doc.id) },
                        onToggleBookmark = { viewModel.toggleVaultBookmark(doc.id, !doc.isBookmarked) }
                    )
                }
            }

            // --- TAB 1: QUESTION DATABASE ---
            if (selectedMainTab == 1) {
                // Filters Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val subjects = listOf("ALL", "Physics", "Chemistry", "Biology", "Mathematics")
                        Text("Subject Filter:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(subjects) { sub ->
                                FilterChip(
                                    selected = activeSubjectFilter == sub,
                                    onClick = { activeSubjectFilter = sub },
                                    label = { Text(sub) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RudraEmerald, selectedLabelColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        val difficulties = listOf("ALL", "Easy", "Medium", "Hard", "Advanced")
                        Text("Difficulty Filter:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(difficulties) { diff ->
                                FilterChip(
                                    selected = activeDifficultyFilter == diff,
                                    onClick = { activeDifficultyFilter = diff },
                                    label = { Text(diff) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RudraAmber, selectedLabelColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search question text, chapter, or concepts...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("questions_search_input")
                        )
                    }
                }

                val filteredQuestions = allQuestions.filter { q ->
                    (activeSubjectFilter == "ALL" || q.subject.equals(activeSubjectFilter, ignoreCase = true)) &&
                    (activeDifficultyFilter == "ALL" || q.difficulty.equals(activeDifficultyFilter, ignoreCase = true)) &&
                    (searchQuery.isBlank() || q.questionText.contains(searchQuery, ignoreCase = true) || q.chapterName.contains(searchQuery, ignoreCase = true) || q.topicName.contains(searchQuery, ignoreCase = true))
                }

                item {
                    Text(
                        text = "SHOWING ${filteredQuestions.size} EXTRACTED QUESTIONS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraEmerald)
                    )
                }

                items(filteredQuestions, key = { it.id }) { q ->
                    ExtractedQuestionCard(
                        question = q,
                        onToggleImportant = { viewModel.toggleQuestionImportant(q.id, !q.isImportant) },
                        onAskTutor = {
                            viewModel.askGroundedAiTutor(
                                topicOrQuestion = q.questionText,
                                subject = q.subject,
                                mode = "BILINGUAL",
                                type = "EXPLAIN"
                            )
                            Toast.makeText(context, "Sent question to AI Tutor for grounded solution!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // --- TAB 2: PATTERN DATABASE ---
            if (selectedMainTab == 2) {
                item {
                    val subjects = listOf("ALL", "Physics", "Chemistry", "Biology", "Mathematics")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(subjects) { sub ->
                            FilterChip(
                                selected = activeSubjectFilter == sub,
                                onClick = { activeSubjectFilter = sub },
                                label = { Text(sub) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RudraCyan, selectedLabelColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                val filteredPatterns = allPatterns.filter { p ->
                    activeSubjectFilter == "ALL" || p.subject.equals(activeSubjectFilter, ignoreCase = true)
                }

                item {
                    Text(
                        text = "RECURRING EXAM PATTERNS & TRAP DATABASE (${filteredPatterns.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraCyan)
                    )
                }

                items(filteredPatterns, key = { it.id }) { pattern ->
                    PatternCard(pattern = pattern)
                }
            }
        }
    }

    // --- UPLOAD & INGESTION DIALOG ---
    if (showUploadDialog && pendingUri != null) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Ingest PDF into Resource Vault", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("File: $pendingFileName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = pendingTitle,
                        onValueChange = { pendingTitle = it },
                        label = { Text("Document Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Subject:", style = MaterialTheme.typography.labelSmall)
                    val subjects = listOf("Physics", "Chemistry", "Biology", "Mathematics", "Hindi", "English")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(subjects) { sub ->
                            FilterChip(
                                selected = pendingSubject == sub,
                                onClick = { pendingSubject = sub },
                                label = { Text(sub, fontSize = 11.sp) },
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }

                    Text("Category:", style = MaterialTheme.typography.labelSmall)
                    val categories = listOf(
                        "QUESTION_BANK" to "Question Bank",
                        "PYQ_PAPER" to "PYQ Paper",
                        "CHAPTER_NOTES" to "Chapter Notes",
                        "FORMULA_SHEET" to "Formula Sheet"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { (code, lbl) ->
                            FilterChip(
                                selected = pendingCategory == code,
                                onClick = { pendingCategory = code },
                                label = { Text(lbl, fontSize = 11.sp) },
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }

                    Text("Target Board:", style = MaterialTheme.typography.labelSmall)
                    val boards = listOf("BSEB", "CBSE", "ALL")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        boards.forEach { b ->
                            FilterChip(
                                selected = pendingBoard == b,
                                onClick = { pendingBoard = b },
                                label = { Text(b, fontSize = 11.sp) },
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingUri
                        if (uri != null) {
                            viewModel.uploadPdfToVault(
                                uri = uri,
                                fileName = pendingFileName,
                                subject = pendingSubject,
                                category = pendingCategory,
                                board = pendingBoard
                            )
                        }
                        showUploadDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black)
                ) {
                    Text("Ingest & Analyze", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- VIEW DOCUMENT DETAIL & ANALYSIS DIALOG ---
    viewingDoc?.let { doc ->
        AlertDialog(
            onDismissRequest = { viewingDoc = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(doc.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.toggleVaultBookmark(doc.id, !doc.isBookmarked) }) {
                        Icon(if (doc.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = null, tint = RudraAmber)
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("📄 ${doc.fileName}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                Text("${doc.subject} • ${doc.board} • ${PdfVaultManager.formatFileSize(doc.fileSizeBytes)} • ${doc.pageCount} Pages", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    if (doc.isAnalyzed) {
                        item {
                            Text("🧠 AI EXTRACTION SUMMARY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber))
                            Text(doc.analyzedSummary, style = MaterialTheme.typography.bodyMedium)
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(shape = RoundedCornerShape(6.dp), color = RudraEmerald.copy(alpha = 0.2f)) {
                                    Text("✅ ${doc.questionsCount} Questions Extracted", modifier = Modifier.padding(6.dp), color = RudraEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = RudraCyan.copy(alpha = 0.2f)) {
                                    Text("🎯 ${doc.patternsCount} Patterns Identified", modifier = Modifier.padding(6.dp), color = RudraCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text("📝 EXTRACTED DOCUMENT TEXT PREVIEW", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        Text(doc.extractedText.take(1200) + if (doc.extractedText.length > 1200) "..." else "", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reanalyzeVaultDocument(doc)
                        viewingDoc = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RudraAmber, contentColor = Color.Black)
                ) {
                    Text("Re-Analyze with AI")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewingDoc = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun VaultDocumentCard(
    doc: VaultDocumentEntity,
    onOpen: () -> Unit,
    onReanalyze: () -> Unit,
    onQueueBackgroundExtraction: () -> Unit,
    onDelete: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("vault_doc_${doc.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(RudraAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (doc.category) {
                            "PYQ_PAPER" -> Icons.Default.Assignment
                            "FORMULA_SHEET" -> Icons.Default.Functions
                            else -> Icons.Default.PictureAsPdf
                        },
                        contentDescription = null,
                        tint = RudraAmber,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${doc.subject} • ${PdfVaultManager.formatFileSize(doc.fileSizeBytes)} • ${doc.pageCount} pgs",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    )
                }

                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = if (doc.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = RudraAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Analysis Badge & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (doc.isAnalyzed) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(4.dp), color = RudraEmerald.copy(alpha = 0.15f)) {
                            Text(
                                text = "ANALYZED (${doc.questionsCount} Qs • ${doc.patternsCount} Patterns)",
                                color = RudraEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else {
                    Surface(shape = RoundedCornerShape(4.dp), color = RudraAmber.copy(alpha = 0.15f)) {
                        Text(
                            text = "PENDING ANALYSIS",
                            color = RudraAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onQueueBackgroundExtraction,
                        modifier = Modifier.size(32.dp).testTag("bg_extract_btn_${doc.id}")
                    ) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = "Background Extract",
                            tint = RudraCyan,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(onClick = onReanalyze, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = "Re-analyze", tint = RudraAmber, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExtractedQuestionCard(
    question: QuestionEntity,
    onToggleImportant: () -> Unit,
    onAskTutor: () -> Unit
) {
    var expandedSolution by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().testTag("question_item_${question.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                        Text(
                            text = question.questionType,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = RudraAmber.copy(alpha = 0.15f)) {
                        Text(
                            text = "${question.marks}M",
                            color = RudraAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = if (question.difficulty == "Advanced") MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else RudraEmerald.copy(alpha = 0.15f)) {
                        Text(
                            text = question.difficulty.uppercase(),
                            color = if (question.difficulty == "Advanced") MaterialTheme.colorScheme.error else RudraEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onToggleImportant, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (question.isImportant) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Important",
                        tint = RudraAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${question.subject} • ${question.chapterName}",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )

            // MCQ Options if present
            val optionsList = remember(question.optionsJson) {
                parseOptionsList(question.optionsJson)
            }
            if (optionsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    optionsList.forEach { opt ->
                        val isCorrect = opt.equals(question.correctAnswer, ignoreCase = true) || question.correctAnswer.startsWith(opt.take(3))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCorrect && expandedSolution) RudraEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isCorrect && expandedSolution) androidx.compose.foundation.BorderStroke(1.dp, RudraEmerald) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isCorrect && expandedSolution) RudraEmerald else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isCorrect && expandedSolution) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Expandable Solution & Actions
            AnimatedVisibility(visible = expandedSolution) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("💡 MODEL SOLUTION & MARKING SCHEME", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraEmerald))
                    if (question.correctAnswer.isNotBlank()) {
                        Text("Answer: ${question.correctAnswer}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber), modifier = Modifier.padding(vertical = 2.dp))
                    }
                    Text(
                        text = question.stepByStepSolution,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { expandedSolution = !expandedSolution }) {
                    Text(if (expandedSolution) "Hide Solution" else "View Solution", fontSize = 12.sp)
                }

                Button(
                    onClick = onAskTutor,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Explain in AI Tutor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PatternCard(pattern: PatternEntity) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().testTag("pattern_item_${pattern.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(4.dp), color = RudraCyan.copy(alpha = 0.15f)) {
                    Text(
                        text = pattern.patternType.replace("_", " "),
                        color = RudraCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "Appeared ${pattern.frequency}x • ${pattern.weightagePercentage}% Wt.",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RudraAmber)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${pattern.subject} • ${pattern.chapterName}",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pattern.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pattern.description,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            if (pattern.examTip.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RudraAmber.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RudraAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = RudraAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = pattern.examTip,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, lineHeight = 16.sp)
                        )
                    }
                }
            }
        }
    }
}

fun queryFileName(context: Context, uri: Uri): String {
    var name = "Document_${System.currentTimeMillis()}.pdf"
    try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
    } catch (ignored: Exception) {}
    return name
}

fun parseOptionsList(optionsJson: String): List<String> {
    if (optionsJson.isBlank() || optionsJson == "[]") return emptyList()
    val list = mutableListOf<String>()
    try {
        val arr = JSONArray(optionsJson)
        for (i in 0 until arr.length()) {
            val item = arr.optString(i)
            if (item.isNotBlank()) {
                list.add(item)
            }
        }
    } catch (ignored: Exception) {}
    return list
}
