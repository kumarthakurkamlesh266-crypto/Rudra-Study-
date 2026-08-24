package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AiChatMessageEntity
import com.example.data.local.QuestionEntity
import com.example.ui.RudraViewModel
import com.example.ui.components.RudraTopAppBar
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class TeachingMode(
    val id: String,
    val label: String,
    val emoji: String,
    val description: String,
    val badgeColor: Color
) {
    EL10("EL10", "EL10 (Like I'm 10)", "🧒", "Simple language, real-life analogies, step-by-step intuition (Default)", RudraAmber),
    EL5("EL5", "EL5 (Like I'm 5)", "🧸", "Ultra-simple playground & toy metaphors, short & clear", RudraCyan),
    BEGINNER("BEGINNER", "Beginner", "🌱", "Foundational definitions, clear concepts & gentle pacing", RudraEmerald),
    INTERMEDIATE("INTERMEDIATE", "High School / NCERT", "📚", "Curriculum aligned, standard definitions & formal formulas", RudraPurple),
    ADVANCED("ADVANCED", "Advanced & Derivation", "🔬", "Deep mathematical proofs, chemical mechanisms & edge cases", RudraRose),
    BOARD_EXAM("BOARD_EXAM", "Board Exam 100%", "🎯", "Marking scheme structure, underline keywords & diagram boxes", RudraAmber)
}

data class SubjectItem(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val placeholder: String
)

