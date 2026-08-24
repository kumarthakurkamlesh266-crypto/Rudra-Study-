package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.*
import com.example.data.pyq.PyqData
import com.example.data.syllabus.PreloadedPdfsData
import com.example.data.syllabus.SyllabusData
import com.example.data.timeline.DefaultTimelineData
import com.example.data.vault.PdfAnalysisEngine
import com.example.data.vault.PdfVaultManager
import com.example.data.vault.PreloadedVaultData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RudraRepository(
    private val database: RudraDatabase,
    private val context: Context
) {
    private val timelineDao = database.timelineDao()
    private val topicDao = database.topicProgressDao()
    private val revisionDao = database.revisionTaskDao()
    private val scorecardDao = database.scorecardDao()
    private val pyqDao = database.pyqDao()
    private val focusSessionDao = database.focusSessionDao()
    private val generatedTestDao = database.generatedTestDao()
    private val pdfDao = database.pdfDao()
    private val vaultDocDao = database.vaultDocumentDao()
    private val chapterDao = database.chapterDao()
    private val questionDao = database.questionDao()
    private val patternDao = database.patternDao()
    private val testAttemptDao = database.testAttemptDao()
    private val aiChatDao = database.aiChatDao()

    val allTimelineBlocks: Flow<List<TimelineBlockEntity>> = timelineDao.getAllBlocks()
    val allTopicProgress: Flow<List<TopicProgressEntity>> = topicDao.getAllProgress()
    val pendingRevisionTasks: Flow<List<RevisionTaskEntity>> = revisionDao.getPendingTasks()
    val allRevisionTasks: Flow<List<RevisionTaskEntity>> = revisionDao.getAllTasks()
    val allScorecards: Flow<List<DailyScorecardEntity>> = scorecardDao.getAllScorecards()
    val pastWeekScorecards: Flow<List<DailyScorecardEntity>> = scorecardDao.getPastWeekScorecards()
    val allPyqs: Flow<List<PyqEntity>> = pyqDao.getAllPyqs()
    val allFocusSessions: Flow<List<FocusSessionEntity>> = focusSessionDao.getAllSessions()
    val allGeneratedTests: Flow<List<GeneratedTestEntity>> = generatedTestDao.getAllTests()
    val allTestAttempts: Flow<List<TestAttemptEntity>> = testAttemptDao.getAllAttempts()
    val latestTestAttempt: Flow<TestAttemptEntity?> = testAttemptDao.getLatestAttempt()
    val allPdfs: Flow<List<PdfDocumentEntity>> = pdfDao.getAllPdfs()
    val weakTopics: Flow<List<TopicProgressEntity>> = topicDao.getWeakTopics()

    // Resource Vault & Question/Chapter/Pattern Databases
    val allVaultDocuments: Flow<List<VaultDocumentEntity>> = vaultDocDao.getAllDocuments()
    val vaultStorageBytes: Flow<Long?> = vaultDocDao.getTotalStorageBytes()
    val vaultDocsCount: Flow<Int> = vaultDocDao.getTotalDocumentsCount()
    val allChapters: Flow<List<ChapterEntity>> = chapterDao.getAllChapters()
    val totalChaptersCount: Flow<Int> = chapterDao.getTotalChaptersCount()
    val allQuestions: Flow<List<QuestionEntity>> = questionDao.getAllQuestions()
    val totalQuestionsCount: Flow<Int> = questionDao.getTotalQuestionsCount()
    val allPatterns: Flow<List<PatternEntity>> = patternDao.getAllPatterns()
    val totalPatternsCount: Flow<Int> = patternDao.getTotalPatternsCount()

    suspend fun seedInitialDataIfNeeded(board: String = "BSEB") = withContext(Dispatchers.IO) {
        val existingBlocks = timelineDao.getAllBlocksList()
        if (existingBlocks.isEmpty()) {
            timelineDao.insertBlocks(DefaultTimelineData.getMasterTimeline())
        }

        val defaultTopics = SyllabusData.getDefaultSyllabus(board)
        topicDao.insertTopics(defaultTopics)

        pyqDao.insertPyqs(PyqData.getDefaultPyqs())
        pdfDao.insertPdfs(PreloadedPdfsData.getDefaultDocuments())

        // Seed initial Vault Documents, Question Bank, and Pattern Bank
        val vaultDocs = PreloadedVaultData.getDefaultVaultDocuments()
        vaultDocDao.insertDocuments(vaultDocs)
        questionDao.insertQuestions(PreloadedVaultData.getDefaultQuestions())
        patternDao.insertPatterns(PreloadedVaultData.getDefaultPatterns())

        // Seed today's scorecard if missing
        val todayStr = getTodayDateString()
        if (scorecardDao.getScorecardByDate(todayStr) == null) {
            scorecardDao.insertScorecard(DailyScorecardEntity(dateString = todayStr))
        }
    }

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getTodayScorecardFlow(): Flow<DailyScorecardEntity?> {
        return scorecardDao.getScorecardByDateFlow(getTodayDateString())
    }

    suspend fun updateScorecard(scorecard: DailyScorecardEntity) = withContext(Dispatchers.IO) {
        scorecardDao.insertScorecard(scorecard)
    }

    // Timeline Block Management
    suspend fun insertBlock(block: TimelineBlockEntity) = withContext(Dispatchers.IO) {
        timelineDao.insertBlock(block)
    }

    suspend fun updateBlock(block: TimelineBlockEntity) = withContext(Dispatchers.IO) {
        timelineDao.updateBlock(block)
    }

    suspend fun deleteBlock(id: Long) = withContext(Dispatchers.IO) {
        timelineDao.deleteBlockById(id)
    }

    suspend fun setBlockCompletedToday(id: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        timelineDao.setCompletedToday(id, completed)
    }

    suspend fun resetTimelineToDefault() = withContext(Dispatchers.IO) {
        timelineDao.insertBlocks(DefaultTimelineData.getMasterTimeline())
    }

    // Syllabus Management
    suspend fun updateTopicProgress(
        topicId: String,
        status: String,
        completion: Int,
        isWeak: Boolean,
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        val topic = topicDao.getTopicById(topicId) ?: return@withContext
        val updated = topic.copy(
            status = status,
            completionPercent = completion,
            isWeakTopic = isWeak,
            notes = notes,
            lastRevisedTimestamp = if (status == "REVISED" || status == "MASTERED") System.currentTimeMillis() else topic.lastRevisedTimestamp,
            revisionCount = if (status == "REVISED" || status == "MASTERED") topic.revisionCount + 1 else topic.revisionCount
        )
        topicDao.updateTopic(updated)

        // Automatically trigger spaced repetition task generation if revised or mastered
        if (status == "REVISED" || status == "MASTERED" || status == "LEARNING") {
            createSpacedRepetitionTasks(updated)
        }
    }

    private suspend fun createSpacedRepetitionTasks(topic: TopicProgressEntity) {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        val tasks = listOf(
            RevisionTaskEntity(
                topicId = topic.topicId,
                topicName = topic.topicName,
                subject = topic.subject,
                chapterName = topic.chapterName,
                intervalType = "SAME_DAY",
                dueDateTimestamp = now + 4 * 3600000L // 4 hours later same evening
            ),
            RevisionTaskEntity(
                topicId = topic.topicId,
                topicName = topic.topicName,
                subject = topic.subject,
                chapterName = topic.chapterName,
                intervalType = "PLUS_1",
                dueDateTimestamp = now + oneDayMillis
            ),
            RevisionTaskEntity(
                topicId = topic.topicId,
                topicName = topic.topicName,
                subject = topic.subject,
                chapterName = topic.chapterName,
                intervalType = "PLUS_3",
                dueDateTimestamp = now + (3 * oneDayMillis)
            ),
            RevisionTaskEntity(
                topicId = topic.topicId,
                topicName = topic.topicName,
                subject = topic.subject,
                chapterName = topic.chapterName,
                intervalType = "PLUS_7",
                dueDateTimestamp = now + (7 * oneDayMillis)
            )
        )
        revisionDao.insertTasks(tasks)
    }

    suspend fun setTopicStatusQuick(topicId: String, status: String) = withContext(Dispatchers.IO) {
        val completion = when (status) {
            "COMPLETED", "MASTERED" -> 100
            "LEARNING", "IN_PROGRESS" -> 50
            else -> 0
        }
        val topic = topicDao.getTopicById(topicId) ?: return@withContext
        val updated = topic.copy(
            status = status,
            completionPercent = completion,
            lastRevisedTimestamp = if (status == "COMPLETED" || status == "MASTERED") System.currentTimeMillis() else topic.lastRevisedTimestamp
        )
        topicDao.updateTopic(updated)
    }

    suspend fun cycleTopicStatus(topicId: String) = withContext(Dispatchers.IO) {
        val topic = topicDao.getTopicById(topicId) ?: return@withContext
        val (nextStatus, nextCompletion) = when (topic.status) {
            "NOT_STARTED" -> "LEARNING" to 50
            "LEARNING", "IN_PROGRESS" -> "COMPLETED" to 100
            else -> "NOT_STARTED" to 0
        }
        val updated = topic.copy(
            status = nextStatus,
            completionPercent = nextCompletion,
            lastRevisedTimestamp = if (nextStatus == "COMPLETED") System.currentTimeMillis() else topic.lastRevisedTimestamp
        )
        topicDao.updateTopic(updated)
    }

    suspend fun markChapterComplete(subject: String, chapterName: String) = withContext(Dispatchers.IO) {
        topicDao.markChapterStatus(subject, chapterName, "COMPLETED", 100)
    }

    suspend fun markChapterStatus(subject: String, chapterName: String, status: String) = withContext(Dispatchers.IO) {
        val completion = when (status) {
            "COMPLETED", "MASTERED" -> 100
            "LEARNING", "IN_PROGRESS" -> 50
            else -> 0
        }
        topicDao.markChapterStatus(subject, chapterName, status, completion)
    }

    suspend fun completeRevisionTask(taskId: Long, topicId: String) = withContext(Dispatchers.IO) {
        revisionDao.markTaskCompleted(taskId, System.currentTimeMillis())
        val topic = topicDao.getTopicById(topicId)
        if (topic != null) {
            topicDao.updateTopic(
                topic.copy(
                    revisionCount = topic.revisionCount + 1,
                    lastRevisedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Focus Session
    suspend fun logFocusSession(
        subject: String,
        topic: String,
        durationMinutes: Int,
        thoughts: String,
        rating: Int
    ) = withContext(Dispatchers.IO) {
        focusSessionDao.insertSession(
            FocusSessionEntity(
                subject = subject,
                topicOrTask = topic,
                durationMinutes = durationMinutes,
                parkingLotThoughts = thoughts,
                qualityRating = rating
            )
        )

        // Also update today's scorecard study minutes
        val today = getTodayDateString()
        val currentScorecard = scorecardDao.getScorecardByDate(today) ?: DailyScorecardEntity(dateString = today)
        scorecardDao.insertScorecard(
            currentScorecard.copy(
                studyHoursMinutes = currentScorecard.studyHoursMinutes + durationMinutes
            )
        )
    }

    // Test Management
    suspend fun saveGeneratedTest(test: GeneratedTestEntity): Long = withContext(Dispatchers.IO) {
        generatedTestDao.insertTest(test)
    }

    suspend fun updateTestScore(testId: Long, score: Int) = withContext(Dispatchers.IO) {
        val test = generatedTestDao.getTestById(testId) ?: return@withContext
        generatedTestDao.updateTest(test.copy(completedTimestamp = System.currentTimeMillis(), userScore = score))
    }

    // PDF Management
    suspend fun togglePdfBookmark(id: Long, bookmarked: Boolean) = withContext(Dispatchers.IO) {
        pdfDao.setBookmarked(id, bookmarked)
    }

    suspend fun recordPdfOpened(id: Long) = withContext(Dispatchers.IO) {
        pdfDao.updateLastOpened(id, System.currentTimeMillis())
    }

    suspend fun addCustomPdf(title: String, category: String, subject: String, content: String) = withContext(Dispatchers.IO) {
        pdfDao.insertPdf(
            PdfDocumentEntity(
                title = title,
                category = category,
                subject = subject,
                description = "User generated document / test export",
                contentMarkdown = content,
                isBookmarked = false,
                lastOpenedTimestamp = System.currentTimeMillis()
            )
        )
    }

    // --- PDF Vault Upload and Analysis Pipeline ---

    suspend fun uploadAndAnalyzePdf(
        uri: Uri,
        fileName: String,
        subject: String,
        category: String,
        board: String,
        apiKey: String
    ): Result<VaultDocumentEntity> = withContext(Dispatchers.IO) {
        try {
            val saveResult = PdfVaultManager.savePdfToVault(context, uri, fileName)
            val savedFile = saveResult.getOrThrow()

            // Create Vault Document Entity
            val docEntity = VaultDocumentEntity(
                fileName = savedFile.fileName,
                title = savedFile.fileName.removeSuffix(".pdf").replace("_", " "),
                fileUriOrPath = savedFile.localPath,
                fileSizeBytes = savedFile.fileSizeBytes,
                pageCount = savedFile.pageCount,
                subject = subject,
                category = category,
                board = board,
                uploadTimestamp = System.currentTimeMillis(),
                extractedText = savedFile.extractedText,
                isAnalyzed = false
            )

            val insertedId = vaultDocDao.insertDocument(docEntity)
            val persistedDoc = docEntity.copy(id = insertedId)

            // Trigger Automatic Analysis
            val analyzed = analyzeDocument(persistedDoc, apiKey)

            Result.success(analyzed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeDocument(document: VaultDocumentEntity, apiKey: String): VaultDocumentEntity = withContext(Dispatchers.IO) {
        val analysis = PdfAnalysisEngine.analyzeDocument(document, document.extractedText, apiKey)

        // Clear any old questions/patterns for this doc
        questionDao.deleteQuestionsByDocId(document.id)
        patternDao.deletePatternsByDocId(document.id)

        // Insert newly parsed questions and patterns
        if (analysis.questions.isNotEmpty()) {
            questionDao.insertQuestions(analysis.questions)
        }
        if (analysis.patterns.isNotEmpty()) {
            patternDao.insertPatterns(analysis.patterns)
        }

        // Update document status in Room
        vaultDocDao.updateAnalyzedData(
            id = document.id,
            isAnalyzed = true,
            summary = analysis.summary,
            qCount = analysis.questions.size,
            pCount = analysis.patterns.size,
            extractedText = document.extractedText
        )

        document.copy(
            isAnalyzed = true,
            analyzedSummary = analysis.summary,
            questionsCount = analysis.questions.size,
            patternsCount = analysis.patterns.size
        )
    }

    suspend fun deleteVaultDocument(id: Long) = withContext(Dispatchers.IO) {
        vaultDocDao.deleteDocumentById(id)
        questionDao.deleteQuestionsByDocId(id)
        patternDao.deletePatternsByDocId(id)
    }

    suspend fun insertVaultDocument(document: VaultDocumentEntity): Long = withContext(Dispatchers.IO) {
        vaultDocDao.insertDocument(document)
    }

    suspend fun toggleVaultBookmark(id: Long, isBookmarked: Boolean) = withContext(Dispatchers.IO) {
        vaultDocDao.setBookmarked(id, isBookmarked)
    }

    suspend fun recordVaultDocOpened(id: Long) = withContext(Dispatchers.IO) {
        vaultDocDao.updateLastOpened(id, System.currentTimeMillis())
    }

    // --- Chapter Database Operations ---

    fun getChaptersBySubject(subject: String): Flow<List<ChapterEntity>> {
        return chapterDao.getChaptersBySubject(subject)
    }

    fun getChaptersByBoardAndSubject(board: String, subject: String): Flow<List<ChapterEntity>> {
        return chapterDao.getChaptersByBoardAndSubject(board, subject)
    }

    suspend fun insertChapter(chapter: ChapterEntity): Long = withContext(Dispatchers.IO) {
        chapterDao.insertChapter(chapter)
    }

    suspend fun insertChapters(chapters: List<ChapterEntity>) = withContext(Dispatchers.IO) {
        chapterDao.insertChapters(chapters)
    }

    suspend fun deleteChapter(id: Long) = withContext(Dispatchers.IO) {
        chapterDao.deleteChapterById(id)
    }

    // --- Question Database Operations ---

    fun getQuestionsByFilter(subject: String, chapter: String = ""): Flow<List<QuestionEntity>> {
        return questionDao.getQuestionsBySubjectAndChapter(subject, chapter)
    }

    fun searchQuestions(query: String): Flow<List<QuestionEntity>> {
        return questionDao.searchQuestions(query)
    }

    suspend fun getRandomVaultQuestions(subject: String, chapter: String, count: Int): List<QuestionEntity> = withContext(Dispatchers.IO) {
        val list = questionDao.getRandomQuestions(subject, chapter, count)
        if (list.isNotEmpty()) list else questionDao.getRandomQuestions(subject, "", count)
    }

    suspend fun toggleQuestionImportant(id: Long, isImp: Boolean) = withContext(Dispatchers.IO) {
        questionDao.toggleImportant(id, isImp)
    }

    suspend fun deleteQuestion(id: Long) = withContext(Dispatchers.IO) {
        questionDao.deleteQuestionById(id)
    }

    // --- Pattern Database Operations ---

    fun getPatternsByFilter(subject: String, chapter: String = ""): Flow<List<PatternEntity>> {
        return patternDao.getPatternsBySubjectAndChapter(subject, chapter)
    }

    // --- Generated Tests & Attempts Operations ---

    suspend fun insertGeneratedTest(test: GeneratedTestEntity): Long = withContext(Dispatchers.IO) {
        generatedTestDao.insertTest(test)
    }

    suspend fun updateGeneratedTest(test: GeneratedTestEntity) = withContext(Dispatchers.IO) {
        generatedTestDao.updateTest(test)
    }

    suspend fun getGeneratedTestById(id: Long): GeneratedTestEntity? = withContext(Dispatchers.IO) {
        generatedTestDao.getTestById(id)
    }

    suspend fun deleteGeneratedTest(id: Long) = withContext(Dispatchers.IO) {
        generatedTestDao.deleteTestById(id)
    }

    suspend fun recordTestAttempt(attempt: TestAttemptEntity): Long = withContext(Dispatchers.IO) {
        val attemptId = testAttemptDao.insertAttempt(attempt)
        // Also update corresponding test if exists
        if (attempt.testId > 0) {
            val test = generatedTestDao.getTestById(attempt.testId)
            if (test != null) {
                generatedTestDao.updateTest(
                    test.copy(
                        completedTimestamp = System.currentTimeMillis(),
                        userScore = attempt.scoredMarks,
                        accuracyPercent = attempt.accuracyPercentage,
                        isCompleted = true
                    )
                )
            }
        }
        attemptId
    }

    fun getAttemptsForTest(testId: Long): Flow<List<TestAttemptEntity>> {
        return testAttemptDao.getAttemptsForTest(testId)
    }

    suspend fun deleteTestAttempt(attemptId: Long) = withContext(Dispatchers.IO) {
        testAttemptDao.deleteAttemptById(attemptId)
    }


    // Dynamic Current/Next Activity Finder
    fun findCurrentAndNextBlock(blocks: List<TimelineBlockEntity>): Pair<TimelineBlockEntity?, TimelineBlockEntity?> {
        if (blocks.isEmpty()) return Pair(null, null)

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentMinutesOfDay = currentHour * 60 + currentMinute

        // Convert "HH:mm" to minutes of day
        fun timeToMinutes(timeStr: String): Int {
            val parts = timeStr.split(":")
            if (parts.size != 2) return 0
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            return h * 60 + m
        }

        // Find which block contains currentMinutesOfDay
        var currentBlock: TimelineBlockEntity? = null
        var nextBlock: TimelineBlockEntity? = null

        for (i in blocks.indices) {
            val block = blocks[i]
            val startMin = timeToMinutes(block.startTime)
            var endMin = timeToMinutes(block.endTime)
            if (endMin < startMin) {
                // cross midnight (e.g. 22:00 to 05:45)
                endMin += 24 * 60
            }

            val adjustedCurrent = if (currentMinutesOfDay < startMin && endMin > 24 * 60) {
                currentMinutesOfDay + 24 * 60
            } else {
                currentMinutesOfDay
            }

            if (adjustedCurrent in startMin until endMin) {
                currentBlock = block
                nextBlock = blocks.getOrNull((i + 1) % blocks.size)
                break
            }
        }

        if (currentBlock == null) {
            // Pick next upcoming
            for (block in blocks) {
                val startMin = timeToMinutes(block.startTime)
                if (startMin > currentMinutesOfDay) {
                    nextBlock = block
                    break
                }
            }
            if (nextBlock == null) nextBlock = blocks.firstOrNull()
        }

        return Pair(currentBlock, nextBlock)
    }

    // --- AI Tutor Chat Persistence ---
    val allAiMessages: Flow<List<AiChatMessageEntity>> = aiChatDao.getAllMessages()

    fun getAiMessagesBySubject(subject: String): Flow<List<AiChatMessageEntity>> {
        return aiChatDao.getMessagesBySubject(subject)
    }

    suspend fun insertAiMessage(message: AiChatMessageEntity): Long = withContext(Dispatchers.IO) {
        aiChatDao.insertMessage(message)
    }

    suspend fun clearAiMessages() = withContext(Dispatchers.IO) {
        aiChatDao.clearAllMessages()
    }

    suspend fun clearAiMessagesBySubject(subject: String) = withContext(Dispatchers.IO) {
        aiChatDao.clearMessagesBySubject(subject)
    }

    suspend fun toggleAiMessageBookmark(id: Long, isBookmarked: Boolean) = withContext(Dispatchers.IO) {
        aiChatDao.toggleBookmark(id, isBookmarked)
    }
}
