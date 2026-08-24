package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.BuildConfig
import com.example.R
import com.example.data.local.QuestionEntity
import com.example.data.local.RudraDatabase
import com.example.data.local.VaultDocumentEntity
import com.example.data.preferences.PreferencesManager
import com.example.data.vault.PdfVaultManager
import com.example.network.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Background CoroutineWorker that iterates through stored PDFs, extracts text via PdfVaultManager / PdfRenderer,
 * and calls the Gemini API to structure the content into 'Question', 'Chapter', and 'Difficulty' fields
 * for insertion into the Room Question Database.
 */
class PdfQuestionExtractionWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "PdfQuestionExtractionWorker"
        const val WORK_NAME = "pdf_question_extraction_work"
        const val NOTIFICATION_CHANNEL_ID = "pdf_extraction_channel"
        const val NOTIFICATION_ID = 2001

        // Input / Output data keys
        const val KEY_DOC_ID = "input_doc_id"
        const val KEY_FORCE_REANALYZE = "input_force_reanalyze"
        const val KEY_PROGRESS_PERCENT = "progress_percent"
        const val KEY_CURRENT_DOC_TITLE = "current_doc_title"
        const val KEY_PROCESSED_DOCS = "processed_docs"
        const val KEY_TOTAL_DOCS = "total_docs"
        const val KEY_TOTAL_QUESTIONS_EXTRACTED = "total_questions_extracted"
        const val KEY_STATUS_MESSAGE = "status_message"
    }

    private val database = RudraDatabase.getDatabase(context)
    private val vaultDocDao = database.vaultDocumentDao()
    private val chapterDao = database.chapterDao()
    private val questionDao = database.questionDao()
    private val patternDao = database.patternDao()
    private val preferencesManager = PreferencesManager(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val targetDocId = inputData.getLong(KEY_DOC_ID, -1L)
        val forceReanalyze = inputData.getBoolean(KEY_FORCE_REANALYZE, false)

        Log.i(TAG, "Starting PDF Question Extraction Background Worker. Target Doc ID: $targetDocId, Force: $forceReanalyze")

        // 1. Resolve Gemini API Key (from Preferences or BuildConfig)
        var apiKey = preferencesManager.geminiApiKeyFlow.first()
        if (apiKey.isBlank()) {
            apiKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
        }

        // 2. Discover PDFs to process
        // Query database for vault documents
        val allDocs = if (targetDocId > 0) {
            val doc = vaultDocDao.getDocumentById(targetDocId)
            if (doc != null) listOf(doc) else emptyList()
        } else {
            val docs = vaultDocDao.getAllDocuments().first()
            if (forceReanalyze) docs else docs.filter { !it.isAnalyzed || it.questionsCount == 0 }
        }

        // Also check if there are raw PDF files in the vault folder not yet in Room
        val vaultDir = PdfVaultManager.getVaultDirectory(context)
        val filesOnDisk = vaultDir.listFiles { file -> file.extension.equals("pdf", ignoreCase = true) } ?: emptyArray()
        
        // Ingest any orphaned disk files if needed
        val knownPaths = allDocs.map { it.fileUriOrPath }.toSet()
        val extraDocsToProcess = mutableListOf<VaultDocumentEntity>()
        for (file in filesOnDisk) {
            if (!knownPaths.contains(file.absolutePath)) {
                val (pageCount, extractedText) = PdfVaultManager.extractTextAndPageCount(file)
                val newDoc = VaultDocumentEntity(
                    fileName = file.name,
                    title = file.name.removeSuffix(".pdf").replace("_", " "),
                    fileUriOrPath = file.absolutePath,
                    fileSizeBytes = file.length(),
                    pageCount = pageCount,
                    subject = inferSubjectFromFileName(file.name),
                    category = "QUESTION_BANK",
                    board = "BSEB",
                    uploadTimestamp = System.currentTimeMillis(),
                    extractedText = extractedText,
                    isAnalyzed = false
                )
                val newId = vaultDocDao.insertDocument(newDoc)
                extraDocsToProcess.add(newDoc.copy(id = newId))
            }
        }

        val totalDocsList = allDocs + extraDocsToProcess
        val totalCount = totalDocsList.size

        if (totalCount == 0) {
            Log.i(TAG, "No pending stored PDFs found for extraction.")
            setProgress(
                workDataOf(
                    KEY_PROGRESS_PERCENT to 100,
                    KEY_STATUS_MESSAGE to "No pending PDFs to extract",
                    KEY_TOTAL_QUESTIONS_EXTRACTED to 0,
                    KEY_PROCESSED_DOCS to 0,
                    KEY_TOTAL_DOCS to 0
                )
            )
            return@withContext Result.success(
                workDataOf(
                    KEY_TOTAL_QUESTIONS_EXTRACTED to 0,
                    KEY_PROCESSED_DOCS to 0,
                    KEY_STATUS_MESSAGE to "Completed: 0 PDFs needed processing"
                )
            )
        }

        var totalExtractedQuestions = 0
        var processedDocsCount = 0

        try {
            totalDocsList.forEachIndexed { index, doc ->
                if (isStopped) {
                    Log.w(TAG, "Worker stopped by WorkManager.")
                    return@withContext Result.retry()
                }

                val currentProgress = ((index.toFloat() / totalCount.toFloat()) * 100).toInt()
                setProgress(
                    workDataOf(
                        KEY_PROGRESS_PERCENT to currentProgress,
                        KEY_CURRENT_DOC_TITLE to doc.title,
                        KEY_PROCESSED_DOCS to processedDocsCount,
                        KEY_TOTAL_DOCS to totalCount,
                        KEY_TOTAL_QUESTIONS_EXTRACTED to totalExtractedQuestions,
                        KEY_STATUS_MESSAGE to "Extracting: ${doc.title} (${index + 1}/$totalCount)"
                    )
                )

                // 3. Extract text from the PDF file if not already extracted
                var textToAnalyze = doc.extractedText
                if (textToAnalyze.isBlank() || textToAnalyze.length < 50) {
                    val pdfFile = File(doc.fileUriOrPath)
                    if (pdfFile.exists()) {
                        val (_, freshText) = PdfVaultManager.extractTextAndPageCount(pdfFile)
                        textToAnalyze = freshText
                    }
                }

                // 4. Structure content into Question, Chapter, and Difficulty using Gemini API via PdfTextStructuringHelper
                val extractedResult = com.example.network.PdfTextStructuringHelper.extractAndStructureWithGemini(
                    apiKey = apiKey,
                    rawText = textToAnalyze,
                    docId = doc.id,
                    docName = doc.fileName,
                    subject = doc.subject,
                    board = doc.board
                )

                // 5. Insert structured questions into Question Database
                if (extractedResult.questions.isNotEmpty()) {
                    // Remove old questions for this doc to prevent duplicate accumulation
                    questionDao.deleteQuestionsByDocId(doc.id)
                    questionDao.insertQuestions(extractedResult.questions)
                    totalExtractedQuestions += extractedResult.questions.size
                }

                if (extractedResult.chapters.isNotEmpty()) {
                    chapterDao.insertChapters(extractedResult.chapters)
                }

                if (extractedResult.patterns.isNotEmpty()) {
                    patternDao.deletePatternsByDocId(doc.id)
                    patternDao.insertPatterns(extractedResult.patterns)
                }

                // 6. Update Vault Document status
                vaultDocDao.updateAnalyzedData(
                    id = doc.id,
                    isAnalyzed = true,
                    summary = extractedResult.summary,
                    qCount = extractedResult.questions.size,
                    pCount = extractedResult.patterns.size,
                    extractedText = textToAnalyze
                )

                processedDocsCount++
            }

            val finalMessage = "Extracted $totalExtractedQuestions questions across $processedDocsCount stored PDFs into Question Database."
            Log.i(TAG, "Success: $finalMessage")

            setProgress(
                workDataOf(
                    KEY_PROGRESS_PERCENT to 100,
                    KEY_STATUS_MESSAGE to finalMessage,
                    KEY_PROCESSED_DOCS to processedDocsCount,
                    KEY_TOTAL_DOCS to totalCount,
                    KEY_TOTAL_QUESTIONS_EXTRACTED to totalExtractedQuestions
                )
            )

            // Post completion notification
            showCompletionNotification(processedDocsCount, totalExtractedQuestions)

            Result.success(
                workDataOf(
                    KEY_TOTAL_QUESTIONS_EXTRACTED to totalExtractedQuestions,
                    KEY_PROCESSED_DOCS to processedDocsCount,
                    KEY_STATUS_MESSAGE to finalMessage
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error during PDF question extraction worker: ${e.message}", e)
            Result.failure(
                workDataOf(
                    KEY_STATUS_MESSAGE to "Extraction error: ${e.message}",
                    KEY_TOTAL_QUESTIONS_EXTRACTED to totalExtractedQuestions,
                    KEY_PROCESSED_DOCS to processedDocsCount
                )
            )
        }
    }

    private fun inferSubjectFromFileName(fileName: String): String {
        val lower = fileName.lowercase()
        return when {
            lower.contains("phy") -> "Physics"
            lower.contains("chem") -> "Chemistry"
            lower.contains("bio") -> "Biology"
            lower.contains("math") -> "Mathematics"
            lower.contains("hin") -> "Hindi"
            lower.contains("eng") -> "English"
            else -> "Physics"
        }
    }

    private fun showCompletionNotification(docsCount: Int, questionsCount: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "PDF Extraction & Question Ingestion",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for background PDF question extraction and database ingestion."
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("PDF Question Ingestion Complete")
                .setContentText("Extracted $questionsCount questions from $docsCount stored PDFs into Question Database.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to post notification: ${e.message}")
        }
    }
}