val AI_TUTOR_SUBJECTS = listOf(
    SubjectItem("Physics", Icons.Default.ElectricBolt, RudraCyan, "e.g. Derive Gauss Law, or explain Lenz's Law with an analogy..."),
    SubjectItem("Chemistry", Icons.Default.Science, RudraEmerald, "e.g. Explain Aldol Condensation mechanism or Periodic Trends..."),
    SubjectItem("Biology", Icons.Default.Biotech, RudraAmber, "e.g. Draw flowchart of Double Fertilization or DNA Replication..."),
    SubjectItem("Mathematics", Icons.Default.Calculate, RudraPurple, "e.g. Explain integration by parts intuition or solve matrix problem..."),
    SubjectItem("English", Icons.Default.MenuBook, Color(0xFF60A5FA), "e.g. Summary of The Last Lesson, writing formal letters..."),
    SubjectItem("Hindi", Icons.Default.Translate, Color(0xFFF472B6), "e.g. बातचीत पाठ का सारांश, अलंकार एवं समास के नियम..."),
    SubjectItem("Computer Science", Icons.Default.Code, Color(0xFF34D399), "e.g. Explain Binary Search tree, recursion stack or SQL joins..."),
    SubjectItem("General Knowledge", Icons.Default.Public, Color(0xFFFBBF24), "e.g. Important Indian constitutional articles, Nobel prize facts..."),
    SubjectItem("Productivity", Icons.Default.Timer, Color(0xFFA78BFA), "e.g. How to maintain focus in 3-hour study blocks, beat phone addiction..."),
    SubjectItem("Personal Development", Icons.Default.SelfImprovement, Color(0xFF38BDF8), "e.g. Stoic mindset for exam stress, overcoming fear of failure...")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTutorScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()
    val allMessages by viewModel.aiChatMessages.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedAiSubject.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedAiTeachingMode.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedAiLanguage.collectAsStateWithLifecycle()
    val isGrounded by viewModel.isVaultGroundingEnabled.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAiTutorLoading.collectAsStateWithLifecycle()
    val allQuestions by viewModel.allQuestions.collectAsStateWithLifecycle()
    val totalQuestionsCount by viewModel.totalQuestionsCount.collectAsStateWithLifecycle()

    var queryText by remember { mutableStateOf("") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showVaultPickerSheet by remember { mutableStateOf(false) }
    var showModePickerMenu by remember { mutableStateOf(false) }
    var showLanguagePickerMenu by remember { mutableStateOf(false) }

    // TTS Engine State
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var currentlySpeakingMessageId by remember { mutableStateOf<Long?>(null) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale.ENGLISH
            }
        }
        ttsInstance = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val listState = rememberLazyListState()

    // Filter messages for active subject (or all if user chooses)
    val subjectMessages = remember(allMessages, selectedSubject) {
        allMessages.filter { it.subject.equals(selectedSubject, ignoreCase = true) }
    }

    // Auto scroll to bottom on new message
    LaunchedEffect(subjectMessages.size, isLoading) {
        if (subjectMessages.isNotEmpty()) {
            listState.animateScrollToItem(subjectMessages.size)
        }
    }

    val activeSubjectItem = remember(selectedSubject) {
        AI_TUTOR_SUBJECTS.find { it.name.equals(selectedSubject, ignoreCase = true) }
            ?: AI_TUTOR_SUBJECTS[0]
    }

    val activeModeEnum = remember(selectedMode) {
        TeachingMode.values().find { it.id == selectedMode } ?: TeachingMode.EL10
    }

    // Quick Prompts per Subject
    val quickActionChips = remember(selectedSubject) {
        when (selectedSubject) {
            "Physics" -> listOf(
                "🧒 Explain Gauss Law (EL10)",
                "📊 Flowchart: AC Generator working",
                "💡 Analogy for Electric Potential",
                "🔢 Step-by-step Numerical: Capacitors",
                "🎯 Quiz Me: 3 Rapid Physics MCQs",
                "⚠️ Common Board Mistakes: Ray Optics",
                "🧸 Explain Lenz's Law as EL5"
            )
            "Chemistry" -> listOf(
                "🧒 Explain Aldol Condensation (EL10)",
                "📊 Flowchart: Coordination Compounds IUPAC",
                "💡 Analogy for Activation Energy",
                "🧪 Mechanism: SN1 vs SN2 Reaction",
                "🎯 Quiz Me: 3 Chemistry PYQs",
                "⚠️ Board Traps: Electrochemistry Nernst",
                "🧠 Mnemonic: Lanthanide Contraction"
            )
            "Biology" -> listOf(
                "🧒 Explain Double Fertilization (EL10)",
                "📊 Diagram: Human Reproduction Cycle",
                "💡 Analogy for DNA Transcription",
                "🎯 Quiz Me: 3 NCERT Line Questions",
                "⚠️ High-Yield Board Traps: Genetics",
                "🧠 Mnemonic: Plant Hormones Auxin/Gibberellin"
            )
            "Mathematics" -> listOf(
                "🧒 Intuition behind Integration by Parts",
                "📊 Concept Map: Conic Sections formulas",
                "🔢 Step-by-step: Solve 3D Geometry shortest distance",
                "💡 Analogy for Matrix Determinants",
                "🎯 Quiz Me: 3 Vector Algebra MCQs"
            )
            "Computer Science" -> listOf(
                "🧒 Explain Binary Search Tree (EL10)",
                "📊 Flowchart: Bubble vs Quick Sort",
                "💡 Analogy for Stack & Heap memory",
                "💻 Write & Explain Python recursion code"
            )
            "Productivity" -> listOf(
                "⚡ How to crush Study Block 1 (Deep Work)",
                "📊 Flowchart: Dopamine Reset Protocol",
                "💡 Parkinson's Law for 3-Hour Exam prep",
                "🛑 How to stop phone checking during revision"
            )
            "Personal Development" -> listOf(
                "🧘 Stoic mindset when feeling overwhelmed",
                "💪 Consistency > Intensity: Daily system rule",
                "🎯 How to recover from a Red/Emergency Day",
                "✨ 3 Affirmations for Class 12 topper focus"
            )
            else -> listOf(
                "🧒 Explain Core Concept (EL10)",
                "📊 Draw Concept Flowchart / Diagram",
                "💡 Give Real-Life Analogy",
                "🎯 Quiz Me on this Topic",
                "⚡ 3 Key High-Yield Takeaways"
            )
        }
    }

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "AI Tutor & Mentor",
                onMenuClick = onOpenDrawer,
                currentStreak = streak,
                isLowEnergy = isLowEnergy,
                actions = {
                    // Vault Grounding Toggle Icon Button
                    IconButton(
                        onClick = { viewModel.toggleVaultGrounding(!isGrounded) },
                        modifier = Modifier.testTag("ai_tutor_vault_toggle")
                    ) {
                        Icon(
                            imageVector = if (isGrounded) Icons.Default.FolderSpecial else Icons.Outlined.Folder,
                            contentDescription = "Vault Grounding",
                            tint = if (isGrounded) RudraEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Clear Chat Menu / Button
                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.testTag("ai_tutor_clear_chat")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Interactive Bottom Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Quick Action Prompts Horizontal Carousel
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        items(quickActionChips) { prompt ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable {
                                    viewModel.sendAiTutorMessage(
                                        userQuery = prompt,
                                        subject = selectedSubject,
                                        mode = selectedMode,
                                        language = selectedLanguage,
                                        promptTag = "QuickAction"
                                    )
                                }
                            ) {
                                Text(
                                    text = prompt,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Input Field and Send Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Vault Question Picker Button
                        IconButton(
                            onClick = { showVaultPickerSheet = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("ai_tutor_vault_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FindInPage,
                                contentDescription = "Pick Question",
                                tint = RudraAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Text Field
                        OutlinedTextField(
                            value = queryText,
                            onValueChange = { queryText = it },
                            placeholder = {
                                Text(
                                    text = activeSubjectItem.placeholder,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_tutor_input_field"),
                            maxLines = 4,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = activeSubjectItem.color,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        )

                        // Send Button
                        IconButton(
                            onClick = {
                                if (queryText.isNotBlank()) {
                                    val textToSend = queryText
                                    queryText = ""
                                    viewModel.sendAiTutorMessage(
                                        userQuery = textToSend,
                                        subject = selectedSubject,
                                        mode = selectedMode,
                                        language = selectedLanguage
                                    )
                                }
                            },
                            enabled = !isLoading && queryText.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (!isLoading && queryText.isNotBlank()) activeSubjectItem.color
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .testTag("ai_tutor_send_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (queryText.isNotBlank()) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // TOP CONTROLS BAR: Subject Selector Row
            ScrollableSubjectHeader(
                selectedSubject = selectedSubject,
                onSelectSubject = { viewModel.selectAiSubject(it) }
            )

            // TEACHING MODE & STATUS BAR
            TutorConfigBanner(
                activeSubjectItem = activeSubjectItem,
                activeModeEnum = activeModeEnum,
                selectedLanguage = selectedLanguage,
                isGrounded = isGrounded,
                totalQuestionsCount = totalQuestionsCount,
                onOpenModePicker = { showModePickerMenu = true },
                onOpenLanguagePicker = { showLanguagePickerMenu = true },
                onToggleGrounding = { viewModel.toggleVaultGrounding(!isGrounded) }
            )

            // Dropdown Menus for Mode & Language
            Box {
                DropdownMenu(
                    expanded = showModePickerMenu,
                    onDismissRequest = { showModePickerMenu = false }
                ) {
                    TeachingMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(mode.emoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(mode.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Text(
                                        text = mode.description,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                viewModel.selectAiTeachingMode(mode.id)
                                showModePickerMenu = false
                            },
                            leadingIcon = {
                                if (selectedMode == mode.id) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = RudraAmber)
                                }
                            }
                        )
                    }
                }

                DropdownMenu(
                    expanded = showLanguagePickerMenu,
                    onDismissRequest = { showLanguagePickerMenu = false }
                ) {
                    listOf(
                        "BILINGUAL" to "Hinglish (Conversational Hindi + English terms)",
                        "ENGLISH" to "English (Crisp & standard English)",
                        "HINDI" to "हिंदी (सरल और स्पष्ट हिंदी)"
                    ).forEach { (code, title) ->
                        DropdownMenuItem(
                            text = { Text(title, fontSize = 13.sp) },
                            onClick = {
                                viewModel.selectAiLanguage(code)
                                showLanguagePickerMenu = false
                            },
                            leadingIcon = {
                                if (selectedLanguage == code) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = RudraAmber)
                                }
                            }
                        )
                    }
                }
            }

            // CHAT CONVERSATION LIST
            if (subjectMessages.isEmpty()) {
                // Empty state greeting card
                TutorWelcomeHero(
                    subjectItem = activeSubjectItem,
                    activeMode = activeModeEnum,
                    onPromptClick = { prompt ->
                        viewModel.sendAiTutorMessage(
                            userQuery = prompt,
                            subject = selectedSubject,
                            mode = selectedMode,
                            language = selectedLanguage
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp)
                ) {
                    items(
                        items = subjectMessages,
                        key = { it.id }
                    ) { message ->
                        if (message.role == "user") {
                            UserMessageBubble(message = message)
                        } else {
                            AssistantMessageBubble(
                                message = message,
                                isSpeaking = currentlySpeakingMessageId == message.id,
                                onToggleTts = {
                                    if (currentlySpeakingMessageId == message.id) {
                                        ttsInstance?.stop()
                                        currentlySpeakingMessageId = null
                                    } else {
                                        ttsInstance?.stop()
                                        val cleanText = cleanMarkdownForSpeech(message.content)
                                        ttsInstance?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "msg_${message.id}")
                                        currentlySpeakingMessageId = message.id
                                    }
                                },
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(message.content))
                                    Toast.makeText(context, "Explanation copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                onSaveToVault = {
                                    val title = "${selectedSubject}: ${message.promptTag.ifBlank { "Study Explanation" }}"
                                    viewModel.saveAiMessageToVault(title, message.content, selectedSubject)
                                    Toast.makeText(context, "Saved to Resource Vault as Study Note", Toast.LENGTH_SHORT).show()
                                },
                                onBookmark = {
                                    viewModel.toggleAiMessageBookmark(message.id, !message.isBookmarked)
                                },
                                onReExplainInMode = { targetMode ->
                                    // Find last user message before this
                                    val lastUserQuery = subjectMessages
                                        .filter { it.role == "user" && it.timestamp <= message.timestamp }
                                        .lastOrNull()?.content ?: "Explain this topic"
                                    viewModel.sendAiTutorMessage(
                                        userQuery = "Re-explain strictly in $targetMode mode: $lastUserQuery",
                                        subject = selectedSubject,
                                        mode = targetMode,
                                        language = selectedLanguage,
                                        promptTag = targetMode
                                    )
                                },
                                onShare = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Rudra AI Tutor - ${selectedSubject}")
                                        putExtra(Intent.EXTRA_TEXT, message.content)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Study Note"))
                                }
                            )
                        }
                    }

                    // Typing Indicator if loading
                    if (isLoading) {
                        item {
                            AiTypingIndicatorCard(
                                mode = activeModeEnum,
                                subject = selectedSubject
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Clearing Chat
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Chat History?") },
            text = { Text("Do you want to clear conversation history for $selectedSubject, or clear all subjects?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAiChatHistory(selectedSubject)
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Clear $selectedSubject Only", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.clearAiChatHistory(null)
                            showClearConfirmDialog = false
                        }
                    ) {
                        Text("Clear All Subjects", color = RudraRose)
                    }
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Vault Question Picker Bottom Sheet Modal
    if (showVaultPickerSheet) {
        VaultQuestionPickerSheet(
            allQuestions = allQuestions,
            selectedSubject = selectedSubject,
            onDismiss = { showVaultPickerSheet = false },
            onSelectQuestion = { q ->
                queryText = q.questionText
                showVaultPickerSheet = false
            }
        )
    }
}

// ------------------------------------------------------------------------------------------------
// TOP SUBJECT HEADER
// ------------------------------------------------------------------------------------------------
@Composable
fun ScrollableSubjectHeader(
    selectedSubject: String,
    onSelectSubject: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(AI_TUTOR_SUBJECTS) { item ->
                val isSelected = item.name.equals(selectedSubject, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) item.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) item.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .clickable { onSelectSubject(item.name) }
                        .testTag("subject_chip_${item.name.lowercase()}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.name,
                            tint = if (isSelected) item.color else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) item.color else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// TUTOR CONFIG BANNER
// ------------------------------------------------------------------------------------------------
@Composable
fun TutorConfigBanner(
    activeSubjectItem: SubjectItem,
    activeModeEnum: TeachingMode,
    selectedLanguage: String,
    isGrounded: Boolean,
    totalQuestionsCount: Int,
    onOpenModePicker: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onToggleGrounding: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Selector Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = activeModeEnum.badgeColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, activeModeEnum.badgeColor.copy(alpha = 0.4f)),
                modifier = Modifier.clickable { onOpenModePicker() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(activeModeEnum.emoji, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = activeModeEnum.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = activeModeEnum.badgeColor
                        )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Mode",
                        tint = activeModeEnum.badgeColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Language & Grounding Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Language Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable { onOpenLanguagePicker() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "HINDI" -> "हिंदी"
                                "ENGLISH" -> "English"
                                else -> "Hinglish"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Language",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Grounding Status Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isGrounded) RudraEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (isGrounded) RudraEmerald.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.clickable { onToggleGrounding() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isGrounded) RudraEmerald else MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGrounded) "Vault ($totalQuestionsCount)" else "Off",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGrounded) RudraEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// TUTOR WELCOME HERO
// ------------------------------------------------------------------------------------------------
@Composable
fun TutorWelcomeHero(
    subjectItem: SubjectItem,
    activeMode: TeachingMode,
    onPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon Circle
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(subjectItem.color.copy(alpha = 0.15f))
                .border(2.dp, subjectItem.color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = subjectItem.icon,
                contentDescription = subjectItem.name,
                tint = subjectItem.color,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Rudra ${subjectItem.name} AI Tutor",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Personal Teacher, Mentor & Visual Concept Explainer",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Mode badge indicator
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = activeMode.badgeColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, activeMode.badgeColor.copy(alpha = 0.3f))
        ) {
            Text(
                text = "${activeMode.emoji} Mode: ${activeMode.label} — ${activeMode.description}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = activeMode.badgeColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "POPULAR STUDY TOPICS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        val welcomePrompts = when (subjectItem.name) {
            "Physics" -> listOf(
                "⚡ Explain Gauss's Law with water balloon analogy",
                "📊 Flowchart: Series vs Parallel LCR Resonance",
                "💡 Demystify Capacitance: Why do dielectric slabs increase C?",
                "🔢 Step-by-step Numerical: Electric Dipole torque in uniform E"
            )
            "Chemistry" -> listOf(
                "🧪 Explain Aldol Condensation with a real-life analogy",
                "📊 Flowchart: Coordination Compounds IUPAC naming rules",
                "💡 Why is Phenol more acidic than Ethanol?",
                "🧠 Memory hook for Lanthanide Contraction"
            )
            "Biology" -> listOf(
                "🌸 Flowchart: Double Fertilization step-by-step",
                "💡 DNA Replication explained like a factory conveyor belt",
                "🎯 3 Common Board Traps in Mendelian Genetics",
                "🧠 Mnemonic for 5 Plant Hormones"
            )
            "Mathematics" -> listOf(
                "📐 Intuition behind Integration by Parts (Product rule inverse)",
                "📊 Concept Map of 3D Geometry lines and planes",
                "🔢 Step-by-step: Solve non-homogeneous system of linear equations"
            )
            else -> listOf(
                "🧒 Explain core foundational concept with a real-life analogy",
                "📊 Draw a complete visual text flowchart / diagram",
                "🎯 Give me a 3-question rapid check quiz"
            )
        }

        welcomePrompts.forEach { prompt ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onPromptClick(prompt) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = subjectItem.color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// USER MESSAGE BUBBLE
// ------------------------------------------------------------------------------------------------
@Composable
fun UserMessageBubble(message: AiChatMessageEntity) {
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.promptTag.isNotBlank()) {
                        Text(
                            text = "[${message.promptTag}]",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// ASSISTANT MESSAGE BUBBLE WITH DIAGRAM ENGINE & QUICK ACTIONS
// ------------------------------------------------------------------------------------------------
@Composable
fun AssistantMessageBubble(
    message: AiChatMessageEntity,
    isSpeaking: Boolean,
    onToggleTts: () -> Unit,
    onCopy: () -> Unit,
    onSaveToVault: () -> Unit,
    onBookmark: () -> Unit,
    onReExplainInMode: (String) -> Unit,
    onShare: () -> Unit
) {
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    val modeItem = remember(message.mode) {
        TeachingMode.values().find { it.id == message.mode } ?: TeachingMode.EL10
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header Row: Avatar, Persona Tag, Mode Pill & Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(RudraAmber),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "AI Tutor",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RUDRA AI TUTOR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = RudraAmber,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Teaching Mode Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = modeItem.badgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${modeItem.emoji} ${modeItem.id}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = modeItem.badgeColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        if (message.isGrounded) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = RudraEmerald.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Vault Grounded",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RudraEmerald,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = timeStr,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rich Markdown & Text Diagram Body
                RichAiTutorContentRenderer(content = message.content)

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Spacer(modifier = Modifier.height(8.dp))

                // ACTION TOOLBAR: TTS, Copy, Save to Vault, Bookmark, Share, Re-explain
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Action Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        // TTS Audio Play/Stop
                        IconButton(
                            onClick = onToggleTts,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Read Aloud",
                                tint = if (isSpeaking) RudraAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Copy
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Text",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Save to Vault
                        IconButton(
                            onClick = onSaveToVault,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SaveAlt,
                                contentDescription = "Save to Vault",
                                tint = RudraEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Bookmark
                        IconButton(
                            onClick = onBookmark,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (message.isBookmarked) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Bookmark",
                                tint = if (message.isBookmarked) RudraAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Share
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Adaptive Mode Switch Quick Pills
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (message.mode != "EL5") {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, RudraCyan.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable { onReExplainInMode("EL5") }
                            ) {
                                Text(
                                    text = "🧸 EL5",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RudraCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (message.mode != "BOARD_EXAM") {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, RudraAmber.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable { onReExplainInMode("BOARD_EXAM") }
                            ) {
                                Text(
                                    text = "🎯 Board Exam",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RudraAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// RICH AI TUTOR CONTENT RENDERER (Supports Diagrams, Tables, Headings, Math)
// ------------------------------------------------------------------------------------------------
@Composable
fun RichAiTutorContentRenderer(content: String) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val blocks = remember(content) {
        parseMarkdownAndDiagramBlocks(content)
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        blocks.forEach { block ->
            when (block) {
                is ParsedBlock.Heading -> {
                    val fontSize = when (block.level) {
                        1 -> 18.sp
                        2 -> 16.sp
                        else -> 14.sp
                    }
                    val color = when (block.level) {
                        1 -> MaterialTheme.colorScheme.primary
                        2 -> RudraAmber
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    Text(
                        text = block.text,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                is ParsedBlock.Paragraph -> {
                    Text(
                        text = block.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 21.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                is ParsedBlock.BulletItem -> {
                    Row(modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp)) {
                        Text(
                            text = "• ",
                            color = RudraAmber,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = block.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                is ParsedBlock.DiagramOrCode -> {
                    // Monospace Box for Flowcharts, Concept Maps, Block Diagrams & Code
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (block.isDiagram) Icons.Default.AccountTree else Icons.Default.Code,
                                        contentDescription = null,
                                        tint = if (block.isDiagram) RudraCyan else RudraAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (block.isDiagram) "CONCEPT DIAGRAM / FLOWCHART" else "CODE / FORMULA BLOCK",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (block.isDiagram) RudraCyan else RudraAmber,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.clickable {
                                        clipboardManager.setText(AnnotatedString(block.rawText))
                                        Toast.makeText(context, "Diagram copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Copy", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Scrollable monospace text area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = block.rawText,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }

                is ParsedBlock.CalloutNote -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = block.tintColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, block.tintColor.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = block.icon,
                                contentDescription = null,
                                tint = block.tintColor,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = block.text,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// PARSING MODELS & LOGIC
// ------------------------------------------------------------------------------------------------
sealed class ParsedBlock {
    data class Heading(val level: Int, val text: String) : ParsedBlock()
    data class Paragraph(val text: String) : ParsedBlock()
    data class BulletItem(val text: String) : ParsedBlock()
    data class DiagramOrCode(val rawText: String, val isDiagram: Boolean) : ParsedBlock()
    data class CalloutNote(val text: String, val icon: ImageVector, val tintColor: Color) : ParsedBlock()
}

fun parseMarkdownAndDiagramBlocks(raw: String): List<ParsedBlock> {
    val result = mutableListOf<ParsedBlock>()
    val lines = raw.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // 1. Code Block / Diagram Block delimited by ```
        if (line.trimStart().startsWith("```")) {
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            if (i < lines.size) i++ // skip closing ```

            val fullBlockText = codeLines.joinToString("\n")
            val isDiagram = fullBlockText.contains("──>") ||
                    fullBlockText.contains("┌") ||
                    fullBlockText.contains("│") ||
                    fullBlockText.contains("+---") ||
                    fullBlockText.contains("-->") ||
                    fullBlockText.contains("|")

            result.add(ParsedBlock.DiagramOrCode(rawText = fullBlockText, isDiagram = isDiagram))
            continue
        }

        // 2. ASCII Diagram detected in raw lines (contains boxes or arrows)
        if (line.contains("┌") || line.contains("├──") || line.contains("──>") || line.contains("+---+") || (line.startsWith("|") && line.endsWith("|"))) {
            val diagramLines = mutableListOf<String>()
            while (i < lines.size && (
                        lines[i].contains("┌") || lines[i].contains("│") || lines[i].contains("└") ||
                        lines[i].contains("──>") || lines[i].contains("+---") || lines[i].contains("|") ||
                        lines[i].startsWith("[") || lines[i].trim().isEmpty()
                    )) {
                if (lines[i].trim().isEmpty() && i + 1 < lines.size && !lines[i + 1].contains("│") && !lines[i + 1].contains("─")) {
                    break
                }
                diagramLines.add(lines[i])
                i++
            }
            if (diagramLines.isNotEmpty()) {
                result.add(ParsedBlock.DiagramOrCode(rawText = diagramLines.joinToString("\n"), isDiagram = true))
                continue
            }
        }

        // 3. Headings
        if (line.startsWith("# ")) {
            result.add(ParsedBlock.Heading(1, line.removePrefix("# ").trim()))
            i++
            continue
        }
        if (line.startsWith("## ")) {
            result.add(ParsedBlock.Heading(2, line.removePrefix("## ").trim()))
            i++
            continue
        }
        if (line.startsWith("### ")) {
            result.add(ParsedBlock.Heading(3, line.removePrefix("### ").trim()))
            i++
            continue
        }

        // 4. Callout / Exam Tips
        if (line.startsWith("> ") || line.startsWith("⚠️") || line.startsWith("💡")) {
            val isTip = line.contains("Tip", ignoreCase = true) || line.contains("💡")
            val isWarning = line.contains("Mistake", ignoreCase = true) || line.contains("⚠️") || line.contains("Trap", ignoreCase = true)
            result.add(
                ParsedBlock.CalloutNote(
                    text = line.removePrefix("> ").trim(),
                    icon = if (isWarning) Icons.Default.Warning else Icons.Default.Lightbulb,
                    tintColor = if (isWarning) RudraRose else RudraAmber
                )
            )
            i++
            continue
        }

        // 5. Bullet items
        if (line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ")) {
            result.add(ParsedBlock.BulletItem(line.trimStart().drop(2).trim()))
            i++
            continue
        }

        // 6. Regular Paragraphs
        if (line.isNotBlank()) {
            result.add(ParsedBlock.Paragraph(line.trim()))
        }

        i++
    }

    return result
}

fun cleanMarkdownForSpeech(raw: String): String {
    return raw
        .replace(Regex("```[\\s\\S]*?```"), " Here is a visual diagram. ")
        .replace("#", "")
        .replace("*", "")
        .replace("- ", "")
        .replace("`", "")
        .replace("⚠️", "Warning: ")
        .replace("💡", "Tip: ")
}

// ------------------------------------------------------------------------------------------------
// TYPING INDICATOR CARD
// ------------------------------------------------------------------------------------------------
@Composable
fun AiTypingIndicatorCard(mode: TeachingMode, subject: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, RudraAmber.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = RudraAmber,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI Tutor is crafting step-by-step explanation...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                    )
                )
                Text(
                    text = "Mode: ${mode.label} | Subject: $subject",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// VAULT QUESTION PICKER BOTTOM SHEET
// ------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultQuestionPickerSheet(
    allQuestions: List<QuestionEntity>,
    selectedSubject: String,
    onDismiss: () -> Unit,
    onSelectQuestion: (QuestionEntity) -> Unit
) {
    val filtered = remember(allQuestions, selectedSubject) {
        allQuestions.filter { it.subject.equals(selectedSubject, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Resource Vault Question Bank",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Select extracted question to solve with AI Tutor (${filtered.size} available in $selectedSubject)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No questions found for $selectedSubject in Resource Vault. Ingest PDFs in the Vault to extract questions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                ) {
                    items(filtered) { q ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectQuestion(q) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = q.chapterName.ifBlank { "Chapter Note" },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RudraAmber
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = "${q.marks}M • ${q.questionType}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = q.questionText,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
