package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.example.data.local.*
import com.example.data.preferences.PreferencesManager
import com.example.data.repository.RudraRepository
import com.example.network.GeminiClient
import com.example.service.PdfExtractionWorkManager
import com.example.service.PdfQuestionExtractionWorker
import com.example.service.TimelineNotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import com.example.data.testengine.ExamAnalysisReport
import com.example.data.testengine.ExamIntelligenceEngine
import com.example.data.testengine.GeneratedExamBundle
import com.example.data.testengine.TestItemData
import org.json.JSONArray
import org.json.JSONObject

data class SyllabusProgressSummary(
    val overallPercent: Int,
    val subjectPercents: Map<String, Int>,
    val totalTopicsCount: Int,
    val masteredTopicsCount: Int,
    val revisedTopicsCount: Int,
    val learningTopicsCount: Int,
    val weakTopicsCount: Int
)

data class InteractiveQuizState(
    val testId: Long = 0L,
    val title: String,
    val board: String,
    val subject: String,
    val testMode: String,
    val difficulty: String,
    val totalMarks: Int,
    val timeLimitMinutes: Int,
    val items: List<TestItemData>,
    val currentQuestionIndex: Int = 0,
    val userAnswers: Map<Int, String> = emptyMap(), // questionNumber -> answer
    val bookmarkedQuestionIndices: Set<Int> = emptySet(),
    val timeRemainingSeconds: Int = 0,
    val isSubmitted: Boolean = false,
    val isReviewMode: Boolean = false
)

data class ExamPaperViewState(
    val testId: Long = 0L,
    val title: String,
    val board: String,
    val subject: String,
    val testMode: String,
    val totalMarks: Int,
    val timeMinutes: Int,
    val questionPaperMarkdown: String,
    val answerKeyMarkdown: String,
    val solutionMarkdown: String,
    val analysisReport: ExamAnalysisReport?,
    val activeTab: Int = 0, // 0: Question Paper, 1: Answer Key, 2: Solutions, 3: Pre-Exam Analysis
    val zoomLevel: Float = 1.0f,
    val searchQuery: String = "",
    val isBookmarked: Boolean = false
)

class RudraViewModel(application: Application) : AndroidViewModel(application) {

    private val database = RudraDatabase.getDatabase(application)
    val repository = RudraRepository(database, application)
    val preferencesManager = PreferencesManager(application)

    // Observables
    val timelineBlocks = repository.allTimelineBlocks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTopics = repository.allTopicProgress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pendingRevisions = repository.pendingRevisionTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allRevisions = repository.allRevisionTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val todayScorecard = repository.getTodayScorecardFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val pastScorecards = repository.allScorecards.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pyqList = repository.allPyqs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val focusSessions = repository.allFocusSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val generatedTests = repository.allGeneratedTests.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pdfList = repository.allPdfs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val weakTopics = repository.weakTopics.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTestAttempts = repository.allTestAttempts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val latestTestAttempt = repository.latestTestAttempt.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Resource Vault, Question Database & Pattern Database Observables
    val vaultDocuments = repository.allVaultDocuments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val vaultStorageBytes = repository.vaultStorageBytes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val vaultDocsCount = repository.vaultDocsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val allQuestions = repository.allQuestions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalQuestionsCount = repository.totalQuestionsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val allPatterns = repository.allPatterns.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalPatternsCount = repository.totalPatternsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Active Examination States (Interactive Quiz Mode & Realistic PDF Exam Mode)
    val activeQuizState = MutableStateFlow<InteractiveQuizState?>(null)
    val activePaperState = MutableStateFlow<ExamPaperViewState?>(null)
    val lastCompletedAttempt = MutableStateFlow<TestAttemptEntity?>(null)
    val activePreAnalysisReport = MutableStateFlow<ExamAnalysisReport?>(null)
    val lastGeneratedBundle = MutableStateFlow<GeneratedExamBundle?>(null)

    // Vault Upload & Analysis State
    val isVaultUploading = MutableStateFlow(false)
    val vaultUploadStatus = MutableStateFlow<String?>(null)
    val isVaultAnalyzing = MutableStateFlow(false)
    val vaultAnalysisStatus = MutableStateFlow<String?>(null)

    // WorkManager PDF Extraction Live Observables
    val pdfExtractionWorkInfo: StateFlow<WorkInfo?> = PdfExtractionWorkManager.getWorkInfoFlow(application)
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Preferences
    val geminiApiKey = preferencesManager.geminiApiKeyFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val selectedBoard = preferencesManager.selectedBoardFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BSEB")
    val isLowEnergyMode = preferencesManager.isLowEnergyModeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val notificationsEnabled = preferencesManager.notificationsEnabledFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val themeMode = preferencesManager.themeModeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DARK")
    val currentStreak = preferencesManager.streakFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    // Derived Progress Summary
    val syllabusSummary = combine(allTopics, selectedBoard) { topics, board ->
        val boardTopics = topics.filter { it.board == board }
        if (boardTopics.isEmpty()) {
            SyllabusProgressSummary(0, emptyMap(), 0, 0, 0, 0, 0)
        } else {
            val total = boardTopics.size
            val mastered = boardTopics.count { it.status == "MASTERED" }
            val revised = boardTopics.count { it.status == "REVISED" }
            val learning = boardTopics.count { it.status == "LEARNING" || it.status == "PRACTICING" }
            val weak = boardTopics.count { it.isWeakTopic }

            val totalScore = boardTopics.sumOf {
                when (it.status) {
                    "MASTERED" -> 100
                    "REVISED" -> 80
                    "PRACTICING" -> 60
                    "LEARNING" -> 30
                    else -> 0
                }
            }
            val overall = (totalScore / total).coerceIn(0, 100)

            val subjectMap = boardTopics.groupBy { it.subject }.mapValues { (_, subTopics) ->
                if (subTopics.isEmpty()) 0 else {
                    val subSum = subTopics.sumOf {
                        when (it.status) {
                            "MASTERED" -> 100
                            "REVISED" -> 80
                            "PRACTICING" -> 60
                            "LEARNING" -> 30
                            else -> 0
                        }
                    }
                    (subSum / subTopics.size).coerceIn(0, 100)
                }
            }

            SyllabusProgressSummary(
                overallPercent = overall,
                subjectPercents = subjectMap,
                totalTopicsCount = total,
                masteredTopicsCount = mastered,
                revisedTopicsCount = revised,
                learningTopicsCount = learning,
                weakTopicsCount = weak
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyllabusProgressSummary(0, emptyMap(), 0, 0, 0, 0, 0))

    // AI Tutor States & Chat Persistence
    val aiChatMessages = repository.allAiMessages.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val selectedAiSubject = MutableStateFlow("Physics")
    val selectedAiTeachingMode = MutableStateFlow("EL10") // "EL10", "EL5", "BEGINNER", "INTERMEDIATE", "ADVANCED", "BOARD_EXAM"
    val selectedAiLanguage = MutableStateFlow("BILINGUAL") // "BILINGUAL", "HINDI", "ENGLISH"
    val isVaultGroundingEnabled = MutableStateFlow(true)

    // Legacy AI single-shot states for compatibility
    val aiTutorResponse = MutableStateFlow<String?>(null)
    val isAiTutorLoading = MutableStateFlow(false)

    val aiCoachResponse = MutableStateFlow<String?>(null)
    val isAiCoachLoading = MutableStateFlow(false)

    val aiTestGenResponse = MutableStateFlow<String?>(null)
    val isAiTestGenLoading = MutableStateFlow(false)

    val screenTimeAnalysisResult = MutableStateFlow<String?>(null)
    val isScreenTimeAnalyzing = MutableStateFlow(false)

    val apiValidationStatus = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            preferencesManager.updateStreak(repository.getTodayDateString())

            // Schedule notification alarms
            timelineBlocks.collect { blocks ->
                if (blocks.isNotEmpty() && notificationsEnabled.value) {
                    TimelineNotificationHelper.scheduleBlockReminders(application, blocks)
                }
            }
        }
    }

    // Timeline actions
    fun toggleBlockCompleted(id: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.setBlockCompletedToday(id, completed)
        }
    }

    fun addTimelineBlock(title: String, subtitle: String, start: String, end: String, type: String, trigger: String, backup: String) {
        viewModelScope.launch {
            repository.insertBlock(
                TimelineBlockEntity(
                    title = title,
                    subtitle = subtitle,
                    startTime = start,
                    endTime = end,
                    type = type,
                    triggerAction = trigger,
                    backupVersion = backup,
                    orderIndex = (timelineBlocks.value.maxOfOrNull { it.orderIndex } ?: 0) + 1
                )
            )
        }
    }

    fun updateTimelineBlock(block: TimelineBlockEntity) {
        viewModelScope.launch {
            repository.updateBlock(block)
        }
    }

    fun deleteTimelineBlock(id: Long) {
        viewModelScope.launch {
            repository.deleteBlock(id)
        }
    }

    fun resetTimeline() {
        viewModelScope.launch {
            repository.resetTimelineToDefault()
        }
    }

    // Topic Progress Actions
    fun cycleTopicStatus(topicId: String) {
        viewModelScope.launch {
            repository.cycleTopicStatus(topicId)
        }
    }

    fun setTopicStatus(topicId: String, status: String) {
        viewModelScope.launch {
            repository.setTopicStatusQuick(topicId, status)
        }
    }

    fun markChapterComplete(subject: String, chapterName: String) {
        viewModelScope.launch {
            repository.markChapterComplete(subject, chapterName)
        }
    }

    fun markChapterStatus(subject: String, chapterName: String, status: String) {
        viewModelScope.launch {
            repository.markChapterStatus(subject, chapterName, status)
        }
    }

    fun toggleWeakTopic(topicId: String, isWeak: Boolean) {
        viewModelScope.launch {
            repository.updateTopicProgress(topicId, status = "LEARNING", completion = 40, isWeak = !isWeak)
        }
    }

    fun updateTopic(topicId: String, status: String, completion: Int, isWeak: Boolean, notes: String = "") {
        viewModelScope.launch {
            repository.updateTopicProgress(topicId, status, completion, isWeak, notes)
        }
    }

    // Revision Actions
    fun completeRevision(taskId: Long, topicId: String) {
        viewModelScope.launch {
            repository.completeRevisionTask(taskId, topicId)
        }
    }

    // Scorecard Actions
    fun updateTodayScorecard(
        wokeUp: Boolean,
        b1: Boolean,
        b3: Boolean,
        fitness: Boolean,
        b5: Boolean,
        shutdown: Boolean,
        noPhone: Boolean,
        jDone: String,
        jMissed: String,
        jFocus: String,
        isEmergency: Boolean = false,
        isLowEnergy: Boolean = false
    ) {
        viewModelScope.launch {
            val todayStr = repository.getTodayDateString()
            val existing = repository.getTodayScorecardFlow().firstOrNull() ?: DailyScorecardEntity(dateString = todayStr)
            val updated = existing.copy(
                wokeUpOnTime = wokeUp,
                completedBlock1 = b1,
                completedBlock3 = b3,
                completedFitness = fitness,
                completedBlock5 = b5,
                didShutdownRitual = shutdown,
                noPhoneBlockedHours = noPhone,
                journalLineDone = jDone,
                journalLineMissed = jMissed,
                journalLineFocusTomorrow = jFocus,
                isEmergencyDay = isEmergency,
                isLowEnergyDay = isLowEnergy
            )
            repository.updateScorecard(updated)
        }
    }

    // Focus Session Log
    fun logFocusSession(subject: String, topic: String, durationMinutes: Int, thoughts: String, rating: Int) {
        viewModelScope.launch {
            repository.logFocusSession(subject, topic, durationMinutes, thoughts, rating)
        }
    }

    // ==========================================
    // AI TUTOR - PERSONAL TEACHER & MENTOR SYSTEM
    // ==========================================

    fun selectAiSubject(subject: String) {
        selectedAiSubject.value = subject
    }

    fun selectAiTeachingMode(mode: String) {
        selectedAiTeachingMode.value = mode
    }

    fun selectAiLanguage(language: String) {
        selectedAiLanguage.value = language
    }

    fun toggleVaultGrounding(enabled: Boolean) {
        isVaultGroundingEnabled.value = enabled
    }

    fun sendAiTutorMessage(
        userQuery: String,
        subject: String = selectedAiSubject.value,
        mode: String = selectedAiTeachingMode.value,
        language: String = selectedAiLanguage.value,
        promptTag: String = ""
    ) {
        val trimmedQuery = userQuery.trim()
        if (trimmedQuery.isBlank()) return

        val apiKey = geminiApiKey.value

        viewModelScope.launch {
            // 1. Record user message in DB
            val userMsg = AiChatMessageEntity(
                role = "user",
                content = trimmedQuery,
                subject = subject,
                mode = mode,
                language = language,
                isGrounded = isVaultGroundingEnabled.value,
                promptTag = promptTag
            )
            repository.insertAiMessage(userMsg)

            if (apiKey.isBlank()) {
                val errorMsg = AiChatMessageEntity(
                    role = "assistant",
                    content = "⚠️ **Gemini API Key Missing**\n\nPlease enter your Gemini API Key in **Settings** to activate your AI Teacher & Study Companion.",
                    subject = subject,
                    mode = mode,
                    language = language
                )
                repository.insertAiMessage(errorMsg)
                return@launch
            }

            isAiTutorLoading.value = true

            // Retrieve grounding from Vault documents & question bank if enabled
            var groundingText = ""
            if (isVaultGroundingEnabled.value) {
                try {
                    val matchingQuestions = repository.allQuestions.firstOrNull()?.filter {
                        it.subject.equals(subject, ignoreCase = true) &&
                        (it.questionText.contains(trimmedQuery, ignoreCase = true) ||
                         trimmedQuery.contains(it.chapterName, ignoreCase = true) ||
                         (it.topicName.isNotBlank() && trimmedQuery.contains(it.topicName, ignoreCase = true)))
                    }?.take(3)

                    if (!matchingQuestions.isNullOrEmpty()) {
                        groundingText = "\n\n[Grounding from Student's Resource Vault & Question Bank]:\n" +
                                matchingQuestions.joinToString("\n---\n") {
                                    "Chapter: ${it.chapterName} | Type: ${it.questionType} | Marks: ${it.marks}M\nQ: ${it.questionText}\nSolution Outline: ${it.stepByStepSolution}"
                                }
                    }
                } catch (_: Exception) {}
            }

            val languageInstruction = when (language) {
                "HINDI" -> "Explain strictly in clear, easy-to-understand Hindi (Devanagari script) with technical terms in English where helpful."
                "ENGLISH" -> "Explain strictly in crisp, crystal-clear English."
                else -> "Explain in clear Hinglish (Conversational everyday Hindi + English technical terms) as used by India's best teachers."
            }

            val modeInstruction = when (mode) {
                "EL5" -> """
                    Teaching Mode: EL5 (Explain Like I Am 5 Years Old)
                    - Use toy, playground, kitchen, or animal metaphors.
                    - Keep sentences short, friendly, and intuitive.
                    - Zero heavy jargon without instant childlike analogy.
                    - Include a mini ASCII/text box diagram if visual.
                """.trimIndent()
                "BEGINNER" -> """
                    Teaching Mode: Beginner Friendly
                    - Focus on fundamental concepts, intuitive reasoning, and clear definitions.
                    - Gentle pacing, break down concepts into bite-sized blocks.
                """.trimIndent()
                "INTERMEDIATE" -> """
                    Teaching Mode: Intermediate / High School Standards
                    - Follow standard Class 11/12 NCERT curriculum.
                    - Provide formal definitions, standard formulas, and clear step-by-step examples.
                """.trimIndent()
                "ADVANCED" -> """
                    Teaching Mode: Advanced & Rigorous Derivation
                    - Provide deep conceptual derivations, vector calculus, reaction mechanisms, electron shifts, or algorithmic details.
                    - Cover subtle edge cases, exceptions, and competitive exam level insights.
                """.trimIndent()
                "BOARD_EXAM" -> """
                    Teaching Mode: Board Exam 100% Score Master (CBSE / State Board)
                    - Format with exact keywords to underline in exam sheets.
                    - Point-wise structure matching board marking criteria.
                    - Standard Board Definition + SI Units + Dimensional formula.
                    - Board Exam Scoring Tip & Common Mistakes to avoid losing marks.
                    - ASCII/Text Diagram ready to draw on the answer sheet.
                """.trimIndent()
                else -> """
                    Teaching Mode: EL10 (Explain Like I Am 10 Years Old - DEFAULT MODE)
                    - Explain in simple, intuitive language using relatable real-life analogies.
                    - Step-by-step clear narrative.
                    - Whenever helpful, generate a clean ASCII text diagram (Flowchart, Concept Map, Block Diagram, Mind Map, or Table).
                    - If technical terms are used, immediately demystify them with an everyday real-world comparison.
                    - Provide 1 clear, practical example with step-by-step clarity.
                """.trimIndent()
            }

            val systemInstruction = """
                You are the Rudra Master AI Tutor — a personal teacher, mentor, and study companion for students studying Physics, Chemistry, Biology, Mathematics, English, Hindi, Computer Science, General Knowledge, Productivity, and Personal Development.
                
                Core Rules:
                1. You are NOT a generic chatbot. You are an empathetic, world-class educator who loves making difficult concepts effortlessly understandable.
                2. Visual Thinking: Whenever explaining a process, reaction, derivation, cycle, or classification, ALWAYS draw a crisp, clear text-based diagram (e.g. Flowchart, Concept Map, ASCII Tree, Block Diagram, or Table) using monospace characters (boxes, arrows ──>, ┌─┐).
                3. $languageInstruction
                4. $modeInstruction
                5. Structure answers with clear Markdown headings, bullet points, and bold text for visual scannability.
                6. End with an encouraging 1-line mentor tip or reflection prompt.
            """.trimIndent()

            // Fetch recent conversation history for this subject
            val history = repository.getAiMessagesBySubject(subject).firstOrNull()?.takeLast(10) ?: emptyList()
            val contents = mutableListOf<com.example.network.Content>()

            for (msg in history) {
                contents.add(
                    com.example.network.Content(
                        parts = listOf(com.example.network.Part(text = msg.content)),
                        role = if (msg.role == "assistant") "model" else "user"
                    )
                )
            }

            val queryWithGrounding = if (groundingText.isNotBlank()) {
                "$trimmedQuery\n$groundingText"
            } else {
                trimmedQuery
            }
            contents.add(
                com.example.network.Content(
                    parts = listOf(com.example.network.Part(text = queryWithGrounding)),
                    role = "user"
                )
            )

            val modelToUse = if (mode == "ADVANCED") GeminiClient.MODEL_PRO else GeminiClient.MODEL_FLASH

            val result = GeminiClient.chatMultiTurn(
                apiKey = apiKey,
                messages = contents,
                systemInstruction = systemInstruction,
                model = modelToUse
            )

            isAiTutorLoading.value = false

            val assistantContent = result.getOrElse { error ->
                "⚠️ **Failed to get response**: ${error.message}\n\nPlease check your internet connection or verify your Gemini API key in Settings."
            }

            val assistantMsg = AiChatMessageEntity(
                role = "assistant",
                content = assistantContent,
                subject = subject,
                mode = mode,
                language = language,
                isGrounded = isVaultGroundingEnabled.value && groundingText.isNotBlank()
            )
            repository.insertAiMessage(assistantMsg)
        }
    }

    fun clearAiChatHistory(subject: String? = null) {
        viewModelScope.launch {
            if (subject != null) {
                repository.clearAiMessagesBySubject(subject)
            } else {
                repository.clearAiMessages()
            }
        }
    }

    fun toggleAiMessageBookmark(id: Long, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleAiMessageBookmark(id, isBookmarked)
        }
    }

    fun saveAiMessageToVault(title: String, content: String, subject: String) {
        viewModelScope.launch {
            repository.insertVaultDocument(
                VaultDocumentEntity(
                    fileName = "AI_Tutor_${System.currentTimeMillis()}.txt",
                    title = title.ifBlank { "AI Tutor Study Note - $subject" },
                    fileUriOrPath = "vault/internal/${System.currentTimeMillis()}.txt",
                    fileSizeBytes = content.toByteArray().size.toLong(),
                    pageCount = 1,
                    subject = subject,
                    category = "CHAPTER_NOTES",
                    board = "BSEB",
                    extractedText = content,
                    isAnalyzed = true,
                    analyzedSummary = "Generated by Rudra AI Tutor (${selectedAiTeachingMode.value} Mode)"
                )
            )
        }
    }

    // Legacy single-shot AI Tutor function for backward compatibility
    fun askAiTutor(
        topicOrQuestion: String,
        subject: String,
        mode: String = "BILINGUAL",
        type: String = "EXPLAIN"
    ) {
        sendAiTutorMessage(
            userQuery = topicOrQuestion,
            subject = subject,
            mode = selectedAiTeachingMode.value,
            language = mode,
            promptTag = type
        )
    }

    // AI Coach
    fun requestAiCoachAnalysis(type: String = "DAILY") {
        val apiKey = geminiApiKey.value
        if (apiKey.isBlank()) {
            aiCoachResponse.value = "⚠️ Please configure your Gemini API Key in Settings to get AI Coach reviews."
            return
        }

        viewModelScope.launch {
            isAiCoachLoading.value = true
            aiCoachResponse.value = null

            val summary = syllabusSummary.value
            val todayCard = todayScorecard.value
            val recentScores = pastScorecards.value.take(7)

            val coachPrompt = """
                Rudra Life OS Status Report:
                - Review Type: $type Review
                - Overall Syllabus Progress: ${summary.overallPercent}%
                - Subject Breakdown: ${summary.subjectPercents}
                - Weak Topics Count: ${summary.weakTopicsCount}
                - Today's Discipline Score: ${todayCard?.totalScore ?: 0}/7 (Status: ${todayCard?.scoreStatus ?: "N/A"})
                - Journal Done: ${todayCard?.journalLineDone}
                - Journal Missed: ${todayCard?.journalLineMissed}
                - Low Energy Mode Active: ${isLowEnergyMode.value}
                - Past 7 Days Scores: ${recentScores.map { "${it.dateString}: ${it.totalScore}/7" }}
                
                You are the Rudra OS Personal Coach. Based strictly on the philosophy:
                1. System > Willpower
                2. Consistency > Intensity (40% daily beats 100% then burnout)
                3. Never-Miss-Twice Rule
                
                Provide:
                1. Objective Assessment (No fake praise, purely data-backed).
                2. Immediate Adjustment ("Chhota Knob" - one single tweak for tomorrow).
                3. Emergency / Low-Energy recommendation if struggling.
                4. Exact Priority Subject for tomorrow's Study Block 1 & 3.
            """.trimIndent()

            val result = GeminiClient.askAi(
                apiKey = apiKey,
                prompt = coachPrompt,
                systemInstruction = "You are the Rudra Life OS Coach. You don't give motivational speeches. You provide calm, stoic, strategic engineering for student execution."
            )

            isAiCoachLoading.value = false
            aiCoachResponse.value = result.getOrElse { "Error: ${it.message}" }
        }
    }

    // Screen Time Screenshot Analyzer
    fun analyzeScreenTime(textOrLog: String, bitmap: Bitmap? = null) {
        val apiKey = geminiApiKey.value
        if (apiKey.isBlank()) {
            screenTimeAnalysisResult.value = "⚠️ Please configure your Gemini API Key in Settings."
            return
        }

        viewModelScope.launch {
            isScreenTimeAnalyzing.value = true
            screenTimeAnalysisResult.value = null

            val prompt = """
                Analyze this Screen Time Data / Screenshot for a Class 12 Science student using Rudra Life OS:
                Data/Notes: $textOrLog
                
                Extract and analyze:
                1. Total Screen Time & Top 3 Distraction Apps (Reels/YouTube/Instagram/Gaming).
                2. Dopamine Baseline Impact: How this phone usage is sabotaging Study Block 1 and Study Block 3.
                3. Friction Solution: Exact environment friction rule (e.g. Phone charging outside bedroom at 9:45 PM, 0 phone during 6:15-7:30 AM).
                4. 7-Day Digital Detox Protocol to recover lost focus.
            """.trimIndent()

            val result = GeminiClient.askAi(
                apiKey = apiKey,
                prompt = prompt,
                systemInstruction = "You are an expert digital minimalism and dopamine management analyst.",
                bitmap = bitmap
            )

            isScreenTimeAnalyzing.value = false
            screenTimeAnalysisResult.value = result.getOrElse { "Error: ${it.message}" }
        }
    }

    // AI Test Generator
    fun generateAiTest(
        board: String,
        subject: String,
        chapter: String,
        testType: String,
        difficulty: String,
        numQuestions: Int = 10
    ) {
        val apiKey = geminiApiKey.value
        if (apiKey.isBlank()) {
            aiTestGenResponse.value = "⚠️ Please configure your Gemini API Key in Settings to generate custom test papers."
            return
        }

        viewModelScope.launch {
            isAiTestGenLoading.value = true
            aiTestGenResponse.value = null

            val prompt = """
                Generate a high-yield targeted Test Paper for Class 12 Science:
                Board: $board
                Subject: $subject
                Chapter/Topic: $chapter
                Test Type: $testType
                Difficulty: $difficulty
                Question Count: $numQuestions
                
                Structure the test strictly:
                [TEST PAPER: $subject - $chapter]
                Section A: Multiple Choice Questions (1 Mark each)
                Section B: Short Answer Questions (2 Marks each)
                Section C: Long Derivation / Numerical / Theory (5 Marks each)
                
                ---
                [ANSWER KEY & STEP-BY-STEP SOLUTIONS]
                Provide comprehensive step-by-step marking scheme and correct answers.
            """.trimIndent()

            val result = GeminiClient.askAi(
                apiKey = apiKey,
                prompt = prompt,
                systemInstruction = "You are the chief examiner for BSEB and CBSE Class 12 Science boards."
            )

            isAiTestGenLoading.value = false
            val text = result.getOrElse { "Error: ${it.message}" }
            aiTestGenResponse.value = text

            // Also auto-save as a Generated Test & PDF Document
            if (result.isSuccess) {
                val title = "$subject: $chapter ($testType - $difficulty)"
                repository.saveGeneratedTest(
                    GeneratedTestEntity(
                        title = title,
                        board = board,
                        subject = subject,
                        testType = testType,
                        difficulty = difficulty,
                        totalMarks = 25,
                        timeMinutes = 45,
                        questionsJson = text
                    )
                )
                repository.addCustomPdf(
                    title = "Generated Test: $title",
                    category = "TEST_PAPER",
                    subject = subject,
                    content = text
                )
            }
        }
    }

    // Preferences updates
    fun updateApiKey(key: String) {
        viewModelScope.launch {
            preferencesManager.setGeminiApiKey(key)
            validateApiKey(key)
        }
    }

    fun validateApiKey(key: String) {
        if (key.isBlank()) {
            apiValidationStatus.value = "Empty Key"
            return
        }
        viewModelScope.launch {
            apiValidationStatus.value = "Validating..."
            val result = GeminiClient.askAi(key, "Say 'OK' if you can read this.", "Connection tester")
            if (result.isSuccess) {
                apiValidationStatus.value = "Connected & Active ✅"
            } else {
                apiValidationStatus.value = "Failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun togglePdfBookmark(id: Long, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.togglePdfBookmark(id, isBookmarked)
        }
    }

    fun recordPdfOpened(id: Long) {
        viewModelScope.launch {
            repository.recordPdfOpened(id)
        }
    }

    fun resetToInitialSeed(board: String) {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded(board)
        }
    }

    fun setSelectedBoard(board: String) {
        viewModelScope.launch {
            preferencesManager.setSelectedBoard(board)
            repository.seedInitialDataIfNeeded(board)
        }
    }

    fun toggleLowEnergyMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setLowEnergyMode(enabled)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
            if (enabled) {
                TimelineNotificationHelper.scheduleBlockReminders(getApplication(), timelineBlocks.value)
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    // --- Vault Management Actions ---

    fun uploadPdfToVault(
        uri: android.net.Uri,
        fileName: String,
        subject: String,
        category: String,
        board: String
    ) {
        viewModelScope.launch {
            isVaultUploading.value = true
            vaultUploadStatus.value = "Extracting & Ingesting PDF..."
            val key = geminiApiKey.value

            val result = repository.uploadAndAnalyzePdf(
                uri = uri,
                fileName = fileName,
                subject = subject,
                category = category,
                board = board,
                apiKey = key
            )

            isVaultUploading.value = false
            if (result.isSuccess) {
                val doc = result.getOrNull()
                vaultUploadStatus.value = "Uploaded & Analyzed! (${doc?.questionsCount ?: 0} Questions, ${doc?.patternsCount ?: 0} Patterns)"
            } else {
                vaultUploadStatus.value = "Upload failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun reanalyzeVaultDocument(doc: VaultDocumentEntity) {
        viewModelScope.launch {
            isVaultAnalyzing.value = true
            vaultAnalysisStatus.value = "Analyzing '${doc.fileName}'..."
            val key = geminiApiKey.value
            try {
                val updated = repository.analyzeDocument(doc, key)
                vaultAnalysisStatus.value = "Analysis complete: ${updated.questionsCount} Questions & ${updated.patternsCount} Patterns extracted."
            } catch (e: Exception) {
                vaultAnalysisStatus.value = "Analysis failed: ${e.message}"
            } finally {
                isVaultAnalyzing.value = false
            }
        }
    }

    fun deleteVaultDocument(id: Long) {
        viewModelScope.launch {
            repository.deleteVaultDocument(id)
        }
    }

    fun toggleVaultBookmark(id: Long, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleVaultBookmark(id, isBookmarked)
        }
    }

    fun recordVaultDocOpened(id: Long) {
        viewModelScope.launch {
            repository.recordVaultDocOpened(id)
        }
    }

    fun toggleQuestionImportant(id: Long, isImp: Boolean) {
        viewModelScope.launch {
            repository.toggleQuestionImportant(id, isImp)
        }
    }

    fun deleteQuestion(id: Long) {
        viewModelScope.launch {
            repository.deleteQuestion(id)
        }
    }

    // --- WorkManager Background PDF Processing Engine ---

    fun triggerBackgroundPdfExtraction(docId: Long? = null, forceReanalyze: Boolean = false) {
        PdfExtractionWorkManager.enqueueExtractionWork(
            context = getApplication(),
            targetDocId = docId ?: -1L,
            forceReanalyze = forceReanalyze
        )
    }

    fun cancelBackgroundPdfExtraction() {
        PdfExtractionWorkManager.cancelExtractionWork(getApplication())
    }

    fun schedulePeriodicPdfExtraction(intervalHours: Long = 24) {
        PdfExtractionWorkManager.schedulePeriodicExtraction(getApplication(), intervalHours)
    }

    // --- Grounded AI Tutor (Using real extracted Vault questions and patterns) ---

    fun askGroundedAiTutor(
        topicOrQuestion: String,
        subject: String,
        mode: String = "BILINGUAL",
        type: String = "EXPLAIN"
    ) {
        val apiKey = geminiApiKey.value
        if (apiKey.isBlank()) {
            aiTutorResponse.value = "⚠️ Please configure your Gemini API Key in Settings to enable AI Tutor."
            return
        }

        viewModelScope.launch {
            isAiTutorLoading.value = true
            aiTutorResponse.value = null

            // Find relevant extracted questions and patterns for grounding
            val matchingQuestions = repository.getRandomVaultQuestions(subject, "", 3)
            val matchingPatterns = repository.getPatternsByFilter(subject).firstOrNull()?.take(2) ?: emptyList()

            val groundingContext = StringBuilder()
            if (matchingQuestions.isNotEmpty()) {
                groundingContext.append("\n--- REAL EXTRACTED VAULT QUESTIONS (GROUNDING DATA) ---\n")
                matchingQuestions.forEachIndexed { idx, q ->
                    groundingContext.append("${idx + 1}. [${q.questionType} - ${q.marks}M - ${q.difficulty}] Chapter: ${q.chapterName} -> ${q.questionText}\n")
                    if (q.stepByStepSolution.isNotBlank()) {
                        groundingContext.append("   Solution: ${q.stepByStepSolution.take(200)}\n")
                    }
                }
            }
            if (matchingPatterns.isNotEmpty()) {
                groundingContext.append("\n--- EXTRACTED BOARD EXAM PATTERNS ---\n")
                matchingPatterns.forEach { p ->
                    groundingContext.append("• ${p.title} (${p.patternType}, ${p.weightagePercentage}% weightage): ${p.examTip}\n")
                }
            }

            val languagePrompt = when (mode) {
                "HINDI" -> "Explain strictly in clear, structured Hindi (Devanagari script) with technical terms."
                "ENGLISH" -> "Explain strictly in concise, precise English."
                else -> "Explain in clear Hinglish (Conversational Hindi + English technical terms) as used by top board educators."
            }

            val prompt = """
                Subject: $subject
                Task Type: $type
                Student Query / Problem: $topicOrQuestion
                
                GROUNDING KNOWLEDGE BASE FROM UPLOADED VAULT:
                $groundingContext
                
                Guidelines:
                - $languagePrompt
                - Synthesize the answer using the exact questions and exam patterns extracted from the user's Resource Vault above.
                - Structure:
                  1. 📌 Core Concept & Formula (SI units included)
                  2. 🧠 Step-by-Step Explanation / Proof
                  3. ⚠️ Board Trap Warning & Examiner Marking Tip (referencing real patterns)
                  4. 🎯 Practice Question from Vault with Step-by-Step Marking Breakdown
                - Keep tone authoritative, encouraging, and zero fluff.
            """.trimIndent()

            val result = GeminiClient.askAi(
                apiKey = apiKey,
                prompt = prompt,
                systemInstruction = "You are Rudra AI Master Tutor, grounded directly in the student's personal Class 12 Science Resource Vault and Question Database."
            )

            isAiTutorLoading.value = false
            aiTutorResponse.value = result.getOrElse { "Error: ${it.message}" }
        }
    }

    // =========================================================================
    // --- EXAMINATION INTELLIGENCE ENGINE & TEST CONTROLLER ---
    // =========================================================================

    /**
     * Conducts deep pre-generation analysis and generates authentic board examination
     */
    fun generateRealisticExamination(
        board: String,
        subject: String,
        unit: String = "",
        chapter: String = "",
        topic: String = "",
        testMode: String = "MOCK_EXAM",
        difficulty: String = "Medium",
        questionCount: Int = 15,
        questionTypes: String = "ALL", // "ALL", "MCQ_ONLY", "SHORT_LONG", "DERIVATION_ONLY"
        timeLimitMinutes: Int = 45,
        outputFormat: String = "QUIZ_MODE", // "QUIZ_MODE", "PDF_EXAM_MODE"
        useAiEnhancement: Boolean = true
    ) {
        viewModelScope.launch {
            isAiTestGenLoading.value = true
            aiTestGenResponse.value = null

            val targetScope = when {
                topic.isNotBlank() -> "$chapter: $topic"
                chapter.isNotBlank() -> chapter
                unit.isNotBlank() -> unit
                else -> ""
            }

            // 1. Fetch live contextual data from DB
            val userWeak = weakTopics.value
            val vaultQList = repository.getRandomVaultQuestions(subject, chapter, 20)
            val vaultPList = repository.getPatternsByFilter(subject, chapter).firstOrNull() ?: emptyList()
            val pyqs = pyqList.value

            // 2. Run Pre-Generation Analysis Pipeline
            val analysisReport = ExamIntelligenceEngine.performPreGenerationAnalysis(
                board = board,
                subject = subject,
                testMode = testMode,
                targetChapterOrUnit = targetScope,
                difficulty = difficulty,
                questionCount = questionCount,
                userWeakTopics = userWeak,
                vaultQuestions = vaultQList,
                vaultPatterns = vaultPList
            )
            activePreAnalysisReport.value = analysisReport

            // 3. Compile Realistic Board Exam Bundle
            var examBundle = ExamIntelligenceEngine.compileRealisticBoardExam(
                board = board,
                subject = subject,
                testMode = testMode,
                targetChapterOrUnit = targetScope,
                difficulty = difficulty,
                questionCount = questionCount,
                analysis = analysisReport,
                vaultQuestions = vaultQList,
                vaultPatterns = vaultPList,
                pyqBank = pyqs
            )

            // 4. If AI enhancement requested and API key present, refine questions
            val apiKey = geminiApiKey.value
            if (useAiEnhancement && apiKey.isNotBlank()) {
                try {
                    val prompt = """
                        You are the Senior Chief Examiner for $board Class 12 $subject.
                        Analyze the following pre-calculated exam paper and enrich with board examiner tips, precise step-by-step marking schemes, and strict board difficulty calibration.
                        
                        Board: $board
                        Subject: $subject
                        Scope: $targetScope
                        Mode: $testMode
                        Difficulty: $difficulty
                        Question Count: ${examBundle.items.size}
                        
                        CURRENT COMPILED QUESTIONS:
                        ${examBundle.items.joinToString("\n") { "Q${it.questionNumber}. [${it.questionType} - ${it.marks}M] ${it.questionText}" }}
                        
                        Output a polished, authentic Examination Paper with complete Section A, Section B, Section C, and Step-by-Step Marking Scheme.
                    """.trimIndent()

                    val aiResult = GeminiClient.askAi(
                        apiKey = apiKey,
                        prompt = prompt,
                        systemInstruction = "You are the official Chief Examiner for $board Class 12 Science Board Examination."
                    )
                    if (aiResult.isSuccess) {
                        aiTestGenResponse.value = aiResult.getOrNull()
                    }
                } catch (ignored: Exception) {}
            }

            lastGeneratedBundle.value = examBundle

            // 5. Persist Generated Test to Room
            val testEntity = GeneratedTestEntity(
                title = examBundle.title,
                board = board,
                subject = subject,
                unit = unit,
                chapter = chapter,
                topic = topic,
                testType = testMode,
                difficulty = difficulty,
                totalMarks = examBundle.totalMarks,
                timeMinutes = examBundle.timeMinutes,
                questionsJson = serializeTestItemsJson(examBundle.items),
                questionPaperMarkdown = examBundle.questionPaperMarkdown,
                answerKeyMarkdown = examBundle.answerKeyMarkdown,
                solutionMarkdown = examBundle.solutionMarkdown,
                analysisSummaryJson = serializeAnalysisReport(analysisReport)
            )
            val savedTestId = repository.insertGeneratedTest(testEntity)

            // Also register in PDF documents
            repository.addCustomPdf(
                title = examBundle.title,
                category = "TEST_PAPER",
                subject = subject,
                content = "${examBundle.questionPaperMarkdown}\n\n${examBundle.solutionMarkdown}"
            )

            isAiTestGenLoading.value = false

            // 6. Launch into selected format
            if (outputFormat == "PDF_EXAM_MODE") {
                openExamPaperView(examBundle, savedTestId, initialTab = 0)
            } else {
                startInteractiveQuiz(examBundle, savedTestId)
            }
        }
    }

    // --- Interactive Quiz Controller ---

    fun startInteractiveQuiz(bundle: GeneratedExamBundle, testId: Long = 0L) {
        activeQuizState.value = InteractiveQuizState(
            testId = testId,
            title = bundle.title,
            board = bundle.board,
            subject = bundle.subject,
            testMode = bundle.testMode,
            difficulty = bundle.difficulty,
            totalMarks = bundle.totalMarks,
            timeLimitMinutes = bundle.timeMinutes,
            items = bundle.items,
            currentQuestionIndex = 0,
            userAnswers = emptyMap(),
            bookmarkedQuestionIndices = emptySet(),
            timeRemainingSeconds = bundle.timeMinutes * 60,
            isSubmitted = false,
            isReviewMode = false
        )
    }

    fun selectQuizAnswer(questionIndex: Int, answer: String) {
        val current = activeQuizState.value ?: return
        val updatedAnswers = current.userAnswers.toMutableMap()
        updatedAnswers[questionIndex] = answer
        activeQuizState.value = current.copy(userAnswers = updatedAnswers)
    }

    fun jumpToQuizQuestion(index: Int) {
        val current = activeQuizState.value ?: return
        if (index in current.items.indices) {
            activeQuizState.value = current.copy(currentQuestionIndex = index)
        }
    }

    fun toggleQuizBookmark(index: Int) {
        val current = activeQuizState.value ?: return
        val updated = current.bookmarkedQuestionIndices.toMutableSet()
        if (index in updated) updated.remove(index) else updated.add(index)
        activeQuizState.value = current.copy(bookmarkedQuestionIndices = updated)
    }

    fun updateQuizTimeRemaining(seconds: Int) {
        val current = activeQuizState.value ?: return
        activeQuizState.value = current.copy(timeRemainingSeconds = seconds)
    }

    fun submitInteractiveQuiz() {
        val current = activeQuizState.value ?: return
        if (current.isSubmitted) return

        viewModelScope.launch {
            var correctCount = 0
            var incorrectCount = 0
            var skippedCount = 0
            var scoredMarks = 0
            val weakTopics = mutableListOf<String>()
            val strongTopics = mutableListOf<String>()

            current.items.forEachIndexed { idx, q ->
                val userAns = current.userAnswers[idx]?.trim()
                if (userAns.isNullOrBlank()) {
                    skippedCount++
                } else {
                    val isCorrect = if (q.questionType == "MCQ") {
                        userAns.equals(q.correctAnswer, ignoreCase = true) ||
                        q.correctAnswer.startsWith(userAns.take(3), ignoreCase = true) ||
                        userAns.startsWith(q.correctAnswer.take(3), ignoreCase = true)
                    } else {
                        // For subjective/numerical, grant marks if user attempted
                        userAns.isNotBlank() && userAns.length > 5
                    }

                    if (isCorrect) {
                        correctCount++
                        scoredMarks += q.marks
                        if (q.topicName.isNotBlank() && q.topicName !in strongTopics) {
                            strongTopics.add(q.topicName)
                        }
                    } else {
                        incorrectCount++
                        if (q.topicName.isNotBlank() && q.topicName !in weakTopics) {
                            weakTopics.add(q.topicName)
                        }
                    }
                }
            }

            val totalQ = current.items.size
            val attempted = correctCount + incorrectCount
            val accuracy = if (attempted > 0) (correctCount.toFloat() / attempted.toFloat()) * 100f else 0f
            val timeTakenSec = (current.timeLimitMinutes * 60) - current.timeRemainingSeconds.coerceAtLeast(0)

            // Examiner Recommendations
            val suggestions = when {
                accuracy >= 85f -> "Outstanding command! Strengthen final decimal accuracy and write SI units with all final formulas to secure a 100% board score."
                accuracy >= 60f -> "Solid preparation. Focus on high-yield derivations and avoid skipping standard definitions in Section B short answers."
                else -> "Significant gaps detected in core concepts. Immediate spaced repetition revision needed for identified weak chapters."
            }

            val revisionRec = if (weakTopics.isNotEmpty()) {
                "Schedule spaced repetition for: ${weakTopics.take(3).joinToString(", ")}. Review PYQ derivations from 2021-2025."
            } else {
                "Proceed to full-syllabus 70-mark timed mock examinations."
            }

            val nextTestRec = if (weakTopics.isNotEmpty()) {
                "Targeted Drill: 10 Questions on ${weakTopics.first()}"
            } else {
                "Full-Length Board Examination: 70 Marks Mock Exam"
            }

            // Save Attempt Record
            val attemptEntity = TestAttemptEntity(
                testId = current.testId,
                testTitle = current.title,
                board = current.board,
                subject = current.subject,
                testMode = current.testMode,
                difficulty = current.difficulty,
                totalQuestions = totalQ,
                attemptedCount = attempted,
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                skippedCount = skippedCount,
                totalMarks = current.totalMarks,
                scoredMarks = scoredMarks,
                accuracyPercentage = accuracy,
                timeTakenSeconds = timeTakenSec.coerceAtLeast(30),
                timeLimitMinutes = current.timeLimitMinutes,
                weakTopicsJson = JSONArray(weakTopics).toString(),
                strongTopicsJson = JSONArray(strongTopics).toString(),
                improvementSuggestions = suggestions,
                revisionRecommendations = revisionRec,
                nextTestRecommendation = nextTestRec,
                userAnswersJson = JSONObject(current.userAnswers.mapKeys { it.key.toString() }).toString()
            )

            val attemptId = repository.recordTestAttempt(attemptEntity)
            lastCompletedAttempt.value = attemptEntity.copy(id = attemptId)

            activeQuizState.value = current.copy(
                isSubmitted = true,
                isReviewMode = true
            )
        }
    }

    fun retryIncorrectQuizQuestions() {
        val current = activeQuizState.value ?: return
        val incorrectIndices = current.items.indices.filter { idx ->
            val ans = current.userAnswers[idx]
            ans.isNullOrBlank() || !ans.equals(current.items[idx].correctAnswer, ignoreCase = true)
        }

        if (incorrectIndices.isEmpty()) return

        val retryItems = incorrectIndices.mapIndexed { newIdx, oldIdx ->
            current.items[oldIdx].copy(questionNumber = newIdx + 1)
        }

        val retryBundle = GeneratedExamBundle(
            title = "Retry Drill: ${current.title} [${retryItems.size} Missed Questions]",
            board = current.board,
            subject = current.subject,
            testMode = "WEAK_TOPICS",
            difficulty = current.difficulty,
            totalMarks = retryItems.sumOf { it.marks },
            timeMinutes = (retryItems.size * 2).coerceAtLeast(10),
            analysisReport = activePreAnalysisReport.value ?: ExamIntelligenceEngine.performPreGenerationAnalysis(
                board = current.board,
                subject = current.subject,
                testMode = "WEAK_TOPICS",
                targetChapterOrUnit = "",
                difficulty = current.difficulty,
                questionCount = retryItems.size,
                userWeakTopics = weakTopics.value,
                vaultQuestions = emptyList(),
                vaultPatterns = emptyList()
            ),
            items = retryItems,
            questionPaperMarkdown = ExamIntelligenceEngine.buildBoardQuestionPaperMarkdown(
                current.board, current.subject, "WEAK_TOPICS", activePreAnalysisReport.value ?: ExamIntelligenceEngine.performPreGenerationAnalysis(
                    board = current.board,
                    subject = current.subject,
                    testMode = "WEAK_TOPICS",
                    targetChapterOrUnit = "",
                    difficulty = current.difficulty,
                    questionCount = retryItems.size,
                    userWeakTopics = weakTopics.value,
                    vaultQuestions = emptyList(),
                    vaultPatterns = emptyList()
                ), retryItems
            ),
            answerKeyMarkdown = ExamIntelligenceEngine.buildBoardAnswerKeyMarkdown(current.board, current.subject, retryItems),
            solutionMarkdown = ExamIntelligenceEngine.buildBoardSolutionMarkdown(current.board, current.subject, retryItems)
        )

        startInteractiveQuiz(retryBundle, current.testId)
    }

    fun closeInteractiveQuiz() {
        activeQuizState.value = null
    }

    // --- PDF Exam Paper Viewer Controller ---

    fun openExamPaperView(bundle: GeneratedExamBundle, testId: Long = 0L, initialTab: Int = 0) {
        activePaperState.value = ExamPaperViewState(
            testId = testId,
            title = bundle.title,
            board = bundle.board,
            subject = bundle.subject,
            testMode = bundle.testMode,
            totalMarks = bundle.totalMarks,
            timeMinutes = bundle.timeMinutes,
            questionPaperMarkdown = bundle.questionPaperMarkdown,
            answerKeyMarkdown = bundle.answerKeyMarkdown,
            solutionMarkdown = bundle.solutionMarkdown,
            analysisReport = bundle.analysisReport,
            activeTab = initialTab,
            zoomLevel = 1.0f,
            searchQuery = "",
            isBookmarked = false
        )
    }

    fun openSavedTestAsExamPaper(test: GeneratedTestEntity, initialTab: Int = 0) {
        val parsedItems = parseTestItemsJson(test.questionsJson)
        val qpMd = if (test.questionPaperMarkdown.isNotBlank()) test.questionPaperMarkdown else test.questionsJson
        val akMd = if (test.answerKeyMarkdown.isNotBlank()) test.answerKeyMarkdown else ExamIntelligenceEngine.buildBoardAnswerKeyMarkdown(test.board, test.subject, parsedItems)
        val solMd = if (test.solutionMarkdown.isNotBlank()) test.solutionMarkdown else ExamIntelligenceEngine.buildBoardSolutionMarkdown(test.board, test.subject, parsedItems)

        activePaperState.value = ExamPaperViewState(
            testId = test.id,
            title = test.title,
            board = test.board,
            subject = test.subject,
            testMode = test.testType,
            totalMarks = test.totalMarks,
            timeMinutes = test.timeMinutes,
            questionPaperMarkdown = qpMd,
            answerKeyMarkdown = akMd,
            solutionMarkdown = solMd,
            analysisReport = parseAnalysisReportJson(test.analysisSummaryJson, test.board, test.subject),
            activeTab = initialTab,
            zoomLevel = 1.0f,
            searchQuery = "",
            isBookmarked = false
        )
    }

    fun openSavedTestAsQuiz(test: GeneratedTestEntity) {
        var items = parseTestItemsJson(test.questionsJson)
        if (items.isEmpty()) {
            // Generate fallback items from stored text
            items = listOf(
                TestItemData(
                    id = "saved_q_1",
                    section = "Section A: Practice Questions",
                    questionNumber = 1,
                    questionText = test.questionsJson.take(300),
                    questionType = "SHORT",
                    correctAnswer = "Consult official solution",
                    stepByStepSolution = test.solutionMarkdown.ifBlank { test.questionsJson },
                    marks = test.totalMarks,
                    difficulty = test.difficulty,
                    topicName = test.chapter.ifBlank { test.subject },
                    chapterName = test.chapter.ifBlank { test.subject },
                    subject = test.subject,
                    sourceType = "PYQ_PATTERN (60%)"
                )
            )
        }

        val bundle = GeneratedExamBundle(
            title = test.title,
            board = test.board,
            subject = test.subject,
            testMode = test.testType,
            difficulty = test.difficulty,
            totalMarks = test.totalMarks,
            timeMinutes = test.timeMinutes,
            analysisReport = parseAnalysisReportJson(test.analysisSummaryJson, test.board, test.subject),
            items = items,
            questionPaperMarkdown = test.questionPaperMarkdown.ifBlank { test.questionsJson },
            answerKeyMarkdown = test.answerKeyMarkdown,
            solutionMarkdown = test.solutionMarkdown
        )

        startInteractiveQuiz(bundle, test.id)
    }

    fun closeExamPaperView() {
        activePaperState.value = null
    }

    fun setPaperActiveTab(tab: Int) {
        val current = activePaperState.value ?: return
        activePaperState.value = current.copy(activeTab = tab)
    }

    fun updatePaperZoom(delta: Float) {
        val current = activePaperState.value ?: return
        val newZoom = (current.zoomLevel + delta).coerceIn(0.7f, 2.0f)
        activePaperState.value = current.copy(zoomLevel = newZoom)
    }

    fun updatePaperSearchQuery(query: String) {
        val current = activePaperState.value ?: return
        activePaperState.value = current.copy(searchQuery = query)
    }

    fun deleteGeneratedTest(id: Long) {
        viewModelScope.launch {
            repository.deleteGeneratedTest(id)
        }
    }

    // --- JSON Serialization Helpers ---
    private fun serializeTestItemsJson(items: List<TestItemData>): String {
        val arr = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("section", item.section)
            obj.put("questionNumber", item.questionNumber)
            obj.put("questionText", item.questionText)
            obj.put("questionType", item.questionType)
            obj.put("options", JSONArray(item.options))
            obj.put("correctAnswer", item.correctAnswer)
            obj.put("stepByStepSolution", item.stepByStepSolution)
            obj.put("marks", item.marks)
            obj.put("difficulty", item.difficulty)
            obj.put("topicName", item.topicName)
            obj.put("chapterName", item.chapterName)
            obj.put("subject", item.subject)
            obj.put("sourceType", item.sourceType)
            obj.put("frequencyTag", item.frequencyTag)
            obj.put("estimatedTimeSeconds", item.estimatedTimeSeconds)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseTestItemsJson(jsonStr: String): List<TestItemData> {
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<TestItemData>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val optsArr = obj.optJSONArray("options")
                val opts = mutableListOf<String>()
                if (optsArr != null) {
                    for (j in 0 until optsArr.length()) {
                        opts.add(optsArr.getString(j))
                    }
                }
                list.add(
                    TestItemData(
                        id = obj.optString("id", "q_$i"),
                        section = obj.optString("section", "Section A"),
                        questionNumber = obj.optInt("questionNumber", i + 1),
                        questionText = obj.optString("questionText", ""),
                        questionType = obj.optString("questionType", "MCQ"),
                        options = opts,
                        correctAnswer = obj.optString("correctAnswer", ""),
                        stepByStepSolution = obj.optString("stepByStepSolution", ""),
                        marks = obj.optInt("marks", 1),
                        difficulty = obj.optString("difficulty", "Medium"),
                        topicName = obj.optString("topicName", ""),
                        chapterName = obj.optString("chapterName", ""),
                        subject = obj.optString("subject", "Science"),
                        sourceType = obj.optString("sourceType", "PYQ_PATTERN (60%)"),
                        frequencyTag = obj.optString("frequencyTag", ""),
                        estimatedTimeSeconds = obj.optInt("estimatedTimeSeconds", 120)
                    )
                )
            }
        } catch (e: Exception) {}
        return list
    }

    private fun serializeAnalysisReport(report: ExamAnalysisReport): String {
        val obj = JSONObject()
        obj.put("board", report.board)
        obj.put("subject", report.subject)
        obj.put("testMode", report.testMode)
        obj.put("targetScope", report.targetScope)
        obj.put("totalMarks", report.totalMarks)
        obj.put("timeMinutes", report.timeMinutes)
        obj.put("pyqMixPercent", report.pyqMixPercent)
        obj.put("modelPaperMixPercent", report.modelPaperMixPercent)
        obj.put("freshMixPercent", report.freshMixPercent)
        obj.put("examinerNotes", report.examinerNotes)
        return obj.toString()
    }

    private fun parseAnalysisReportJson(jsonStr: String, defaultBoard: String, defaultSubject: String): ExamAnalysisReport {
        if (jsonStr.isNotBlank()) {
            try {
                val obj = JSONObject(jsonStr)
                return ExamAnalysisReport(
                    board = obj.optString("board", defaultBoard),
                    subject = obj.optString("subject", defaultSubject),
                    testMode = obj.optString("testMode", "MOCK_EXAM"),
                    targetScope = obj.optString("targetScope", "Full Syllabus"),
                    totalMarks = obj.optInt("totalMarks", 25),
                    timeMinutes = obj.optInt("timeMinutes", 45),
                    pyqMixPercent = obj.optInt("pyqMixPercent", 60),
                    modelPaperMixPercent = obj.optInt("modelPaperMixPercent", 20),
                    freshMixPercent = obj.optInt("freshMixPercent", 20),
                    unitWeightageList = ExamIntelligenceEngine.physicsUnitWeightage,
                    highProbabilityTopics = listOf("Gauss's Law", "Lens Maker's Formula", "Transformer", "Photoelectric Effect"),
                    repeatedPyqTopics = ExamIntelligenceEngine.fiveYearFrequencies[defaultSubject] ?: emptyList(),
                    weakTopicsIncluded = emptyList(),
                    difficultyDistribution = mapOf("Beginner" to 30, "Medium" to 50, "Advanced" to 20),
                    examinerNotes = obj.optString("examinerNotes", "Official Board Examination Pattern")
                )
            } catch (e: Exception) {}
        }
        return ExamIntelligenceEngine.performPreGenerationAnalysis(
            board = defaultBoard,
            subject = defaultSubject,
            testMode = "MOCK_EXAM",
            targetChapterOrUnit = "",
            difficulty = "Medium",
            questionCount = 15,
            userWeakTopics = emptyList(),
            vaultQuestions = emptyList(),
            vaultPatterns = emptyList()
        )
    }
}


